package com.launcher.samiboxtv.presentation.settings.components

import android.view.KeyEvent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.launcher.samiboxtv.domain.model.SettingsSection
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily

@Composable
fun SettingsMenuTabItem(
    section: SettingsSection,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(8.dp)

    val borderColor = when {
        isFocused -> CyberAmber
        isSelected -> CyberCyan
        else -> Color.Transparent
    }
    val containerBg = when {
        isFocused -> Color(0xFF1B2B4C)
        isSelected -> Color(0xFF101B33)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerBg)
            .border(width = if (isFocused || isSelected) 1.5.dp else 0.dp, color = borderColor, shape = shape)
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) onSelect()
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onSelect() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(id = section.iconRes),
                contentDescription = section.title,
                colorFilter = ColorFilter.tint(
                    when {
                        isFocused -> CyberAmber
                        isSelected -> CyberCyan
                        else -> CyberGrey
                    }
                ),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = section.title,
                color = when {
                    isFocused -> CyberAmber
                    isSelected -> Color.White
                    else -> Color(0xFFA6C5E2)
                },
                fontSize = 11.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun SettingsActionItem(
    title: String,
    subtitle: String,
    isHighlighted: Boolean,
    enabled: Boolean = true,
    @DrawableRes iconRes: Int? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(8.dp)

    val borderColor = when {
        !enabled -> CyberCyan.copy(alpha = 0.15f)
        isFocused -> CyberAmber
        isHighlighted -> CyberCyan
        else -> CyberCyan.copy(alpha = 0.25f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isFocused) Color(0xFF1B2848) else Color(0xFF0F172B))
            .border(width = if (isFocused) 2.dp else 1.dp, color = borderColor, shape = shape)
            .onFocusChanged { isFocused = it.isFocused }
            .tvClickable { if (enabled) onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (iconRes != null) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        when {
                            !enabled -> Color.Gray
                            isFocused -> CyberAmber
                            isHighlighted -> CyberCyan
                            else -> Color.White
                        }
                    ),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = when {
                        !enabled -> Color.Gray
                        isFocused -> CyberAmber
                        isHighlighted -> CyberCyan
                        else -> Color.White
                    },
                    fontSize = 12.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    val isError = subtitle.startsWith("Error", ignoreCase = true)
                    Text(
                        text = subtitle,
                        color = when {
                            isError -> Color(0xFFFF5252)
                            isHighlighted -> CyberCyan.copy(alpha = 0.9f)
                            else -> Color(0xFF7E9BB8)
                        },
                        fontSize = 9.sp,
                        fontFamily = ShareTechMonoFontFamily
                    )
                }
            }
        }
    }
}

fun Modifier.tvClickable(onClick: () -> Unit): Modifier = composed {
    this
        .onKeyEvent { keyEvent ->
            if (keyEvent.type == KeyEventType.KeyUp &&
                (keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                        keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER ||
                        keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
            ) {
                onClick()
                true
            } else false
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
}