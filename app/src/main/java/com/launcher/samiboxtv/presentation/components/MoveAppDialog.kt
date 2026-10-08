package com.launcher.samiboxtv.presentation.components

import android.view.KeyEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.rememberAsyncImagePainter
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
import kotlinx.coroutines.delay

/**
 * Diálogo interactivo optimizado para Android TV (D-Pad y control remoto).
 * Utiliza componentes nativos de Jetpack Compose for TV (androidx.tv.material3.Button)
 * garantizando foco automático, navegación D-Pad bidireccional y cero pérdida de foco.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MoveAppDialog(
    appItem: AppItem,
    categoryName: String,
    currentPosition: Int = 1,
    totalPositions: Int = 1,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onDismiss: () -> Unit
) {
    val initialFocus = remember { FocusRequester() }

    // Asegurar foco automático al abrirse el diálogo en Android TV con reintentos
    LaunchedEffect(Unit) {
        for (i in 1..4) {
            delay(100)
            try {
                initialFocus.requestFocus()
                break
            } catch (_: Exception) {}
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(520.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF090E1B))
                .border(2.dp, CyberAmber, RoundedCornerShape(14.dp))
                .padding(24.dp)
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown &&
                        keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_BACK
                    ) {
                        onDismiss()
                        true
                    } else false
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Cabecera del diálogo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⇄ REORGANIZAR POSICIÓN",
                        color = CyberAmber,
                        fontSize = 15.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberCyan.copy(alpha = 0.2f))
                            .border(1.dp, CyberCyan, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = categoryName.uppercase(),
                            color = CyberCyan,
                            fontSize = 10.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tarjeta informativa de la aplicación
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF10182C))
                        .border(1.dp, CyberAmber.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconPainter = rememberAsyncImagePainter(
                        model = appItem.bannerDrawable ?: appItem.iconDrawable
                    )
                    Image(
                        painter = iconPainter,
                        contentDescription = appItem.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appItem.name,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Moviendo en la fila de $categoryName",
                            color = CyberGrey,
                            fontSize = 10.sp,
                            fontFamily = ShareTechMonoFontFamily
                        )
                    }

                    // Indicador de posición visual en vivo
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF18223B))
                            .border(1.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "POS: $currentPosition / $totalPositions",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Navega con el D-Pad y pulsa [OK] para mover de lugar:",
                    color = Color(0xFFA6C5E2),
                    fontSize = 11.sp,
                    fontFamily = ShareTechMonoFontFamily
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fila 1: Botones principales de desplazamiento izquierda/derecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Mover Izquierda (recibe foco inicial)
                    Button(
                        onClick = onMoveLeft,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(initialFocus),
                        colors = ButtonDefaults.colors(
                            containerColor = Color(0xFF142442),
                            focusedContainerColor = CyberAmber,
                            focusedContentColor = Color.Black
                        ),
                        shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "◄ MOVER IZQUIERDA",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botón Mover Derecha
                    Button(
                        onClick = onMoveRight,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.colors(
                            containerColor = Color(0xFF142442),
                            focusedContainerColor = CyberAmber,
                            focusedContentColor = Color.Black
                        ),
                        shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "MOVER DERECHA ►",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fila 2: Botón de confirmación y salida
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.colors(
                        containerColor = Color(0xFF101B33),
                        focusedContainerColor = CyberCyan,
                        focusedContentColor = Color.Black
                    ),
                    shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "✓ LISTO (CONFIRMAR Y SALIR)",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
