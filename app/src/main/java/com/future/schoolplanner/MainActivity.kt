package com.future.schoolplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.future.schoolplanner.ui.SchoolPlannerApp
import com.future.schoolplanner.ui.GradeViewModel
import com.future.schoolplanner.ui.theme.SchoolplannerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: GradeViewModel = viewModel { GradeViewModel(this@MainActivity) }
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val useDynamicColors by viewModel.useDynamicColors.collectAsState()
            val useAmoledTheme by viewModel.useAmoledTheme.collectAsState()
            val customAccentColor by viewModel.customAccentColor.collectAsState()

            SchoolplannerTheme(
                darkTheme = isDarkTheme,
                dynamicColor = useDynamicColors,
                amoledTheme = useAmoledTheme,
                customAccentColor = customAccentColor
            ) {
                SchoolPlannerApp(viewModel = viewModel)
            }
        }
    }
}

