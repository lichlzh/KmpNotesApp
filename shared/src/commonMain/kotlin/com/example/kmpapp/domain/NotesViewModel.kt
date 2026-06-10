package com.example.kmpapp.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kmpapp.data.Note
import com.example.kmpapp.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * 共享 ViewModel —— 使用 JetBrains 的 lifecycle-viewmodel-compose 库，
 * 在 Android 和 iOS 上共享同一份 ViewModel 代码。
 *
 * 管理笔记列表、搜索、编辑状态等 UI 逻辑。
 * 这是 KMP 架构中最有价值的共享层：业务逻辑只写一次。
 */
class NotesViewModel : ViewModel() {

    private val repository = NoteRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    /** 笔记列表：置顶优先 → 按更新时间倒序，受搜索关键词过滤 */
    val notes: StateFlow<List<Note>> = combine(
        repository.notes,
        _searchQuery,
        _selectedCategory
    ) { allNotes, query, category ->
        var filtered = allNotes
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
            }
        }
        if (category != null) {
            filtered = filtered.filter { it.colorHex == categoryColorMap[category] }
        }
        filtered.sortedWith(
            compareByDescending<Note> { it.isPinned }
                .thenByDescending { it.updatedAt }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 笔记总数（响应式） */
    val totalCount: StateFlow<Int> = repository.notes
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch() {
        _isSearchActive.value = !_isSearchActive.value
        if (!_isSearchActive.value) _searchQuery.value = ""
    }

    fun addNote(title: String, content: String, colorHex: Long = 0xFFFFF3E0, weatherSnapshot: String? = null) {
        if (title.isBlank() && content.isBlank()) return
        repository.addNote(title, content, colorHex, weatherSnapshot)
    }

    fun updateNote(note: Note) {
        if (note.title.isBlank() && note.content.isBlank()) return
        repository.updateNote(note.id, note.title, note.content, note.colorHex, note.isPinned)
    }

    fun deleteNote(id: Long) {
        repository.deleteNote(id)
    }

    fun togglePin(id: Long) {
        repository.togglePin(id)
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }

    companion object {
        val categoryColorMap = mapOf(
            "默认" to 0xFFFFF3E0,
            "工作" to 0xFFE3F2FD,
            "个人" to 0xFFF3E5F5,
            "重要" to 0xFFFFEBEE,
            "灵感" to 0xFFE8F5E9
        )

        val colorOptions = listOf(
            0xFFFFF3E0, // 暖黄
            0xFFE3F2FD, // 浅蓝
            0xFFF3E5F5, // 浅紫
            0xFFFFEBEE, // 浅红
            0xFFE8F5E9, // 浅绿
            0xFFFBE9E7, // 浅橙
        )
    }
}
