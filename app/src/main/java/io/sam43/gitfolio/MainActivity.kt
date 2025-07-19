package io.sam43.gitfolio

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.sam43.gitfolio.presentation.common.NetworkUiEvent
import io.sam43.gitfolio.presentation.common.theme.GitFolioTheme
import io.sam43.gitfolio.presentation.screens.UserListScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appInstance: App
        get() = applicationContext as App
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            appInstance.networkUiEvents.collectLatest { event ->
                when (event) {
                    is NetworkUiEvent.ShowToast -> {
                        Toast.makeText(this@MainActivity, event.message, Toast.LENGTH_LONG).show()
                    }
                    else -> { /* Do nothing */}
                }
            }
        }
        setContent {
            GitFolioTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    App(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun App(modifier: Modifier = Modifier) {
    UserListScreen(modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    GitFolioTheme {
        App()
    }
}