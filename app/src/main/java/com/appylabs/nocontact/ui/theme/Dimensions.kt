package com.appylabs.nocontact.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class NoContactDimensions(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 40.dp,
    val screenPadding: Dp = 16.dp,
    val cardPadding: Dp = 12.dp,
    val cardRadius: Dp = 12.dp,
    val heroRadius: Dp = 16.dp,
    val pillRadius: Dp = 100.dp,
    val navHeight: Dp = 56.dp,
    val icon: Dp = 20.dp,
    val iconLarge: Dp = 24.dp,
    val bottomSafeSpacing: Dp = 8.dp
) {
    companion object {
        fun forScreenWidth(widthDp: Int): NoContactDimensions {
            return when {
                widthDp >= 840 -> NoContactDimensions(
                    screenPadding = 24.dp,
                    cardPadding = 20.dp,
                    navHeight = 60.dp,
                    iconLarge = 26.dp
                )
                widthDp >= 600 -> NoContactDimensions(
                    screenPadding = 20.dp,
                    cardPadding = 16.dp,
                    navHeight = 60.dp,
                    iconLarge = 26.dp
                )
                else -> NoContactDimensions()
            }
        }
    }
}

val LocalNoContactDimensions = compositionLocalOf { NoContactDimensions() }
