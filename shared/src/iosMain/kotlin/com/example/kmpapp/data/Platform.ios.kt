package com.example.kmpapp.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionDataDelegateProtocol
import platform.Foundation.NSURLSessionDataTask
import platform.Foundation.NSURLSessionTask
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSessionDelegateProtocol
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.darwin.NSObject

/**
 * iOS actual 实现 —— 使用 NSUserDefaults 持久化键值对。
 */
actual class PlatformKeyValueStorage actual constructor() {

    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    actual fun getString(key: String): String? =
        defaults.stringForKey(key)

    actual fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }
}

actual fun getPlatformName(): String = "iOS"

/**
 * iOS HTTP 实现 —— 使用 NSURLSession + delegate 模式。
 *
 * Kotlin/Native 对 NSURLSession 的 completion handler 重载绑定有限，
 * 因此改用 delegate 模式（NSURLSessionDataDelegate）接收数据，
 * 这是 iOS 网络编程的经典模式，在 KMP 中同样适用。
 */
@OptIn(ExperimentalForeignApi::class)
private class HttpHelper(
    private val onResult: (Result<String>) -> Unit
) : NSObject(), NSURLSessionDataDelegateProtocol, NSURLSessionDelegateProtocol {

    private val chunks = mutableListOf<ByteArray>()

    override fun URLSession(
        session: NSURLSession,
        dataTask: NSURLSessionDataTask,
        didReceiveData: NSData
    ) {
        val length = didReceiveData.length.toInt()
        if (length > 0) {
            val bytes = ByteArray(length)
            bytes.usePinned { pinned ->
                platform.posix.memcpy(
                    pinned.addressOf(0),
                    didReceiveData.bytes,
                    length.toULong()
                )
            }
            chunks.add(bytes)
        }
    }

    override fun URLSession(
        session: NSURLSession,
        task: NSURLSessionTask,
        didCompleteWithError: NSError?
    ) {
        if (didCompleteWithError != null) {
            onResult(Result.failure(Exception(didCompleteWithError.localizedDescription)))
        } else {
            val allBytes = chunks.fold(ByteArray(0)) { acc, bytes -> acc + bytes }
            val body = allBytes.decodeToString()
            onResult(Result.success(body))
        }
    }
}

actual fun httpGet(url: String, onResult: (Result<String>) -> Unit) {
    val nsUrl = NSURL.URLWithString(url) ?: run {
        onResult(Result.failure(Exception("Invalid URL: $url")))
        return
    }
    val request = NSURLRequest.requestWithURL(nsUrl)
    val helper = HttpHelper(onResult)
    val session = NSURLSession.sessionWithConfiguration(
        configuration = platform.Foundation.NSURLSessionConfiguration.defaultSessionConfiguration,
        delegate = helper,
        delegateQueue = null
    )
    val task = session.dataTaskWithRequest(request)
    task.resume()
}
