package com.cemre.capacitortodo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LauncherScreen(
                onOpenTodo = { userName ->
                    openTodoScreen(userName)
                }
            )
        }
    }

    private fun openTodoScreen(userName: String) {
        val encodedName = Uri.encode(userName)

        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("capacitortodo://open?userName=$encodedName")
        }

        startActivity(intent)
        finish()
    }
}