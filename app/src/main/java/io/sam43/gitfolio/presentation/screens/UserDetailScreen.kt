@file:OptIn(ExperimentalSharedTransitionApi::class)

package io.sam43.gitfolio.presentation.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.sam43.gitfolio.data.helper.ErrorType
import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.presentation.common.CenteredCircularProgressIndicator
import io.sam43.gitfolio.presentation.common.ErrorScreen
import io.sam43.gitfolio.presentation.common.LoadImageWith
import io.sam43.gitfolio.presentation.state.UserProfileState
import io.sam43.gitfolio.presentation.state.hasErrorWithoutUser
import io.sam43.gitfolio.presentation.viewmodels.UserProfileDetailsViewModel
import io.sam43.gitfolio.utils.createPlaceholderUser
import io.sam43.gitfolio.utils.toFormattedCountString

@Composable
fun GithubProfileScreen(
    username: String,
    avatarUrl: String,
    displayName: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    navController: NavController,
    viewModel: UserProfileDetailsViewModel = hiltViewModel()
) {
    LaunchedEffect(username) {
        viewModel.fetchUserProfileByUsername(username)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val displayUser = remember(state.user, avatarUrl, displayName, username) {
        state.user ?: createPlaceholderUser(username, avatarUrl, displayName)
    }
    when {
        state.hasErrorWithoutUser() -> {
            ErrorScreen(error = state.error ?: ErrorType.UnknownError())
        }

        else -> {
            UserProfileView(
                user = displayUser,
                profileState = state,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun UserProfileView(
    user: UserDetail,
    profileState: UserProfileState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBackClick: () -> Unit
) {
    val headerHeight = 280.dp

    val headerHeightPx = with(LocalDensity.current) { headerHeight.toPx() }

    val headerOffsetHeightPx = remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = headerOffsetHeightPx.floatValue + delta
                headerOffsetHeightPx.floatValue = newOffset.coerceIn(-headerHeightPx, 0f)
                return Offset.Zero
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                profileState.isLoading -> CenteredCircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
                profileState.hasLoaded && profileState.repositories.isEmpty() && profileState.error == null -> {
                    ErrorScreen(error = ErrorType.UnknownError("No repositories found"))
                }
                profileState.error != null -> {
                    ErrorScreen(error = profileState.error)
                }
                else -> {
                    RepoList(
                        reposList = profileState.repositories,
                        headerHeight = headerHeight,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            CollapsingToolbar(
                user = user,
                headerHeight = headerHeight,
                offset = headerOffsetHeightPx.floatValue,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .graphicsLayer {
                        translationY = headerOffsetHeightPx.floatValue
                    }
            )

            CollapsingTopBar(
                user = user,
                headerHeight = headerHeight,
                offset = headerOffsetHeightPx.floatValue,
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
fun RepoList(reposList: List<Repo>, headerHeight: Dp, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(top = headerHeight)
    ) {
        items(reposList) { repo ->
            RepoListItem(repo)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        }
    }
}

@Composable
fun CollapsingToolbar(
    user: UserDetail,
    headerHeight: Dp,
    offset: Float,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val collapseFraction =
        (offset / -with(LocalDensity.current) { headerHeight.toPx() }).coerceIn(0f, 1f)
    val imageSize = (120 * (1 - collapseFraction * 0.5f)).dp

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        // Main content centered
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            with(sharedTransitionScope) {
                user.avatarUrl.LoadImageWith(
                    modifier = Modifier
                        .size(imageSize)
                        .clip(RectangleShape)
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "user-avatar-${user.login}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                        .graphicsLayer {
                            translationY = offset * 0.4f
                        }
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "@${user.login}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "username-${user.login}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                        .graphicsLayer {
                            alpha = 1f - collapseFraction * 2
                        }
                )
                Text(
                    user.name ?: "---",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.graphicsLayer { alpha = 1f - collapseFraction * 2 }
                )
            }
            Spacer(Modifier.height(16.dp))
            FollowerInfo(
                followers = user.followers,
                following = user.following,
                modifier = Modifier.graphicsLayer { alpha = 1f - collapseFraction * 2 }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingTopBar(user: UserDetail, headerHeight: Dp, offset: Float, onBackClick: () -> Unit) {
    val headerHeightPx = with(LocalDensity.current) { headerHeight.toPx() }
    val collapseThreshold = headerHeightPx * 0.7f

    val toolbarAlpha by animateFloatAsState(
        targetValue = if (-offset > collapseThreshold) 1f else 0f,
        label = "Toolbar Alpha"
    )

    val iconTint by animateColorAsState(
        targetValue = if (-offset > collapseThreshold) MaterialTheme.colorScheme.onSurface else Color.White,
        label = "Icon Tint"
    )


    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                user.avatarUrl.LoadImageWith(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .graphicsLayer { alpha = toolbarAlpha }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    user.login,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    modifier = Modifier.graphicsLayer { alpha = toolbarAlpha }
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    modifier = Modifier.size(32.dp),
                    contentDescription = "Back",
                    tint = iconTint
                )
            }
        },
        actions = {
            Text(
                user.followers.toFollowersString().plus(" • ")
                    .plus(user.following.toFollowingsString()),
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .graphicsLayer { alpha = toolbarAlpha }
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = toolbarAlpha),
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun RepoListItem(repo: Repo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(repo.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(repo.description ?: "", color = Color.Gray, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Star,
                    contentDescription = "Stars",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    repo.stargazersCount.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(getLanguageColorBy(repo.language), CircleShape)
                )
                Spacer(Modifier.width(4.dp))
                Text(repo.language ?: "", color = Color.Gray)
            }
        }
    }
}

private fun getLanguageColorBy(language: String?): Color =
    when (language) {
        "Kotlin" -> Color(0xFFF18E33)
        "Java" -> Color(0xFFB07219)
        "Python" -> Color(0xFF3572A5)
        "JavaScript" -> Color(0xFFF1E05A)
        "HTML" -> Color(0xFFE34C26)
        "CSS" -> Color(0xFF563D7C)
        "Shell" -> Color(0xFF89E051)
        "C" -> Color(0xFF555555)
        "C++" -> Color(0xFFF34B7D)
        "C#" -> Color(0xFF178600)
        "Swift" -> Color(0xFFF05138)
        "Rust" -> Color(0xFFDEA584)
        "TypeScript" -> Color(0xFF2B7489)
        "Go" -> Color(0xFF00ADD8)
        "PHP" -> Color(0xFF4F5D95)
        "Ruby" -> Color(0xFF701516)
        "Dart" -> Color(0xFF00B4AB)
        "R" -> Color(0xFF198CE7)
        "Lua" -> Color(0xFF000080)
        "MATLAB" -> Color(0xFFE16737)
        "Scala" -> Color(0xFFDC322F)
        "Haskell" -> Color(0xFF5E5086)
        else -> Color(0xFFD3D3D3)
    }

@Composable
fun FollowerInfo(followers: Int, following: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Text(
            followers.toFollowersString(),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Text(" followers • ", color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(8.dp))
        Text(
            following.toFollowingsString(),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Text(" following", color = Color.Gray)
    }
}

fun Int.toFollowersString(): String = this.toFormattedCountString()

fun Int.toFollowingsString(): String = this.toFormattedCountString()