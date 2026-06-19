package com.cemnect.android.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object TaskExecution : Screen("task_execution/{taskId}") {
        fun createRoute(taskId: String) = "task_execution/$taskId"
    }
}
