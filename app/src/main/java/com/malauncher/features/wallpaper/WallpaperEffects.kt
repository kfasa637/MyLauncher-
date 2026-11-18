package com.malauncher.features.wallpaper

/**
 * Wallpaper effect settings are stored independently from the launcher theme.
 * Parallax uses motion/scroll offsets; blur is applied to the wallpaper layer only.
 */
data class WallpaperEffects(
    val parallaxEnabled: Boolean = false,
    val parallaxStrength: Float = 0.35f,
    val blurRadius: Float = 0f,
    val dimAmount: Float = 0f
)
