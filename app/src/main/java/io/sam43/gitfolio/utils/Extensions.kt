package io.sam43.gitfolio.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

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