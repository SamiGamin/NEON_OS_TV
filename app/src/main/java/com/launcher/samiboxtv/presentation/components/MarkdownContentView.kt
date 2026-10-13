package com.launcher.samiboxtv.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.CyberWhite
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily

sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data class BulletItem(val indentLevel: Int, val text: String) : MarkdownBlock()
    data class TaskItem(val indentLevel: Int, val checked: Boolean, val text: String) : MarkdownBlock()
    data class OrderedItem(val indentLevel: Int, val number: String, val text: String) : MarkdownBlock()
    data class Blockquote(val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    object Divider : MarkdownBlock()
    object EmptyLine : MarkdownBlock()
}

/**
 * Parsea un texto en formato Markdown plano a una lista de bloques estructurados.
 */
fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    if (markdown.isBlank()) return emptyList()
    val lines = markdown.lines()
    val blocks = mutableListOf<MarkdownBlock>()

    var inCodeBlock = false
    var codeLanguage = ""
    val codeLines = mutableListOf<String>()

    var i = 0
    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        // Bloque de código multi-línea (```)
        if (trimmed.startsWith("```")) {
            if (inCodeBlock) {
                blocks.add(MarkdownBlock.CodeBlock(codeLanguage, codeLines.joinToString("\n")))
                codeLines.clear()
                inCodeBlock = false
            } else {
                inCodeBlock = true
                codeLanguage = trimmed.removePrefix("```").trim()
            }
            i++
            continue
        }

        if (inCodeBlock) {
            codeLines.add(line)
            i++
            continue
        }

        // Línea vacía
        if (trimmed.isEmpty()) {
            if (blocks.lastOrNull() !is MarkdownBlock.EmptyLine) {
                blocks.add(MarkdownBlock.EmptyLine)
            }
            i++
            continue
        }

        // Separador horizontal (---, ***, ___)
        if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
            blocks.add(MarkdownBlock.Divider)
            i++
            continue
        }

        // Encabezados (# H1, ## H2, etc.)
        if (trimmed.startsWith("#")) {
            val hashCount = trimmed.takeWhile { it == '#' }.length
            if (hashCount in 1..6 && trimmed.getOrNull(hashCount) == ' ') {
                val headerText = trimmed.drop(hashCount + 1).trim()
                blocks.add(MarkdownBlock.Header(hashCount, headerText))
                i++
                continue
            }
        }

        // Citas / Blockquotes (> Quote) agrupadas consecutivas
        if (trimmed.startsWith(">")) {
            val quoteLines = mutableListOf<String>()
            while (i < lines.size && lines[i].trim().startsWith(">")) {
                quoteLines.add(lines[i].trim().removePrefix(">").trim())
                i++
            }
            blocks.add(MarkdownBlock.Blockquote(quoteLines.joinToString("\n")))
            continue
        }

        // Elementos de lista de tareas (- [ ] o - [x] o * [ ])
        val taskRegex = Regex("""^(\s*)([-*+])\s+\[([ xX])\]\s+(.*)$""")
        val taskMatch = taskRegex.matchEntire(line)
        if (taskMatch != null) {
            val indentSpaces = taskMatch.groupValues[1].length
            val indentLevel = (indentSpaces / 2).coerceAtMost(4)
            val isChecked = taskMatch.groupValues[3].equals("x", ignoreCase = true)
            val itemText = taskMatch.groupValues[4]
            blocks.add(MarkdownBlock.TaskItem(indentLevel, isChecked, itemText))
            i++
            continue
        }

        // Elementos de lista de viñetas (- item, * item, + item)
        val bulletRegex = Regex("""^(\s*)([-*+])\s+(.*)$""")
        val bulletMatch = bulletRegex.matchEntire(line)
        if (bulletMatch != null) {
            val indentSpaces = bulletMatch.groupValues[1].length
            val indentLevel = (indentSpaces / 2).coerceAtMost(4)
            val itemText = bulletMatch.groupValues[3]
            blocks.add(MarkdownBlock.BulletItem(indentLevel, itemText))
            i++
            continue
        }

        // Elementos de lista numerada (1. item, 2. item)
        val orderedRegex = Regex("""^(\s*)(\d+)\.\s+(.*)$""")
        val orderedMatch = orderedRegex.matchEntire(line)
        if (orderedMatch != null) {
            val indentSpaces = orderedMatch.groupValues[1].length
            val indentLevel = (indentSpaces / 2).coerceAtMost(4)
            val number = orderedMatch.groupValues[2]
            val itemText = orderedMatch.groupValues[3]
            blocks.add(MarkdownBlock.OrderedItem(indentLevel, number, itemText))
            i++
            continue
        }

        // Párrafo de texto normal
        blocks.add(MarkdownBlock.Paragraph(line.trim()))
        i++
    }

    if (inCodeBlock && codeLines.isNotEmpty()) {
        blocks.add(MarkdownBlock.CodeBlock(codeLanguage, codeLines.joinToString("\n")))
    }

    return blocks
}

/**
 * Analiza el formato inline (negrita, cursiva, código, enlaces, tachado) dentro de una línea.
 */
fun buildMarkdownAnnotatedString(
    text: String,
    techFont: FontFamily = ShareTechMonoFontFamily
): AnnotatedString {
    return buildAnnotatedString {
        appendInlineMarkdown(text, techFont)
    }
}

private fun AnnotatedString.Builder.appendInlineMarkdown(
    text: String,
    techFont: FontFamily,
    isBold: Boolean = false,
    isItalic: Boolean = false
) {
    val inlinePattern = Regex(
        """(`[^`]+`)|(\*\*[^\s*](?:.*?[^\s*])?\*\*)|(__[^\s_](?:.*?[^\s_])?__)|(~~[^\s~](?:.*?[^\s~])?~~)|(\[[^\]]+\]\([^)]+\))|(https?://[^\s)\]>,]+)|(\*[^\s*](?:.*?[^\s*])?\*)|(_[^\s_](?:.*?[^\s_])?_)"""
    )
    var currentIndex = 0
    for (match in inlinePattern.findAll(text)) {
        if (match.range.first > currentIndex) {
            append(text.substring(currentIndex, match.range.first))
        }
        val matchValue = match.value
        when {
            // Código inline `code`
            matchValue.startsWith("`") && matchValue.endsWith("`") -> {
                val code = matchValue.substring(1, matchValue.length - 1)
                withStyle(
                    SpanStyle(
                        color = CyberCyan,
                        background = Color(0x3300F0FF),
                        fontFamily = techFont,
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append(" $code ")
                }
            }

            // Negrita **bold** o __bold__
            (matchValue.startsWith("**") && matchValue.endsWith("**")) ||
            (matchValue.startsWith("__") && matchValue.endsWith("__")) -> {
                val inner = matchValue.substring(2, matchValue.length - 2)
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = CyberWhite
                    )
                ) {
                    appendInlineMarkdown(inner, techFont, isBold = true, isItalic = isItalic)
                }
            }

            // Tachado ~~strikethrough~~
            matchValue.startsWith("~~") && matchValue.endsWith("~~") -> {
                val inner = matchValue.substring(2, matchValue.length - 2)
                withStyle(
                    SpanStyle(
                        textDecoration = TextDecoration.LineThrough,
                        color = CyberGrey
                    )
                ) {
                    appendInlineMarkdown(inner, techFont, isBold = isBold, isItalic = isItalic)
                }
            }

            // Enlaces [texto](url)
            matchValue.startsWith("[") && matchValue.contains("](") && matchValue.endsWith(")") -> {
                val closeBracket = matchValue.indexOf("](")
                val linkText = matchValue.substring(1, closeBracket)
                withStyle(
                    SpanStyle(
                        color = CyberCyan,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    append(linkText)
                }
            }

            // Enlace crudo https://...
            matchValue.startsWith("http://") || matchValue.startsWith("https://") -> {
                withStyle(
                    SpanStyle(
                        color = CyberCyan,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(matchValue)
                }
            }

            // Cursiva *italic* o _italic_
            (matchValue.startsWith("*") && matchValue.endsWith("*")) ||
            (matchValue.startsWith("_") && matchValue.endsWith("_")) -> {
                val inner = matchValue.substring(1, matchValue.length - 1)
                withStyle(
                    SpanStyle(
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFFE2E8F0)
                    )
                ) {
                    appendInlineMarkdown(inner, techFont, isBold = isBold, isItalic = true)
                }
            }

            else -> {
                append(matchValue)
            }
        }
        currentIndex = match.range.last + 1
    }
    if (currentIndex < text.length) {
        append(text.substring(currentIndex))
    }
}

/**
 * Componente Compose TV para visualizar notas de versión o texto Markdown con estética Cyberpunk.
 */
@Composable
fun MarkdownContentView(
    markdown: String,
    modifier: Modifier = Modifier,
    baseTextColor: Color = Color(0xFFD0D8E8),
    fontFamily: FontFamily = ShareTechMonoFontFamily,
    fontSize: TextUnit = 13.sp
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }

    Column(modifier = modifier) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    val headerColor: Color
                    val headerSize: TextUnit
                    val headerWeight: FontWeight
                    val topPadding: Dp

                    when (block.level) {
                        1 -> {
                            headerColor = CyberAmber
                            headerSize = 17.sp
                            headerWeight = FontWeight.ExtraBold
                            topPadding = 8.dp
                        }
                        2 -> {
                            headerColor = CyberCyan
                            headerSize = 15.sp
                            headerWeight = FontWeight.Bold
                            topPadding = 6.dp
                        }
                        3 -> {
                            headerColor = CyberWhite
                            headerSize = 13.5.sp
                            headerWeight = FontWeight.SemiBold
                            topPadding = 5.dp
                        }
                        else -> {
                            headerColor = Color(0xFF94A3B8)
                            headerSize = 12.5.sp
                            headerWeight = FontWeight.SemiBold
                            topPadding = 4.dp
                        }
                    }

                    Text(
                        text = buildMarkdownAnnotatedString(block.text, fontFamily),
                        color = headerColor,
                        fontSize = headerSize,
                        fontWeight = headerWeight,
                        fontFamily = fontFamily,
                        modifier = Modifier.padding(top = topPadding, bottom = 2.dp)
                    )
                }

                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = buildMarkdownAnnotatedString(block.text, fontFamily),
                        color = baseTextColor,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(vertical = 1.5.dp)
                    )
                }

                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = (block.indentLevel * 12).dp,
                                top = 1.5.dp,
                                bottom = 1.5.dp
                            ),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = CyberCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = buildMarkdownAnnotatedString(block.text, fontFamily),
                            color = baseTextColor,
                            fontSize = fontSize,
                            fontFamily = fontFamily,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }

                is MarkdownBlock.TaskItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = (block.indentLevel * 12).dp,
                                top = 1.5.dp,
                                bottom = 1.5.dp
                            ),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = if (block.checked) "[✓]" else "[ ]",
                            color = if (block.checked) CyberCyan else Color(0xFF64748B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = buildMarkdownAnnotatedString(block.text, fontFamily),
                            color = if (block.checked) baseTextColor else Color(0xFF8E95A5),
                            fontSize = fontSize,
                            fontFamily = fontFamily,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }

                is MarkdownBlock.OrderedItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = (block.indentLevel * 12).dp,
                                top = 1.5.dp,
                                bottom = 1.5.dp
                            ),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}.",
                            color = CyberAmber,
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = buildMarkdownAnnotatedString(block.text, fontFamily),
                            color = baseTextColor,
                            fontSize = fontSize,
                            fontFamily = fontFamily,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }

                is MarkdownBlock.Blockquote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(16.dp)
                                .background(CyberAmber, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = buildMarkdownAnnotatedString(block.text, fontFamily),
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = fontFamily,
                            lineHeight = 16.sp
                        )
                    }
                }

                is MarkdownBlock.CodeBlock -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF050811))
                            .border(1.dp, CyberCyan.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        if (block.language.isNotBlank()) {
                            Text(
                                text = block.language.uppercase(),
                                color = CyberAmber.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        Text(
                            text = block.code,
                            color = CyberCyan,
                            fontSize = 11.5.sp,
                            fontFamily = fontFamily,
                            lineHeight = 16.sp
                        )
                    }
                }

                is MarkdownBlock.Divider -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .height(1.dp)
                            .background(CyberCyan.copy(alpha = 0.2f))
                    )
                }

                is MarkdownBlock.EmptyLine -> {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}
