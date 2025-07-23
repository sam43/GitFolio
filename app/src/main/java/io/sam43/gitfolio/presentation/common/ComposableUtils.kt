package io.sam43.gitfolio.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import io.sam43.gitfolio.data.helper.ErrorType


@Composable
fun CenteredCircularProgressIndicator(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(), // Fill the entire available space
        verticalArrangement = Arrangement.Center, // Center vertically
        horizontalAlignment = Alignment.CenterHorizontally // Center horizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp), // Example size
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
    }
}

@Composable
fun String.LoadImageWith(
    modifier: Modifier,
    scale: ContentScale = ContentScale.Inside,
): Unit = AsyncImage(
    model = this,
    contentDescription = "avatar",
    contentScale = scale,
    modifier = modifier
)


@Composable
fun ErrorScreen(error: ErrorType) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(text = error.toString(), color = Color.Red)
    }
}
