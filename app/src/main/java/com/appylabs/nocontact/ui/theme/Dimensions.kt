package com.appylabs.nocontact.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class NoContactDimensions(
    val xxs: Dp = 4.dp,
    val xs: Dp = 6.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 30.dp,
    val screenPadding: Dp = 14.dp,
    val cardPadding: Dp = 10.dp,
    val cardRadius: Dp = 10.dp,
    val heroRadius: Dp = 12.dp,
    val pillRadius: Dp = 100.dp,
    val navHeight: Dp = 56.dp,
    val icon: Dp = 16.dp,
    val iconLarge: Dp = 20.dp,
    val bottomSafeSpacing: Dp = 6.dp
) {
    companion object {
        fun forScreenWidth(widthDp: Int): NoContactDimensions {
            return when {
                widthDp >= 840 -> NoContactDimensions(
                    screenPadding = 20.dp,
                    cardPadding = 12.dp,
                    navHeight = 58.dp,
                    iconLarge = 22.dp
                )
                widthDp >= 600 -> NoContactDimensions(
                    screenPadding = 18.dp,
                    cardPadding = 12.dp,
                    navHeight = 58.dp,
                    iconLarge = 22.dp
                )
                else -> NoContactDimensions()
            }
        }
    }
}

val LocalNoContactDimensions = compositionLocalOf { NoContactDimensions() }
