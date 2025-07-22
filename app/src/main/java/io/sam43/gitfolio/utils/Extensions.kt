package io.sam43.gitfolio.utils

import android.annotation.SuppressLint

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
