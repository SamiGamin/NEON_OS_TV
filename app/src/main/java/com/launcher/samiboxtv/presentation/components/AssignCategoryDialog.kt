package com.launcher.samiboxtv.presentation.components

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Text
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
import com.launcher.samiboxtv.util.CategoryHelper

@Composable
fun AssignCategoryDialog(
    app: AppItem,
    categories: List<String>,
    currentCategory: String,
    onDismiss: () -> Unit,
    onSelectCategory: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(480.dp)
                .background(Color(0xFF090E1B), RoundedCornerShape(14.dp))
                .border(1.5.dp, CyberCyan, RoundedCornerShape(14.dp))
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "MOVER: ${app.name}",
                    color = CyberCyan,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Selecciona la fila donde deseas mostrar esta aplicación:",
                    color = Color(0xFFA6C5E2),
                    fontSize = 11.sp,
                    fontFamily = ShareTechMonoFontFamily
                )
                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isCurrent = cat.equals(currentCategory, ignoreCase = true)
                        var isFocused by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isFocused) Color(0xFF1B2B4C) else Color(0xFF121B30))
                                .border(
                                    width = if (isFocused) 1.5.dp else 1.dp,
                                    color = if (isFocused) CyberAmber else (if (isCurrent) CyberCyan else Color.Transparent),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .onFocusChanged { isFocused = it.isFocused }
                                .onKeyEvent { keyEvent ->
                                    if (keyEvent.type == KeyEventType.KeyDown &&
                                        (keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                                                keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER ||
                                                keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
                                    ) {
                                        onSelectCategory(cat)
                                        true
                                    } else false
                                }
                                .focusable()
                                .clickable { onSelectCategory(cat) }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = CategoryHelper.getCategoryIcon(cat),
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = cat,
                                        color = if (isCurrent) CyberCyan else Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = ShareTechMonoFontFamily,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                if (isCurrent) {
                                    Text(
                                        text = "✓ ACTUAL",
                                        color = CyberAmber,
                                        fontSize = 10.sp,
                                        fontFamily = ShareTechMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    var closeFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (closeFocused) Color(0xFF1F2B48) else Color(0xFF141C30))
                            .border(
                                1.dp,
                                if (closeFocused) CyberAmber else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .onFocusChanged { closeFocused = it.isFocused }
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown &&
                                    (keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                                            keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER ||
                                            keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
                                ) {
                                    onDismiss()
                                    true
                                } else false
                            }
                            .focusable()
                            .clickable { onDismiss() }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "CERRAR",
                            color = if (closeFocused) CyberAmber else CyberGrey,
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
