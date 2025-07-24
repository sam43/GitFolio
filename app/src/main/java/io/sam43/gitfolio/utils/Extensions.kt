package io.sam43.gitfolio.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import io.sam43.gitfolio.domain.model.UserDetail
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


// String formatting extension
@SuppressLint("DefaultLocale")
fun Int.toFormattedCountString(includeDecimalForThousands: Boolean = false): String =
    when {
        this < 1000 -> this.toString()
        this < 1_000_000 -> { // Thousands
            if (includeDecimalForThousands) {
                String.format("%.1fK", this / 1000.0).replace(".0K", "K")
            } else {
                "${this / 1000}K"
            }
        }

        this < 1_000_000_000 -> { // Millions
            String.format("%.1fM", this / 1_000_000.0).replace(".0M", "M")
        }

        else -> { // Billions and above
            String.format("%.1fB", this / 1_000_000_000.0).replace(".0B", "B")
        }
    }

fun Context.isOnline(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } else {
        @Suppress("DEPRECATION")
        val networkInfo = connectivityManager.activeNetworkInfo
        networkInfo?.isConnected == true
    }
}

// Get app version without BuildConfig
fun Context.appVersionName(): String {
    return try {
        val packageInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            packageManager.getPackageInfo(packageName, 0)
        }
        packageInfo.versionName ?: "---"
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        "---"
    }

}

// Placeholders and processors
fun String.urlDecoded(): String =
    URLDecoder.decode(this, StandardCharsets.UTF_8.name())

fun createPlaceholderUser(
    username: String,
    avatarUrl: String,
    displayName: String
): UserDetail = UserDetail(
    login = username,
    id = 0,
    avatarUrl = avatarUrl.urlDecoded(),
    name = displayName.urlDecoded(),
    htmlUrl = "",
    company = null,
    blog = null,
    location = null,
    email = null,
    bio = null,
    publicRepos = 0,
    followers = 0,
    following = 0
)
