package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.JarvisHudScreen
import com.example.ui.JarvisViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val jarvisViewModel: JarvisViewModel = viewModel()
            val isRedAlert by jarvisViewModel.isRedAlert.collectAsStateWithLifecycle()

            MyApplicationTheme(isRedAlert = isRedAlert) {
                JarvisHudScreen(
                    viewModel = jarvisViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
