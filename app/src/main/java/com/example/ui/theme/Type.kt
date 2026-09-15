package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val IbmPlexSansArabic = FontFamily(
    Font(R.font.ibm_plex_sans_arabic, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_arabic, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_arabic, FontWeight.SemiBold),
    Font(R.font.ibm_plex_sans_arabic, FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = IbmPlexSansArabic,
        fontWeight = FontWeight.Bold,
        fontSize = 52.sp,
        lineHeight = 60.sp
    ),
    displayMedium = TextStyle(
        fontFamily = IbmPlexSansArabic,
        fontWeight = FontWeight.Bold,
        fontSize = 42.sp,
        lineHeight = 48.sp
    ),
    displaySmall = TextStyle(
        fontFamily = IbmPlexSansArabic,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 40.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = IbmPlexSansArabic,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = IbmPlexSansArabic,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = IbmPlexSansArabic,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = IbmPlexSansArabic,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = IbmPlexSansArabic,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 23.sp
    ),
    titleSmall = TextStyle(
        fontFamily = IbmPlexSansArabic,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = IbmPlexSansArabic,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = IbmPlexSansArabic,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    bodySmall = TextStyle(
        fontFamily = IbmPlexSansArabic,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = IbmPlexSansArabic,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = IbmPlexSansArabic,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = IbmPlexSansArabic,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
)

