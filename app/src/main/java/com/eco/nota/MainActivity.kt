package com.eco.nota

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.eco.nota.models.NotaViewModel
import com.eco.nota.ui.navigation.NotaApp
import com.eco.nota.ui.theme.NotaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = ViewModelProvider(this)[NotaViewModel::class.java]
        setContent {
            NotaTheme {
                NotaApp(viewModel = viewModel)
            }
        }
    }
}
