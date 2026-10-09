package com.launcher.samiboxtv.presentation.components

import android.graphics.Paint
import android.graphics.RectF
import android.view.KeyEvent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.launcher.samiboxtv.R
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.domain.model.VirtualApps
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCard
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Helper para resolver dinámicamente recursos drawable por nombre sin romper
 * la compilación si un archivo es agregado o eliminado en runtime.
 */
private fun getDrawableResId(context: Context, vararg names: String): Int {
    val res = context.resources
    val pkg = context.packageName
    for (name in names) {
        val id = res.getIdentifier(name, "drawable", pkg)
        if (id != 0) return id
    }
    return 0
}

/**
 * Modificador de resplandor neón ultraligero que usa el shadow layer nativo del Canvas.
 * No genera recomposiciones pesadas ni caídas de frames en GPUs Mali/PowerVR (TV Box 1-2GB RAM).
 */
fun Modifier.cyberNeonGlow(
    isFocused: Boolean,
    glowColor: Color = CyberCyan,
    cornerRadius: Dp = 10.dp,
    maxGlowRadius: Dp = 14.dp
): Modifier = if (!isFocused) this else this.drawBehind {
    val radiusPx = cornerRadius.toPx()
    val glowPx = maxGlowRadius.toPx()
    val androidColor = glowColor.toArgb()

    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            isAntiAlias = true
            color = androidColor
            style = Paint.Style.STROKE
            strokeWidth = 2.dp.toPx()
            setShadowLayer(glowPx, 0f, 0f, androidColor)
        }
        val rect = RectF(0f, 0f, size.width, size.height)
        canvas.nativeCanvas.drawRoundRect(rect, radiusPx, radiusPx, paint)
    }
}

/**
 * Tarjeta cibernética responsiva para aplicaciones en NEONOS TV.
 * Soporta carátula/banner panorámico completo (16:9 / Leanback), diseño de apps virtuales
 * (IPTV y Media Hub con iconos y banners nativos) o icono centrado (56dp-64dp).
 * Gestiona clic simple y pulsación sostenida (long-press D-Pad) para desplegar el menú contextual.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CyberAppCard(
    app: AppItem,
    width: Dp,
    height: Dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onFocus: () -> Unit = {},
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    isFavorite: Boolean = app.isFavorite,
    isGhostMode: Boolean = false,
    isEditing: Boolean = false,
    showAppName: Boolean = true,
    onMoveDirection: (Int) -> Unit = {},
    onConfirmMove: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isFocused by remember { mutableStateOf(false) }
    val cardFocusRequester = focusRequester ?: remember(app.packageName) { FocusRequester() }

    var longPressJob by remember { mutableStateOf<Job?>(null) }
    var wasLongPressTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(isGhostMode, app.orderIndex) {
        if (isGhostMode) {
            try {
                cardFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    val scale by animateFloatAsState(
        targetValue = when {
            isGhostMode -> 1.10f
            isFocused -> 1.06f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "cyberCardScale"
    )

    val shape = RoundedCornerShape(10.dp)

    val isLiveTv = app.packageName == VirtualApps.PKG_IPTV ||
            ((app.packageName.contains("iptv", ignoreCase = true) ||
              app.packageName.contains("live", ignoreCase = true) ||
              app.name.contains("TV", ignoreCase = true)) && app.bannerDrawable == null)

    val isMediaHub = app.packageName == VirtualApps.PKG_MEDIA_HUB ||
            ((app.packageName.contains("media", ignoreCase = true) ||
              app.name.contains("MEDIA", ignoreCase = true)) && app.bannerDrawable == null)

    val isSystemNode = isLiveTv || isMediaHub

    val glowColor = when {
        isGhostMode -> CyberMagenta
        isMediaHub -> CyberMagenta
        isLiveTv -> CyberCyan
        isFavorite -> CyberAmber
        else -> CyberCyan
    }

    val borderColor = when {
        isGhostMode -> CyberMagenta
        isFocused -> glowColor
        isEditing -> CyberAmber
        isMediaHub -> CyberMagenta.copy(alpha = 0.35f)
        isLiveTv -> CyberCyan.copy(alpha = 0.35f)
        else -> Color(0xFF19243C)
    }

    val containerBg = when {
        isGhostMode -> Color(0xFF220B2E)
        isFocused -> if (isMediaHub) Color(0xFF240E32) else Color(0xFF14203A)
        isMediaHub -> Color(0xFF14081C)
        isLiveTv -> Color(0xFF070F1E)
        else -> CyberCard
    }

    val hasBanner = app.bannerDrawable != null

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .focusRequester(cardFocusRequester)
            .onPreviewKeyEvent { keyEvent ->
                if (isGhostMode) {
                    val nativeCode = keyEvent.key.nativeKeyCode
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (nativeCode) {
                            KeyEvent.KEYCODE_DPAD_LEFT -> {
                                onMoveDirection(-1)
                                return@onPreviewKeyEvent true
                            }
                            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                onMoveDirection(1)
                                return@onPreviewKeyEvent true
                            }
                            KeyEvent.KEYCODE_DPAD_UP,
                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                return@onPreviewKeyEvent true
                            }
                            KeyEvent.KEYCODE_DPAD_CENTER,
                            KeyEvent.KEYCODE_ENTER,
                            KeyEvent.KEYCODE_NUMPAD_ENTER,
                            KeyEvent.KEYCODE_BACK -> {
                                onConfirmMove()
                                return@onPreviewKeyEvent true
                            }
                        }
                    } else if (keyEvent.type == KeyEventType.KeyUp) {
                        when (nativeCode) {
                            KeyEvent.KEYCODE_DPAD_LEFT,
                            KeyEvent.KEYCODE_DPAD_RIGHT,
                            KeyEvent.KEYCODE_DPAD_UP,
                            KeyEvent.KEYCODE_DPAD_DOWN,
                            KeyEvent.KEYCODE_DPAD_CENTER,
                            KeyEvent.KEYCODE_ENTER,
                            KeyEvent.KEYCODE_NUMPAD_ENTER,
                            KeyEvent.KEYCODE_BACK -> {
                                return@onPreviewKeyEvent true
                            }
                        }
                    }
                }
                false
            }
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    onFocus()
                } else {
                    longPressJob?.cancel()
                    longPressJob = null
                    wasLongPressTriggered = false
                }
            }
            .focusable()
            .scale(scale)
            .cyberNeonGlow(
                isFocused = isFocused || isGhostMode,
                glowColor = glowColor,
                cornerRadius = 10.dp,
                maxGlowRadius = 14.dp
            )
            .clip(shape)
            .background(containerBg)
            .border(
                width = if (isFocused || isGhostMode) 2.dp else 1.dp,
                color = borderColor,
                shape = shape
            )
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (isGhostMode) onConfirmMove() else onClick()
                },
                onLongClick = {
                    if (!isGhostMode) onLongClick()
                }
            )
            .onKeyEvent { keyEvent ->
                val nativeCode = keyEvent.key.nativeKeyCode
                val isConfirmKey = nativeCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                        nativeCode == KeyEvent.KEYCODE_ENTER ||
                        nativeCode == KeyEvent.KEYCODE_NUMPAD_ENTER

                if (keyEvent.type == KeyEventType.KeyDown) {
                    if (isGhostMode) {
                        return@onKeyEvent true
                    }
                    if (nativeCode == KeyEvent.KEYCODE_MENU) {
                        onLongClick()
                        return@onKeyEvent true
                    }

                    if (isConfirmKey) {
                        if (keyEvent.nativeKeyEvent.repeatCount == 0) {
                            wasLongPressTriggered = false
                            longPressJob?.cancel()
                            longPressJob = coroutineScope.launch {
                                delay(450L) // Pulsación sostenida estándar en TV
                                wasLongPressTriggered = true
                                onLongClick()
                            }
                        } else if (keyEvent.nativeKeyEvent.isLongPress && !wasLongPressTriggered) {
                            wasLongPressTriggered = true
                            longPressJob?.cancel()
                            onLongClick()
                        }
                        return@onKeyEvent true
                    }
                } else if (keyEvent.type == KeyEventType.KeyUp) {
                    if (isGhostMode) {
                        return@onKeyEvent true
                    }
                    if (isConfirmKey) {
                        longPressJob?.cancel()
                        longPressJob = null

                        if (wasLongPressTriggered) {
                            wasLongPressTriggered = false
                            return@onKeyEvent true
                        }

                        onClick()
                        return@onKeyEvent true
                    }
                }
                false
            }
    ) {
        if (isLiveTv) {
            val liveTvBannerRes = remember(context) {
                getDrawableResId(context, "banner_live_tv", "banner_iptv", "banner")
            }
            val liveTvIconRes = remember(context) {
                getDrawableResId(context, "icono_live_tv", "iconop_live_tv")
            }

            if (liveTvBannerRes != 0) {
                // [1] BANNER DE LIVE TV CARGADO DESDE DRAWABLE
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(liveTvBannerRes)
                        .crossfade(false)
                        .build(),
                    contentDescription = "Live TV",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Degradado inferior para legibilidad del título
                if (showAppName) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                    startY = 40f
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "LIVE // SEÑAL EN VIVO",
                            color = CyberAmber,
                            fontSize = 11.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "IPTV STREAMING NODE",
                            color = CyberCyan.copy(alpha = 0.85f),
                            fontSize = 9.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                // [2] FALLBACK CYBERPUNK DINÁMICO EN COMPOSE
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF002B49),
                                    Color(0xFF040915)
                                )
                            )
                        )
                        .drawBehind {
                            val strokeColor = CyberCyan.copy(alpha = 0.08f)
                            val step = 20.dp.toPx()
                            var x = 0f
                            while (x < size.width) {
                                drawLine(strokeColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                                x += step
                            }
                            var y = 0f
                            while (y < size.height) {
                                drawLine(strokeColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                                y += step
                            }
                        }
                ) {
                    // Centro: Ícono holográfico de TV con ondas de señal de transmisión radiante
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(if (showAppName) Alignment.TopCenter else Alignment.Center)
                            .padding(top = if (showAppName) 10.dp else 0.dp)
                    ) {
                        Canvas(modifier = Modifier.size(76.dp)) {
                            val waveColor = CyberCyan.copy(alpha = if (isFocused) 0.35f else 0.18f)
                            drawCircle(
                                color = waveColor,
                                radius = size.width * 0.46f,
                                style = Stroke(width = 1.2.dp.toPx())
                            )
                            drawCircle(
                                color = waveColor.copy(alpha = waveColor.alpha * 0.6f),
                                radius = size.width * 0.34f,
                                style = Stroke(width = 1.dp.toPx())
                            )
                            drawCircle(
                                color = waveColor.copy(alpha = waveColor.alpha * 0.3f),
                                radius = size.width * 0.22f,
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }

                        val iconSize = if (!showAppName) 54.dp else 44.dp
                        if (liveTvIconRes != 0) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(liveTvIconRes)
                                    .crossfade(false)
                                    .build(),
                                contentDescription = "Live TV",
                                modifier = Modifier.size(iconSize),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_cyber_iptv),
                                contentDescription = "Live TV",
                                modifier = Modifier.size(iconSize)
                            )
                        }
                    }

                    if (showAppName) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "LIVE // SEÑAL EN VIVO",
                                color = CyberAmber,
                                fontSize = 11.sp,
                                fontFamily = ShareTechMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "IPTV STREAMING NODE",
                                color = CyberCyan.copy(alpha = 0.85f),
                                fontSize = 9.sp,
                                fontFamily = ShareTechMonoFontFamily,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        } else if (isMediaHub) {
            val mediaHubBannerRes = remember(context) {
                getDrawableResId(context, "banner_media", "baner_media")
            }
            val mediaHubIconRes = remember(context) {
                getDrawableResId(context, "icono_media", "iconop_media")
            }

            if (mediaHubBannerRes != 0) {
                // [1] BANNER DE MEDIA HUB CARGADO DESDE DRAWABLE
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(mediaHubBannerRes)
                        .crossfade(false)
                        .build(),
                    contentDescription = "Media Hub",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (showAppName) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                    startY = 40f
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "MEDIA // DISCOS & USB",
                            color = CyberAmber,
                            fontSize = 11.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "STORAGE EXPLORER NODE",
                            color = CyberMagenta.copy(alpha = 0.85f),
                            fontSize = 9.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                // [2] FALLBACK CYBERPUNK DINÁMICO EN COMPOSE
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF350A45),
                                    Color(0xFF0A020F)
                                )
                            )
                        )
                        .drawBehind {
                            val strokeColor = CyberMagenta.copy(alpha = 0.08f)
                            val step = 20.dp.toPx()
                            var x = 0f
                            while (x < size.width) {
                                drawLine(strokeColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                                x += step
                            }
                            var y = 0f
                            while (y < size.height) {
                                drawLine(strokeColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                                y += step
                            }
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(if (showAppName) Alignment.TopCenter else Alignment.Center)
                            .padding(top = if (showAppName) 10.dp else 0.dp)
                    ) {
                        Canvas(modifier = Modifier.size(76.dp)) {
                            val waveColor = CyberMagenta.copy(alpha = if (isFocused) 0.35f else 0.18f)
                            drawCircle(
                                color = waveColor,
                                radius = size.width * 0.46f,
                                style = Stroke(width = 1.2.dp.toPx())
                            )
                            drawCircle(
                                color = waveColor.copy(alpha = waveColor.alpha * 0.6f),
                                radius = size.width * 0.34f,
                                style = Stroke(width = 1.dp.toPx())
                            )
                            drawCircle(
                                color = waveColor.copy(alpha = waveColor.alpha * 0.3f),
                                radius = size.width * 0.22f,
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }

                        val iconSize = if (!showAppName) 54.dp else 44.dp
                        if (mediaHubIconRes != 0) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(mediaHubIconRes)
                                    .crossfade(false)
                                    .build(),
                                contentDescription = "Media Hub",
                                modifier = Modifier.size(iconSize),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_cyber_media_hub),
                                contentDescription = "Media Hub",
                                modifier = Modifier.size(iconSize)
                            )
                        }
                    }

                    if (showAppName) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "MEDIA // DISCOS & USB",
                                color = CyberAmber,
                                fontSize = 11.sp,
                                fontFamily = ShareTechMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "STORAGE EXPLORER NODE",
                                color = CyberMagenta.copy(alpha = 0.85f),
                                fontSize = 9.sp,
                                fontFamily = ShareTechMonoFontFamily,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        } else if (hasBanner) {
            // [1] APP CON BANNER HORIZONTAL (Netflix, YouTube, XMTV, etc.)
            // Llena toda la tarjeta con el arte horizontal panorámico oficial
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(app.bannerDrawable)
                    .crossfade(false)
                    .build(),
                contentDescription = app.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Degradado inferior para legibilidad del título
            if (showAppName) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                startY = 60f
                            )
                        )
                )

                Text(
                    text = app.name.uppercase(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
        } else {
            // [2] APP SIN BANNER: FONDO OSCURO CYBERPUNK CON ICONO CENTRADO (56dp a 64dp) Y NOMBRE
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x99050A14)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val iconModel = remember(app) {
                    ImageRequest.Builder(context)
                        .data(app.iconDrawable)
                        .size(96, 96)
                        .crossfade(false)
                        .build()
                }

                val iconSize = if (!showAppName) {
                    if (height >= 140.dp) 72.dp else 64.dp
                } else {
                    if (height >= 140.dp) 60.dp else 54.dp
                }

                AsyncImage(
                    model = iconModel,
                    contentDescription = app.name,
                    modifier = Modifier.size(iconSize),
                    contentScale = ContentScale.Fit
                )

                if (showAppName) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = app.name.uppercase(),
                        color = if (isFocused) Color.White else Color(0xFFC0D2E8),
                        fontSize = if (height >= 140.dp) 11.sp else 10.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Badge superior izquierdo para Favoritos
        if (isFavorite) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberAmber.copy(alpha = 0.35f))
                    .border(0.5.dp, CyberAmber, RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "★",
                    color = CyberAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Badge superior derecho para Apps Virtuales / Sistema
        if (isSystemNode) {
            val badgeColor = if (isLiveTv) CyberCyan else CyberMagenta
            val dotColor = if (isLiveTv) Color(0xFFFF0055) else CyberMagenta
            val badgeText = if (isLiveTv) "LIVE TV" else "USB HUB"

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(0.8.dp, badgeColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 8.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Overlay cuando está en modo reordenamiento fantasma
        if (isGhostMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .border(1.dp, CyberMagenta, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "◄ MOVER ►",
                    color = CyberMagenta,
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
