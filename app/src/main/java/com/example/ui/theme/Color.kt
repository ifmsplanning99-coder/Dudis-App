package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Traveloka-Inspired Brand Color Palette
val TravelokaBluePrimary = Color(0xFF0194F3)   // Primary vibrant Traveloka blue
val TravelokaBlueSecondary = Color(0xFF0D6EFD) // Secondary deep royal blue
val TravelokaBlueLight = Color(0xFFE1F2FF)     // Ultra light blue container
val TravelokaBlueDark = Color(0xFF0064B0)      // Deep brand blue for dark headers

// System Status Colors
val SuccessGreen = Color(0xFF00C851)           // Success color
val SuccessGreenBg = Color(0xFFE8F8F0)
val WarningYellow = Color(0xFFFFBB33)          // Warning color (expiring soon)
val WarningYellowBg = Color(0xFFFFF8E6)
val DangerRed = Color(0xFFFF4444)              // Danger / expired / high severity
val DangerRedBg = Color(0xFFFFEEEE)

// Legacy alias mapping to maintain component compatibility
val QcBluePrimary = TravelokaBluePrimary
val QcBlueDark = TravelokaBlueDark
val QcBlueLight = TravelokaBlueLight
val QcBlueSecondary = TravelokaBlueSecondary
val QcBlueTertiary = Color(0xFF00ACC1)

// Neutral Canvas & Surface
val QcBackground = Color(0xFFF6F8FB)          // Modern clean app background
val QcSurface = Color(0xFFFFFFFF)             // Pure white cards
val QcSurfaceVariant = Color(0xFFEDF2F7)      // Light neutral surface
val QcOutline = Color(0xFFDCE4EC)             // Crisp light border

// Status & Severity Colors
val SeverityLowBg = SuccessGreenBg
val SeverityLowText = Color(0xFF007E33)
val SeverityMediumBg = WarningYellowBg
val SeverityMediumText = Color(0xFFC67D00)
val SeverityHighBg = DangerRedBg
val SeverityHighText = Color(0xFFCC0000)

// Duration Capacity Gauge
val GaugeSafe = SuccessGreen
val GaugeWarning = WarningYellow
val GaugeDanger = DangerRed

// Text Colors (High contrast for clear readability on modern cards)
val QcTextPrimary = Color(0xFF0F172A)     // Deep slate navy 900
val QcTextSecondary = Color(0xFF334155)   // Slate 700 - bold crisp readable dark grey
val QcTextMuted = Color(0xFF64748B)       // Slate 500

