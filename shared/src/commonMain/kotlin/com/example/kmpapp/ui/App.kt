package com.example.kmpapp.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kmpapp.domain.DashboardViewModel
import com.example.kmpapp.domain.NotesViewModel
import com.example.kmpapp.ui.screens.DashboardScreen
import com.example.kmpapp.ui.screens.NoteEditScreen
import com.example.kmpapp.ui.screens.NoteListScreen
import com.example.kmpapp.ui.screens.ThemeSettingsScreen
import com.example.kmpapp.ui.theme.DynamicAppTheme

/**
 * App 入口 Composable —— 导航和主题在这里统一管理。
 *
 * 导航层级：
 *   Dashboard（首页）→ List（全部笔记）→ Edit（编辑/新建）
 *   Dashboard（首页）→ ThemeSettings（主题设置）
 *
 * 使用 DynamicAppTheme 替代静态 AppTheme，支持运行时主题切换。
 * 主题配置由 ThemeEngine 管理，切换时 Compose 自动 recompose 全局 UI。
 *
 * 这是 Android 和 iOS 共同使用的 UI 入口：
 * - Android: 在 MainActivity 的 setContent 中调用 App()
 * - iOS: 在 MainViewController 中调用 App()
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun App(darkTheme: Boolean = false) {
    // 动态主题：ThemeEngine 管理当前主题，StateFlow 驱动 Compose 自动更新
    DynamicAppTheme {
        val notesViewModel: NotesViewModel = viewModel { NotesViewModel() }
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    when (targetState) {
                        is Screen.Edit -> slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                        is Screen.List -> slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                        is Screen.ThemeSettings -> slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                        else -> slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    is Screen.Dashboard -> DashboardScreen(
                        viewModel = DashboardViewModel,
                        onNoteClick = { noteId -> currentScreen = Screen.Edit(noteId) },
                        onViewAllNotes = { currentScreen = Screen.List },
                        onSearch = { currentScreen = Screen.List },
                        onToggleTheme = { currentScreen = Screen.ThemeSettings }
                    )
                    is Screen.List -> NoteListScreen(
                        viewModel = notesViewModel,
                        onNoteClick = { noteId -> currentScreen = Screen.Edit(noteId) },
                        onBack = { currentScreen = Screen.Dashboard }
                    )
                    is Screen.Edit -> NoteEditScreen(
                        noteId = screen.noteId,
                        viewModel = notesViewModel,
                        onBack = { currentScreen = Screen.List }
                    )
                    is Screen.ThemeSettings -> ThemeSettingsScreen(
                        onBack = { currentScreen = Screen.Dashboard }
                    )
                }
            }
        }
    }
}

/** 简易导航状态 */
sealed class Screen {
    data object Dashboard : Screen()
    data object List : Screen()
    data class Edit(val noteId: Long?) : Screen()
    data object ThemeSettings : Screen()
}
