package com.launcher.samiboxtv.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.rememberAsyncImagePainter
import com.launcher.samiboxtv.R
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.presentation.components.AddAppDialog
import com.launcher.samiboxtv.presentation.components.AppCard
import com.launcher.samiboxtv.presentation.components.AppContextMenu
import com.launcher.samiboxtv.presentation.overlay.SystemMonitorLog
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCard
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val ADD_BUTTON_ID = "ADD_BUTTON"

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )

    uiState.selectedAppForMenu?.let { app ->
        AppContextMenu(
            appItem = app,
            onDismiss = { viewModel.onEvent(HomeUiEvent.CloseContextMenu) },
            onMove = { viewModel.onEvent(HomeUiEvent.StartMovingApp(app)) },
            onHide = { viewModel.onEvent(HomeUiEvent.HideApp(app)) }
        )
    }

    if (uiState.isAddDialogOpen) {
        AddAppDialog(
            hiddenApps = uiState.hiddenApps,
            onUnhideApp = { viewModel.onEvent(HomeUiEvent.UnhideApp(it)) },
            onDismiss = { viewModel.onEvent(HomeUiEvent.CloseAddDialog) }
        )
    }

    if (uiState.isSystemLogOpen) {
        SystemMonitorLog(
            onDismiss = { viewModel.onEvent(HomeUiEvent.CloseSystemLog) }
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LauncherHeader()

        val allItems: List<Any> = uiState.visibleApps + ADD_BUTTON_ID
        val appsPerPage = 10
        val pagedItems = allItems.chunked(appsPerPage)
        val pageCount = if (pagedItems.isEmpty()) 1 else pagedItems.size
        val pagerState = rememberPagerState(pageCount = { pageCount })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            val itemsOnThisPage = if (pagedItems.isNotEmpty()) pagedItems[pageIndex] else emptyList()

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalArrangement = Arrangement.spacedBy(28.dp),
                    userScrollEnabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 58.dp, end = 58.dp, top = 6.dp, bottom = 36.dp)
                ) {
                    items(
                        items = itemsOnThisPage,
                        key = { item ->
                            if (item is AppItem) item.packageName else ADD_BUTTON_ID
                        }
                    ) { item ->
                        when (item) {
                            is AppItem -> {
                                val isEditing = uiState.editingApp?.packageName == item.packageName
                                AppCard(
                                    appItem = item,
                                    onClick = {
                                        if (!isEditing) onEvent(HomeUiEvent.LaunchApp(item))
                                    },
                                    onLongClick = { onEvent(HomeUiEvent.OpenContextMenu(item)) },
                                    isEditing = isEditing,
                                    onMoveLeft = { onEvent(HomeUiEvent.MoveApp(item, -1)) },
                                    onMoveRight = { onEvent(HomeUiEvent.MoveApp(item, 1)) },
                                    onMoveUp = { onEvent(HomeUiEvent.MoveApp(item, -5)) },
                                    onMoveDown = { onEvent(HomeUiEvent.MoveApp(item, 5)) },
                                    onExitEdit = { onEvent(HomeUiEvent.FinishMovingApp) }
                                )
                            }
                            else -> {
                                val addInteractionSource = remember { MutableInteractionSource() }
                                val addIsFocused by addInteractionSource.collectIsFocusedAsState()

                                Card(
                                    onClick = { onEvent(HomeUiEvent.OpenAddDialog) },
                                    interactionSource = addInteractionSource,
                                    shape = CardDefaults.shape(RoundedCornerShape(8.dp)),
                                    colors = CardDefaults.colors(
                                        containerColor = CyberCard,
                                        focusedContainerColor = Color(0xFF1B2238)
                                    ),
                                    border = CardDefaults.border(
                                        focusedBorder = Border(
                                            border = BorderStroke(2.dp, CyberCyan),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    )
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .fillMaxSize()
                                        ) {
                                            Image(
                                                painter = rememberAsyncImagePainter(model = R.drawable.ic_add_retro),
                                                contentDescription = "Agregar",
                                                colorFilter = ColorFilter.tint(
                                                    if (addIsFocused) CyberCyan else CyberGrey
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(1f),
                                                contentScale = ContentScale.Fit
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Añadir Aplicación",
                                                color = if (addIsFocused) CyberCyan else Color.White,
                                                fontWeight = if (addIsFocused) FontWeight.Bold else FontWeight.Normal,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.then(
                                                    if (addIsFocused) Modifier.basicMarquee() else Modifier
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LauncherHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 58.dp, vertical = 30.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberAsyncImagePainter(model = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 16.dp)
            )
            Text(
                text = "SAMIBOX TV",
                color = CyberCyan,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )
        }

        ClockView()
    }
}

@Composable
fun ClockView() {
    var timeText by remember { mutableStateOf(getFormattedTime()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            timeText = getFormattedTime()
        }
    }
    Text(
        text = timeText,
        color = CyberAmber,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = ShareTechMonoFontFamily
    )
}

fun getFormattedTime(): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("America/Bogota")
    return sdf.format(Date())
}

@Preview(device = Devices.TV_1080p)
@Composable
fun HomeScreenPreview() {
    HomeScreenContent(
        uiState = HomeUiState(),
        onEvent = {}
    )
}
