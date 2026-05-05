package com.example.smartattend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.smartattend.ui.navigation.AppNavGraph
import com.example.smartattend.ui.theme.SmartAttendTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SmartAttendTheme {
                AppNavGraph()
            }
        }
    }
}