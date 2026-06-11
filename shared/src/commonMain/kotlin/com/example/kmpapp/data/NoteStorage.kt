package com.example.kmpapp.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * 平台存储抽象 —— KMP expect/actual 机制的核心演示。
 *
 * commonMain 定义接口，androidMain 用 SharedPreferences 实现，
 * iosMain 用 NSUserDefaults 实现。上层 Repository 完全不关心底层细节。
 */
interface NoteStorage {
    fun saveNotes(notes: List<Note>)
    fun loadNotes(): List<Note>
}

/**
 * 基于平台 KV 存储的 NoteStorage 实现。
 * androidMain / iosMain 各自提供 PlatformKeyValueStorage 的 actual 实现。
 *
 * 使用 kotlinx.serialization 自动序列化/反序列化，
 * 替代了之前的 Note.listToJson() / Note.listFromJson()。
 */
class PlatformNoteStorage : NoteStorage {
    private val storage = PlatformKeyValueStorage()

    override fun saveNotes(notes: List<Note>) {
        storage.putString(KEY_NOTES, AppJson.encodeToString<List<Note>>(notes))
    }

    override fun loadNotes(): List<Note> {
        val json = storage.getString(KEY_NOTES) ?: return emptyList()
        return try {
            AppJson.decodeFromString<List<Note>>(json)
        } catch (e: Exception) {
            // 旧格式数据不兼容时清空
            emptyList()
        }
    }

    private companion object {
        const val KEY_NOTES = "kmp_notes_data"
    }
}
