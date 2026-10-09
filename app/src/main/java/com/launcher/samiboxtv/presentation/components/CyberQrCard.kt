package com.launcher.samiboxtv.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
import com.launcher.samiboxtv.util.QrCodeGenerator

/**
 * Tarjeta Cyberpunk modal para el escaneo de código QR y envío de listas IPTV.
 * Recibe 'closeFocusRequester' para anclar el foco del control remoto inmediatamente
 * sin pérdida de foco ni cierres parpadeantes.
 */
@Composable
fun CyberQrCard(
    serverUrl: String,
    closeFocusRequester: FocusRequester,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qrBitmap = remember(serverUrl) {
        QrCodeGenerator.generateQr(serverUrl, sizePx = 400)
    }
    var closeFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .width(480.dp)
            .background(Color(0xFF090E1B), RoundedCornerShape(14.dp))
            .border(1.5.dp, CyberCyan, RoundedCornerShape(14.dp))
            .padding(24.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Consumir clics dentro de la tarjeta */ }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "📲 ESCANEA CON TU CELULAR",
                color = CyberCyan,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Apunta con la cámara de tu teléfono para abrir el asistente web y enviar la lista sin escribir con el control remoto.",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontFamily = ShareTechMonoFontFamily,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Código QR enmarcado con alto contraste
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap,
                        contentDescription = "Código QR de configuración",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "Generando QR...",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontFamily = ShareTechMonoFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = serverUrl,
                color = CyberAmber,
                fontSize = 13.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tu celular y esta TV deben estar conectados a la misma red Wi-Fi",
                color = Color.Gray,
                fontSize = 10.sp,
                fontFamily = ShareTechMonoFontFamily
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Botón CERRAR con anclaje de foco explícito
            Box(
                modifier = Modifier
                    .focusRequester(closeFocusRequester)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (closeFocused) CyberCyan else Color(0xFF131D33))
                    .border(
                        width = if (closeFocused) 2.dp else 1.dp,
                        color = if (closeFocused) CyberAmber else CyberCyan.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .onFocusChanged { closeFocused = it.isFocused }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClose() }
                    .padding(horizontal = 28.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "LISTO / CERRAR",
                    color = if (closeFocused) Color.Black else CyberCyan,
                    fontSize = 12.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
