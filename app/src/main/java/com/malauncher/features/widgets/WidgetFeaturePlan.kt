package com.malauncher.features.widgets

/**
 * Widget feature boundary.
 *
 * Planned implementation:
 * - Android AppWidgetHost/AppWidgetManager integration for real third-party widgets.
 * - Launcher-owned smart widgets (clock, battery, weather and device information).
 * - Resizable widget placement persisted in the home-screen layout.
 * - Floating widget overlay with explicit user permission where Android requires it.
 *
 * Kept separate from the home screen so widget rendering and placement can evolve
 * without coupling them to app discovery or launcher navigation.
 */
data class WidgetPlacement(
    val widgetId: Int,
    val page: Int,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)
