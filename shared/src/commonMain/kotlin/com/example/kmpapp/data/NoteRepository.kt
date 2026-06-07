package com.example.kmpapp.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

/**
 * 笔记仓库层 —— 封装 CRUD 操作，对外暴露 StateFlow 响应式数据流。
 *
 * 通过 NoteStorage 接口与平台存储交互，完全不感知底层是
 * SharedPreferences 还是 NSUserDefaults。
 * 这就是 KMP 的威力：一份业务逻辑，双平台运行。
 */
class NoteRepository(private val storage: NoteStorage = PlatformNoteStorage()) {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    init {
        _notes.value = storage.loadNotes()
    }

    fun addNote(title: String, content: String, colorHex: Long = 0xFFFFF3E0, weatherSnapshot: String? = null): Note {
        val now = Clock.System.now().toEpochMilliseconds()
        val note = Note(
            id = now,
            title = title,
            content = content,
            createdAt = now,
            updatedAt = now,
            colorHex = colorHex,
            weatherSnapshot = weatherSnapshot
        )
        _notes.value = _notes.value + note
        storage.saveNotes(_notes.value)
        return note
    }

    fun updateNote(
        id: Long,
        title: String,
        content: String,
        colorHex: Long,
        isPinned: Boolean
    ) {
        _notes.value = _notes.value.map { note ->
            if (note.id == id) {
                note.copy(
                    title = title,
                    content = content,
                    colorHex = colorHex,
                    isPinned = isPinned,
                    updatedAt = Clock.System.now().toEpochMilliseconds()
                )
            } else note
        }
        storage.saveNotes(_notes.value)
    }

    fun deleteNote(id: Long) {
        _notes.value = _notes.value.filter { it.id != id }
        storage.saveNotes(_notes.value)
    }

    fun togglePin(id: Long) {
        _notes.value = _notes.value.map { note ->
            if (note.id == id) note.copy(isPinned = !note.isPinned) else note
        }
        storage.saveNotes(_notes.value)
    }

    /** 置顶笔记排在前面，其余按更新时间倒序 */
    fun getSortedNotes(): List<Note> =
        _notes.value.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt })
}
