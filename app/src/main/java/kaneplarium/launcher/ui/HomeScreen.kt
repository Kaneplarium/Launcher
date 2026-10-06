package kaneplarium.launcher.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kaneplarium.launcher.data.AppModel

// Pure OLED Black Color Palette & Dark Red for Notifications
val OLEDBlack = Color(0xFF000000)
val DarkSurface = Color(0xFF121212)
val PureWhite = Color(0xFFFFFFFF)
val MutedText = Color(0xFF888888)
val DarkRedNotification = Color(0xFFC62828) // Dark Red for apps with active notifications

@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedAppForMenu by remember { mutableStateOf<AppModel?>(null) }

    PixelDarkTheme {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(OLEDBlack)
                .statusBarsPadding(), // Full-screen gesture navigation support
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally, // Centered horizontally
            ) {
                // Notification Access Banner if permission missing
                if (uiState is AppsUiState.Success && !(uiState as AppsUiState.Success).isNotificationPermissionGranted) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { viewModel.openNotificationSettings() })
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "🔔 Tippe hier für Benachrichtigungszugriff",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.SansSerif,
                            color = MutedText,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Centered App List (Horizontally and vertically centered in the middle)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    when (uiState) {
                        is AppsUiState.Loading -> {
                            CircularProgressIndicator(color = PureWhite)
                        }

                        is AppsUiState.Success -> {
                            val successState = uiState as AppsUiState.Success
                            if (successState.apps.isEmpty()) {
                                Text(
                                    text = "Keine Apps vorhanden",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = FontFamily.SansSerif,
                                    color = MutedText,
                                    textAlign = TextAlign.Center,
                                )
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(vertical = 16.dp),
                                    verticalArrangement = Arrangement.Center, // Vertically centered
                                    horizontalAlignment = Alignment.CenterHorizontally, // Horizontally centered
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    items(
                                        items = successState.apps,
                                        key = { it.packageName },
                                    ) { app ->
                                        CenteredAppTextRow(
                                            app = app,
                                            onClick = { viewModel.launchApp(app.packageName) },
                                            onLongClick = { selectedAppForMenu = app },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // App Context Menu Dialog
        selectedAppForMenu?.let { app ->
            AppContextMenuDialog(
                app = app,
                onDismiss = { selectedAppForMenu = null },
                onOpenDetails = {
                    viewModel.openAppDetails(app.packageName)
                    selectedAppForMenu = null
                },
                onUninstall = {
                    viewModel.uninstallApp(app.packageName)
                    selectedAppForMenu = null
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CenteredAppTextRow(
    app: AppModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(vertical = 10.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = app.label,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                fontWeight = if (app.hasNotification) FontWeight.Bold else FontWeight.Normal,
            ),
            fontFamily = FontFamily.SansSerif,
            color = if (app.hasNotification) DarkRedNotification else PureWhite,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContextMenuDialog(
    app: AppModel,
    onDismiss: () -> Unit,
    onOpenDetails: () -> Unit,
    onUninstall: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = PureWhite,
                textAlign = TextAlign.Center,
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.SansSerif,
                color = MutedText,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            DropdownMenuItem(
                text = {
                    Text("App-Info", fontFamily = FontFamily.SansSerif, color = PureWhite)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PureWhite,
                    )
                },
                onClick = onOpenDetails,
            )

            DropdownMenuItem(
                text = {
                    Text("Deinstallieren", fontFamily = FontFamily.SansSerif, color = Color(0xFFFF5555))
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFFF5555),
                    )
                },
                onClick = onUninstall,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PixelDarkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = OLEDBlack,
            surface = DarkSurface,
            onBackground = PureWhite,
            onSurface = PureWhite,
            primary = PureWhite,
            onPrimary = Color.Black,
        ),
        content = content,
    )
}
