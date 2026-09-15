package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.GoogleAccountManager
import com.example.data.GoogleSignInState
import com.example.ui.TvScreen
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

data class NavItem(
    val screen: TvScreen,
    val titleRes: Int,
    val icon: ImageVector
)

val TvNavItems = listOf(
    NavItem(TvScreen.Search, R.string.nav_search, Icons.Default.Search),
    NavItem(TvScreen.Home, R.string.nav_home, Icons.Default.Home),
    NavItem(TvScreen.Trending, R.string.nav_trending, Icons.Default.Whatshot),
    NavItem(TvScreen.Music, R.string.nav_music, Icons.Default.MusicNote),
    NavItem(TvScreen.Gaming, R.string.nav_gaming, Icons.Default.SportsEsports),
    NavItem(TvScreen.News, R.string.nav_news, Icons.Default.Newspaper),
    NavItem(TvScreen.Library, R.string.nav_library, Icons.Default.VideoLibrary),
    NavItem(TvScreen.Settings, R.string.nav_settings, Icons.Default.Settings)
)

@Composable
fun TvNavigationRail(
    currentScreen: TvScreen? = null,
    currentRoute: String? = null,
    onScreenSelected: (TvScreen) -> Unit,
    accountManager: GoogleAccountManager? = null,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false
) {
    val railWidth by animateDpAsState(
        targetValue = if (isExpanded) 190.dp else 74.dp,
        label = "nav_rail_width"
    )

    val signInState = accountManager?.signInState?.collectAsState()?.value

    Column(
        modifier = modifier
            .width(railWidth)
            .fillMaxHeight()
            .background(Color(0xFF0D0E16))
            .border(
                width = 1.dp,
                color = Color(0x14FFFFFF)
            )
            .padding(vertical = 18.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        // App brand header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp, start = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF171824))
                    .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_logo),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier.size(28.dp)
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1
                )
            }
        }

        // Nav items
        TvNavItems.forEach { item ->
            val isSelected = when {
                currentRoute != null -> isRouteMatchingScreen(currentRoute, item.screen)
                currentScreen != null -> currentScreen == item.screen
                else -> false
            }
            TvNavButton(
                item = item,
                isSelected = isSelected,
                isExpanded = isExpanded,
                onClick = { onScreenSelected(item.screen) }
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Account status indicator at bottom of navigation rail (NuvioTV clean style)
        if (accountManager != null) {
            val interactionSource = remember { MutableInteractionSource() }
            val isFocused by interactionSource.collectIsFocusedAsState()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isFocused) Color.White else Color(0xFF161724))
                    .border(
                        width = if (isFocused) 2.dp else 1.dp,
                        color = if (isFocused) Color.White else Color(0x20FFFFFF),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onScreenSelected(TvScreen.Settings) }
                    )
                    .focusable(interactionSource = interactionSource)
                    .padding(8.dp)
                    .testTag("nav_account_profile")
            ) {
                when (signInState) {
                    is GoogleSignInState.SignedIn -> {
                        if (!signInState.profile.avatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = signInState.profile.avatarUrl,
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color(0x40FFFFFF), CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isFocused) Color(0xFF1A1C28) else Color(0xFF2A2D40)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = signInState.profile.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = signInState.profile.name,
                                    color = if (isFocused) Color(0xFF10111A) else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                Text(
                                    text = stringResource(R.string.account_status_signed_in),
                                    color = if (isFocused) Color(0xFF1B5E20) else Color(0xFF81C784),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Sign In",
                            tint = if (isFocused) Color(0xFF10111A) else Color(0xFFA0A3B6),
                            modifier = Modifier.size(32.dp)
                        )
                        if (isExpanded) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.account_sign_in_google),
                                color = if (isFocused) Color(0xFF10111A) else Color(0xFFA0A3B6),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TvNavButton(
    item: NavItem,
    isSelected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> Color(0x28FFFFFF)
            else -> Color.Transparent
        },
        label = "nav_item_bg"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color(0xFF10111A)
            isSelected -> Color.White
            else -> Color(0xFF8E91A6)
        },
        label = "nav_item_content_color"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (isFocused) 1.5.dp else 0.dp,
                color = if (isFocused) Color.White else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("nav_item_${item.screen::class.simpleName?.lowercase() ?: "nav"}")
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = stringResource(item.titleRes),
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )

        if (isExpanded) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(item.titleRes),
                color = contentColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

private fun isRouteMatchingScreen(route: String, screen: TvScreen): Boolean {
    return when (screen) {
        is TvScreen.Search -> route == "search" || route.startsWith("search")
        is TvScreen.Settings -> route == "settings" || route.startsWith("settings")
        is TvScreen.Trending -> route.contains("Trending")
        is TvScreen.Music -> route.contains("Music")
        is TvScreen.Gaming -> route.contains("Gaming")
        is TvScreen.News -> route.contains("News")
        is TvScreen.Library -> route == "library" || route.startsWith("library")
        is TvScreen.Home -> (route.startsWith("home") && !route.contains("Trending") && !route.contains("Music") && !route.contains("Gaming") && !route.contains("News"))
        is TvScreen.Player -> route.startsWith("player")
    }
}

