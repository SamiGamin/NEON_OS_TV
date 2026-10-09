package com.launcher.samiboxtv.presentation.iptv

import android.app.Activity
import android.content.ContextWrapper
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.launcher.samiboxtv.domain.model.IptvChannel
import com.launcher.samiboxtv.presentation.home.HomeUiEvent
import com.launcher.samiboxtv.presentation.home.HomeUiState
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
import com.launcher.samiboxtv.util.TvRemoteKeyCodes
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun IptvPlayerScreen(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit
) {
    val context = LocalContext.current

    // Obtener la Activity anfitriona para controlar el bloqueo de pantalla del sistema
    val activity = remember(context) {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return@remember ctx
            ctx = ctx.baseContext
        }
        null
    }

    val playerFocusRequester = remember { FocusRequester() }
    val drawerListState = rememberLazyListState()

    // Instancia única y local de ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    var isBuffering by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showOsd by remember { mutableStateOf(true) }
    var favoriteFeedbackText by remember { mutableStateOf<String?>(null) }
    var previousFavoriteUrls by remember { mutableStateOf(uiState.favoriteIptvChannelUrls) }

    // Notificación en pantalla al cambiar el estado de favorito del canal actual
    LaunchedEffect(uiState.favoriteIptvChannelUrls) {
        val currentChannel = uiState.currentIptvChannel
        if (currentChannel != null && previousFavoriteUrls != uiState.favoriteIptvChannelUrls) {
            val isNowFav = uiState.favoriteIptvChannelUrls.contains(currentChannel.streamUrl)
            val wasFav = previousFavoriteUrls.contains(currentChannel.streamUrl)
            if (isNowFav != wasFav) {
                favoriteFeedbackText = if (isNowFav) "★ CANAL AÑADIDO A FAVORITOS" else "☆ CANAL ELIMINADO DE FAVORITOS"
                showOsd = true
                delay(2500)
                favoriteFeedbackText = null
            }
        }
        previousFavoriteUrls = uiState.favoriteIptvChannelUrls
    }

    // Ciclo de vida y gestión anti-suspensión (Screensaver Lock)
    DisposableEffect(exoPlayer, activity) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = (playbackState == Player.STATE_BUFFERING)
                if (playbackState == Player.STATE_READY) {
                    errorMessage = null
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                // Si el canal está reproduciendo video activamente, bloquear el protector de pantalla
                if (isPlaying) {
                    activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                errorMessage = "Señal no disponible (${error.errorCodeName})"
                // Si la señal cayó, permitir suspensión
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            // AL SALIR DEL REPRODUCTOR: Limpiar la bandera inmediatamente para que el TV suspenda normalmente en el launcher
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    // Pausar reproducción cuando la actividad o pantalla pasa a segundo plano
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                exoPlayer.pause()
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Sintonización sin recrear la superficie ni el reproductor
    LaunchedEffect(uiState.currentIptvChannel?.streamUrl) {
        val streamUrl = uiState.currentIptvChannel?.streamUrl
        if (!streamUrl.isNullOrBlank()) {
            isBuffering = true
            errorMessage = null
            showOsd = true
            exoPlayer.stop()
            val mediaItem = MediaItem.fromUri(streamUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    // Sincronizar HUD OSD con el estado global (KEY_INFO, zapping o teclas remotas)
    LaunchedEffect(uiState.isIptvOsdVisible) {
        showOsd = uiState.isIptvOsdVisible
    }

    // Auto-ocultado del HUD tras 4 segundos
    LaunchedEffect(uiState.currentIptvChannel, showOsd) {
        if (showOsd) {
            delay(4000)
            showOsd = false
        }
    }

    // Control del botón Atrás (Back)
    BackHandler(enabled = true) {
        if (uiState.isIptvChannelListOpen) {
            onEvent(HomeUiEvent.ToggleChannelList)
        } else {
            exoPlayer.stop()
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            onEvent(HomeUiEvent.CloseLiveTv)
        }
    }

    // Devolver el foco al reproductor principal cuando se cierra la guía lateral
    LaunchedEffect(uiState.isIptvChannelListOpen) {
        if (!uiState.isIptvChannelListOpen) {
            delay(50)
            try {
                playerFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(playerFocusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false
                val keyCode = keyEvent.key.nativeKeyCode
                val scanCode = keyEvent.nativeKeyEvent.scanCode

                if (!uiState.isIptvChannelListOpen) {
                    when {
                        keyCode == KeyEvent.KEYCODE_CHANNEL_UP ||
                        keyCode == KeyEvent.KEYCODE_DPAD_UP -> {
                            showOsd = true
                            onEvent(HomeUiEvent.NextChannel)
                            true
                        }
                        keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN ||
                        keyCode == KeyEvent.KEYCODE_DPAD_DOWN -> {
                            showOsd = true
                            onEvent(HomeUiEvent.PreviousChannel)
                            true
                        }
                        keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                        keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                        keyCode == KeyEvent.KEYCODE_ENTER ||
                        keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                            onEvent(HomeUiEvent.ToggleChannelList)
                            true
                        }
                        TvRemoteKeyCodes.isFavoriteKey(keyCode, scanCode) ||
                        keyCode == KeyEvent.KEYCODE_MENU -> {
                            onEvent(HomeUiEvent.ToggleCurrentIptvFavorite)
                            showOsd = true
                            true
                        }
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            showOsd = !showOsd
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!uiState.isIptvChannelListOpen) {
                    onEvent(HomeUiEvent.ToggleChannelList)
                }
            }
    ) {
        // 1. Vista de video ExoPlayer en pantalla completa con keepScreenOn = true
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    keepScreenOn = true // Mantiene encendido el panel a nivel de Vista
                    isFocusable = false
                    isFocusableInTouchMode = false
                    descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .focusable(false)
        )

        // 2. Indicador sutil de Buffering en la esquina superior derecha
        if (isBuffering && !showOsd) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(28.dp)
                    .background(Color(0xD0070B16), RoundedCornerShape(8.dp))
                    .border(1.dp, CyberAmber, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "⚡ SINTONIZANDO...",
                    color = CyberAmber,
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 3. Alerta HUD superior para Favoritos
        AnimatedVisibility(
            visible = favoriteFeedbackText != null,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 28.dp)
                    .background(Color(0xF0070B16), RoundedCornerShape(8.dp))
                    .border(1.5.dp, CyberAmber, RoundedCornerShape(8.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = favoriteFeedbackText ?: "",
                    color = CyberAmber,
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 4. Error en pantalla si la señal falló
        if (errorMessage != null && !isBuffering) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color(0xF0080D1A), RoundedCornerShape(12.dp))
                    .border(1.5.dp, CyberMagenta, RoundedCornerShape(12.dp))
                    .padding(horizontal = 28.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⚠ SEÑAL NO DISPONIBLE",
                        color = CyberMagenta,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage ?: "No se pudo conectar con el stream",
                        color = Color.White.copy(alpha = 0.8f),
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Presiona ▲ / ▼ para saltar a otro canal",
                        color = CyberCyan,
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 5. OSD HUD Inferior Cyberpunk (Auto-ocultado)
        AnimatedVisibility(
            visible = showOsd && !uiState.isIptvChannelListOpen,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val channel = uiState.currentIptvChannel
            val isCurrentFavorite = channel?.streamUrl?.let { uiState.favoriteIptvChannelUrls.contains(it) } ?: false

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp, vertical = 24.dp)
                    .background(Color(0xF0070B16), RoundedCornerShape(14.dp))
                    .border(1.5.dp, CyberCyan.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 22.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF131D33), RoundedCornerShape(8.dp))
                            .border(1.dp, CyberAmber, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "#%03d".format(uiState.currentIptvIndex + 1),
                            color = CyberAmber,
                            fontSize = 15.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(Color(0xFF0F172B), RoundedCornerShape(8.dp))
                            .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!channel?.logoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = channel.logoUrl,
                                contentDescription = channel.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                        } else {
                            Text(text = "📺", fontSize = 20.sp)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = channel?.name ?: "CANAL IPTV",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (isCurrentFavorite) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "★",
                                    color = CyberAmber,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${channel?.groupTitle ?: "GENERAL"}  •  LIVE STREAM",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontFamily = ShareTechMonoFontFamily
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isBuffering) "⚡ BUFFERING..." else "● SEÑAL EN VIVO",
                            color = if (isBuffering) CyberAmber else Color(0xFF00E676),
                            fontSize = 11.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "▲/▼ Zapping  •  OK/◄ Guía  •  [🟡/FAV/MENU] ★ Favorito",
                            color = CyberGrey,
                            fontSize = 10.sp,
                            fontFamily = ShareTechMonoFontFamily
                        )
                    }
                }
            }
        }

        // 6. Guía lateral de Canales (Channel Drawer)
        AnimatedVisibility(
            visible = uiState.isIptvChannelListOpen,
            enter = slideInHorizontally { -it } + fadeIn(),
            exit = slideOutHorizontally { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            ChannelGuideDrawer(
                uiState = uiState,
                lazyListState = drawerListState,
                onEvent = onEvent,
                onClose = { onEvent(HomeUiEvent.ToggleChannelList) }
            )
        }
    }
}

@Composable
private fun ChannelGuideDrawer(
    uiState: HomeUiState,
    lazyListState: androidx.compose.foundation.lazy.LazyListState,
    onEvent: (HomeUiEvent) -> Unit,
    onClose: () -> Unit
) {
    var expandedCategory by remember {
        mutableStateOf(uiState.currentIptvChannel?.groupTitle?.trim()?.uppercase())
    }

    val groupedCategories = remember(uiState.iptvChannels, uiState.favoriteIptvChannelUrls) {
        val list = mutableListOf<Pair<String, List<IptvChannel>>>()

        val favChannels = uiState.iptvChannels.filter {
            uiState.favoriteIptvChannelUrls.contains(it.streamUrl)
        }
        list.add("★ FAVORITOS" to favChannels)

        val groups = uiState.iptvChannels
            .map { it.groupTitle.trim().uppercase().ifEmpty { "GENERAL" } }
            .filter { it != "★ FAVORITOS" }
            .distinct()

        for (group in groups) {
            val channelsInGroup = uiState.iptvChannels.filter {
                val chGroup = it.groupTitle.trim().uppercase().ifEmpty { "GENERAL" }
                chGroup.equals(group, ignoreCase = true)
            }
            list.add(group to channelsInGroup)
        }
        list
    }

    val closeButtonFocusRequester = remember { FocusRequester() }
    var isCloseFocused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        try {
            closeButtonFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    LaunchedEffect(uiState.currentIptvChannel) {
        val currentCat = uiState.currentIptvChannel?.groupTitle?.trim()?.uppercase()
        if (!currentCat.isNullOrBlank() && expandedCategory == null) {
            expandedCategory = currentCat
        }
    }

    LaunchedEffect(expandedCategory) {
        if (expandedCategory != null) {
            var itemIdx = 0
            for ((catName, channels) in groupedCategories) {
                if (catName.equals(expandedCategory, ignoreCase = true)) {
                    val currentChannel = uiState.currentIptvChannel
                    val chIdx = if (currentChannel != null) {
                        channels.indexOfFirst { it.id == currentChannel.id }
                    } else -1

                    val target = if (chIdx >= 0) {
                        (itemIdx + 1 + chIdx - 2).coerceAtLeast(0)
                    } else {
                        itemIdx.coerceAtLeast(0)
                    }
                    delay(50)
                    try {
                        lazyListState.scrollToItem(target)
                    } catch (_: Exception) {}
                    break
                }
                itemIdx += 1
            }
        }
    }

    Box(
        modifier = Modifier
            .width(420.dp)
            .fillMaxHeight()
            .background(Color(0xF5060A14))
            .border(width = 1.5.dp, color = CyberCyan.copy(alpha = 0.5f))
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    onClose()
                    true
                } else false
            }
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GUÍA DE CANALES",
                        color = CyberCyan,
                        fontSize = 16.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.iptvChannels.size} disponibles // OK sintonizar // FAV/MENU fav",
                        color = CyberGrey,
                        fontSize = 10.sp,
                        fontFamily = ShareTechMonoFontFamily
                    )
                }

                Box(
                    modifier = Modifier
                        .focusRequester(closeButtonFocusRequester)
                        .onFocusChanged { isCloseFocused = it.isFocused }
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isCloseFocused) CyberCyan else Color(0xFF131D33))
                        .border(
                            1.dp,
                            if (isCloseFocused) CyberAmber else CyberCyan.copy(alpha = 0.3f),
                            RoundedCornerShape(6.dp)
                        )
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.type == KeyEventType.KeyDown) {
                                when (keyEvent.key.nativeKeyCode) {
                                    KeyEvent.KEYCODE_DPAD_RIGHT,
                                    KeyEvent.KEYCODE_DPAD_CENTER,
                                    KeyEvent.KEYCODE_ENTER,
                                    KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                        onClose()
                                        true
                                    }
                                    else -> false
                                }
                            } else false
                        }
                        .focusable()
                        .clickable { onClose() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "◀ CERRAR",
                        color = if (isCloseFocused) Color.Black else CyberCyan,
                        fontSize = 10.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedCategories.forEach { (categoryName, channels) ->
                    val isExpanded = expandedCategory.equals(categoryName, ignoreCase = true)
                    val isSpecial = categoryName == "★ FAVORITOS"

                    item(key = "cat_$categoryName") {
                        CategoryHeaderItem(
                            categoryName = categoryName,
                            channelCount = channels.size,
                            isExpanded = isExpanded,
                            isSpecial = isSpecial,
                            onToggleExpand = {
                                expandedCategory = if (isExpanded) null else categoryName
                            },
                            onCloseDrawer = onClose
                        )
                    }

                    if (isExpanded) {
                        if (channels.isEmpty() && isSpecial) {
                            item(key = "empty_favorites") {
                                FavoritesEmptyItem()
                            }
                        } else {
                            items(
                                items = channels,
                                key = { "ch_${categoryName}_${it.id}" }
                            ) { channel ->
                                val isCurrent = uiState.currentIptvChannel?.id == channel.id
                                val isFavorite = uiState.favoriteIptvChannelUrls.contains(channel.streamUrl)
                                val originalIndex = uiState.iptvChannels.indexOf(channel)

                                ChannelDrawerItem(
                                    channel = channel,
                                    displayNumber = if (originalIndex != -1) originalIndex + 1 else 1,
                                    isCurrent = isCurrent,
                                    isFavorite = isFavorite,
                                    onClick = {
                                        if (originalIndex != -1) {
                                            onEvent(HomeUiEvent.SelectChannel(originalIndex))
                                        }
                                        onClose()
                                    },
                                    onToggleFavorite = {
                                        onEvent(HomeUiEvent.ToggleIptvFavorite(channel))
                                    },
                                    onCloseDrawer = onClose
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeaderItem(
    categoryName: String,
    channelCount: Int,
    isExpanded: Boolean,
    isSpecial: Boolean = false,
    onToggleExpand: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val accentColor = if (isSpecial) CyberAmber else CyberCyan

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isFocused -> Color(0xFF1B2848)
                    isExpanded -> Color(0xFF101C36)
                    else -> Color(0xFF0C1324)
                }
            )
            .border(
                width = if (isFocused) 1.5.dp else if (isExpanded) 1.dp else 0.5.dp,
                color = when {
                    isFocused -> CyberAmber
                    isExpanded -> accentColor
                    isSpecial -> CyberAmber.copy(alpha = 0.5f)
                    else -> CyberCyan.copy(alpha = 0.25f)
                },
                shape = RoundedCornerShape(8.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key.nativeKeyCode) {
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            onCloseDrawer()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_CENTER,
                        KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                            onToggleExpand()
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .focusable()
            .clickable { onToggleExpand() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isExpanded) "▼" else "▶",
                    color = if (isFocused) CyberAmber else accentColor,
                    fontSize = 11.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = categoryName.uppercase(),
                    color = when {
                        isFocused -> CyberAmber
                        isExpanded -> Color.White
                        isSpecial -> CyberAmber
                        else -> Color.White.copy(alpha = 0.9f)
                    },
                    fontSize = 12.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "($channelCount)",
                color = if (isFocused) CyberAmber.copy(alpha = 0.8f) else CyberGrey,
                fontSize = 11.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun FavoritesEmptyItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x300C1324), RoundedCornerShape(8.dp))
            .border(0.5.dp, CyberAmber.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "★ SIN FAVORITOS AÚN",
                color = CyberAmber,
                fontSize = 12.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pulsa FAV, MENU o mantén presionado OK sobre cualquier canal para guardarlo aquí.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontFamily = ShareTechMonoFontFamily,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChannelDrawerItem(
    channel: IptvChannel,
    displayNumber: Int,
    isCurrent: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isFocused -> Color(0xFF1B2848)
                    isCurrent -> Color(0xFF0F1C33)
                    else -> Color(0x600C1324)
                }
            )
            .border(
                width = if (isFocused || isCurrent) 1.dp else 0.5.dp,
                color = when {
                    isFocused -> CyberAmber
                    isCurrent -> CyberCyan
                    isFavorite -> CyberAmber.copy(alpha = 0.5f)
                    else -> CyberCyan.copy(alpha = 0.15f)
                },
                shape = RoundedCornerShape(8.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    val keyCode = keyEvent.key.nativeKeyCode
                    val scanCode = keyEvent.nativeKeyEvent.scanCode
                    when {
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            onCloseDrawer()
                            true
                        }
                        TvRemoteKeyCodes.isFavoriteKey(keyCode, scanCode) ||
                        keyCode == KeyEvent.KEYCODE_MENU -> {
                            onToggleFavorite()
                            true
                        }
                        keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                        keyCode == KeyEvent.KEYCODE_ENTER ||
                        keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                            if (keyEvent.nativeKeyEvent.isLongPress) {
                                onToggleFavorite()
                                true
                            } else {
                                onClick()
                                true
                            }
                        }
                        else -> false
                    }
                } else false
            }
            .focusable()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onToggleFavorite
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "%03d".format(displayNumber),
                color = if (isCurrent) CyberCyan else CyberGrey,
                fontSize = 11.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF070B16), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!channel.logoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = channel.logoUrl,
                        contentDescription = channel.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                } else {
                    Text(text = "📺", fontSize = 14.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.name,
                    color = if (isFocused) CyberAmber else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = channel.groupTitle.uppercase(),
                    color = CyberGrey,
                    fontSize = 9.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isFavorite) {
                Text(
                    text = "★",
                    color = CyberAmber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .background(CyberCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .border(0.5.dp, CyberCyan, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "● EN VIVO",
                        color = CyberCyan,
                        fontSize = 8.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}