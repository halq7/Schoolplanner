package com.future.schoolplanner

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.future.schoolplanner.ui.SchoolPlannerApp
import com.future.schoolplanner.ui.GradeViewModel
import com.future.schoolplanner.ui.theme.SchoolplannerTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val language = prefs.getString("language", "vi") ?: "vi"
        val locale = Locale.forLanguageTag(language)
        Locale.setDefault(locale)
        val config = newBase.resources.configuration
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

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

