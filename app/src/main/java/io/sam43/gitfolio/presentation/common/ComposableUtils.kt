package io.sam43.gitfolio.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import io.sam43.gitfolio.data.helper.ErrorHandler
import io.sam43.gitfolio.data.helper.ErrorType
import io.sam43.gitfolio.utils.isOnline


@Composable
fun CenteredCircularProgressIndicator(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
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
fun ErrorScreen(error: ErrorType?) {
    val context = LocalContext.current
    val isOnline by remember(context) { mutableStateOf(context.isOnline()) }
    val screenText = if (!isOnline) {
        ErrorHandler.getErrorMessage(ErrorType.NetworkError)
    } else if (error != null) {
        ErrorHandler.getErrorMessage(error)
    } else {
        "Loading..."
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = screenText,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            color = Color.Red
        )
    }
}
