package com.launcher.samiboxtv.presentation.components

import android.view.KeyEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.rememberAsyncImagePainter
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.presentation.theme.CyberCard
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberMagenta

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AppCard(
    appItem: AppItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isEditing: Boolean = false,
    onMoveLeft: () -> Unit = {},
    onMoveRight: () -> Unit = {},
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    onExitEdit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val keyModifier = if (isEditing) {
        modifier.onKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown) {
                when (event.key.nativeKeyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> { onMoveLeft(); true }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> { onMoveRight(); true }
                    KeyEvent.KEYCODE_DPAD_UP -> { onMoveUp(); true }
                    KeyEvent.KEYCODE_DPAD_DOWN -> { onMoveDown(); true }
                    KeyEvent.KEYCODE_DPAD_CENTER,
                    KeyEvent.KEYCODE_ENTER,
                    KeyEvent.KEYCODE_BACK -> { onExitEdit(); true }
                    else -> false
                }
            } else if (event.type == KeyEventType.KeyUp) {
                when (event.key.nativeKeyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT,
                    KeyEvent.KEYCODE_DPAD_RIGHT,
                    KeyEvent.KEYCODE_DPAD_UP,
                    KeyEvent.KEYCODE_DPAD_DOWN,
                    KeyEvent.KEYCODE_DPAD_CENTER,
                    KeyEvent.KEYCODE_ENTER,
                    KeyEvent.KEYCODE_BACK -> true
                    else -> false
                }
            } else false
        }
    } else modifier

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Card(
        onClick = {
            if (isEditing) onExitEdit() else onClick()
        },
        onLongClick = onLongClick,
        interactionSource = interactionSource,
        modifier = keyModifier,
        shape = CardDefaults.shape(RoundedCornerShape(8.dp)),
        colors = CardDefaults.colors(
            containerColor = CyberCard,
            focusedContainerColor = Color(0xFF1B2238)
        ),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(2.dp, CyberCyan),
                shape = RoundedCornerShape(8.dp)
            ),
            pressedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(2.dp, CyberMagenta),
                shape = RoundedCornerShape(8.dp)
            )
        )
    ) {
        val editingModifier = if (isEditing) {
            Modifier
                .background(CyberMagenta.copy(alpha = 0.2f))
                .border(2.dp, CyberMagenta, RoundedCornerShape(8.dp))
        } else Modifier

        Box(modifier = Modifier.fillMaxSize().then(editingModifier)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Image(
                    painter = rememberAsyncImagePainter(appItem.icon),
                    contentDescription = appItem.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = appItem.name,
                    color = if (isFocused || isEditing) CyberCyan else Color.White,
                    fontWeight = if (isFocused || isEditing) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.then(if (isFocused || isEditing) Modifier.basicMarquee() else Modifier)
                )
            }

            if (isEditing) {
                Text(
                    text = "◄ ▲ ▼ ►",
                    color = CyberMagenta,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
                Text(
                    text = "OK",
                    color = CyberMagenta,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                )
            }
        }
    }
}
