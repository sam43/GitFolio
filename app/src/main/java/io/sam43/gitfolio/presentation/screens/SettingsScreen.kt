package io.sam43.gitfolio.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.sam43.gitfolio.presentation.viewmodels.ThemeViewModel
import io.sam43.gitfolio.utils.appVersionName

@Composable
fun SettingsScreen(
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    var showAboutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Dark Theme")
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { themeViewModel.toggleTheme() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = { showAboutDialog = true }
        ) { Text(text = "About GitFolio", fontFamily = FontFamily.Monospace) }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "v${context.appVersionName()}",
            color = Color.DarkGray,
            modifier = Modifier.align(Alignment.End)
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(text = "What\'s init?", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
            text = { Text(text = "\n1.GitHub Explorer: Easily search and view GitHub user profiles and their repositories.\n2.Modern Android: Built with the latest technologies including Jetpack Compose (UI), Kotlin Coroutines, and MVI architecture.\n3.Smooth Experience: Features Material 3 design and Shared Element Transitions for seamless navigation.\n4.Core Tech: Utilizes Retrofit for API calls, Coil for image loading, and Hilt for dependency management.\n5.Developer: Created by Sadat Sayem. \nContact: scode43@gmail.com", fontFamily = FontFamily.Monospace) },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text(text = "Thanks!", fontFamily = FontFamily.Monospace)
                }
            }
        )
    }
}