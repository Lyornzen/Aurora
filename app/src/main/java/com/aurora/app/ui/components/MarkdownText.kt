package com.aurora.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Renders LLM Markdown output with support for:
 * - **bold**, *italic*, ***bold-italic***, ~~strikethrough~~
 * - `inline code`, ```code blocks```
 * - # H1–H3 headers, > blockquote, horizontal rules
 * - -/+ unordered and 1. ordered lists
 * - | tables |
 * - [links](url) and ![image](url) placeholders
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    codeBg: Color = Color.Unspecified,
    fontSize: Float = 14f,
    lineHeight: Float = 22f,
) {
    val blocks = remember(text) { parseMarkdownBlocks(text) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (block in blocks) {
            when (block) {
                is MdBlock.CodeBlock -> CodeBlockView(block.code, codeBg)
                is MdBlock.Heading -> {
                    Text(
                        text = renderInlineMarkdown(block.text, textColor, codeBg, fontSize),
                        fontSize = when (block.level) {
                            1 -> (fontSize + 6).sp
                            2 -> (fontSize + 3).sp
                            else -> (fontSize + 1).sp
                        },
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        lineHeight = (lineHeight + 2).sp,
                    )
                }
                is MdBlock.Paragraph -> {
                    Text(
                        text = renderInlineMarkdown(block.text, textColor, codeBg, fontSize),
                        fontSize = fontSize.sp,
                        color = textColor,
                        lineHeight = lineHeight.sp,
                    )
                }
                is MdBlock.ListItem -> {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (block.ordered) "${block.index}. " else "• ",
                            fontSize = fontSize.sp,
                            color = textColor,
                            lineHeight = lineHeight.sp,
                        )
                        Text(
                            text = renderInlineMarkdown(block.text, textColor, codeBg, fontSize),
                            fontSize = fontSize.sp,
                            color = textColor,
                            lineHeight = lineHeight.sp,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                is MdBlock.BlockQuote -> {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier
                            .width(3.dp)
                            .height((lineHeight * 1.2f).dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(textColor.copy(alpha = 0.3f))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = renderInlineMarkdown(block.text, textColor, codeBg, fontSize),
                            fontSize = fontSize.sp,
                            color = textColor.copy(alpha = 0.85f),
                            fontStyle = FontStyle.Italic,
                            lineHeight = lineHeight.sp,
                        )
                    }
                }
                is MdBlock.HorizontalRule -> {
                    HorizontalDivider(color = textColor.copy(alpha = 0.2f))
                }
                is MdBlock.Table -> {
                    TableBlock(block, textColor, codeBg, fontSize, lineHeight)
                }
                is MdBlock.LatexBlock -> {
                    LatexBlockView(block.formula, textColor, codeBg)
                }
            }
        }
    }
}

// ─── Table rendering ─────────────────────────────────────────────

@Composable
private fun TableBlock(
    table: MdBlock.Table,
    textColor: Color,
    codeBg: Color,
    fontSize: Float,
    lineHeight: Float,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(textColor.copy(alpha = 0.06f))
            .padding(8.dp)
    ) {
        table.rows.forEachIndexed { rowIdx, row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEachIndexed { colIdx, cell ->
                    Text(
                        text = renderInlineMarkdown(cell.trim(), textColor, codeBg, fontSize),
                        fontSize = (fontSize - 1).sp,
                        color = textColor,
                        fontWeight = if (rowIdx == 0) FontWeight.Bold else FontWeight.Normal,
                        lineHeight = lineHeight.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                }
            }
            if (rowIdx == 0 && table.rows.size > 1) {
                HorizontalDivider(
                    color = textColor.copy(alpha = 0.15f),
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun LatexBlockView(formula: String, textColor: Color, accent: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = 0.08f))
            .padding(12.dp)
    ) {
        Text(
            text = LatexRenderer.render(formula.trimEnd(), accent),
            fontSize = 14.sp,
            color = accent,
            lineHeight = 22.sp,
        )
    }
}

@Composable
private fun CodeBlockView(code: String, bg: Color) {
    val resolvedBg = if (bg == Color.Unspecified) Color(0xFF1E1E2E) else bg
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(resolvedBg.copy(alpha = 0.12f))
            .padding(12.dp)
    ) {
        Text(
            text = code.trimEnd(),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = if (bg == Color.Unspecified) Color(0xFFCDD6F4) else bg,
            lineHeight = 18.sp,
        )
    }
}

// ─── Markdown parser ────────────────────────────────────────────

private sealed class MdBlock {
    data class Paragraph(val text: String) : MdBlock()
    data class Heading(val text: String, val level: Int) : MdBlock()
    data class CodeBlock(val code: String) : MdBlock()
    data class ListItem(val text: String, val ordered: Boolean, val index: Int = 0) : MdBlock()
    data class BlockQuote(val text: String) : MdBlock()
    data object HorizontalRule : MdBlock()
    data class Table(val rows: List<List<String>>) : MdBlock()
    data class LatexBlock(val formula: String) : MdBlock()
}

private fun parseMarkdownBlocks(raw: String): List<MdBlock> {
    val blocks = mutableListOf<MdBlock>()
    val lines = raw.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        when {
            // LaTeX block $$...$$
            line.trimStart().startsWith("$$") && !line.trimStart().startsWith("$$$") -> {
                val sb = StringBuilder()
                i++ // skip opening $$
                while (i < lines.size && !lines[i].trimStart().startsWith("$$")) {
                    if (sb.isNotEmpty()) sb.append("\n")
                    sb.append(lines[i])
                    i++
                }
                if (sb.isNotEmpty()) blocks.add(MdBlock.LatexBlock(sb.toString()))
                i++ // skip closing $$
            }

            // Code block
            line.trimStart().startsWith("```") -> {
                val sb = StringBuilder()
                i++
                while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                    if (sb.isNotEmpty()) sb.append("\n")
                    sb.append(lines[i])
                    i++
                }
                if (sb.isNotEmpty()) blocks.add(MdBlock.CodeBlock(sb.toString()))
                i++
            }

            // Table (must check before heading since ### is 3 chars but | is distinctive)
            line.trimStart().startsWith("|") && line.trimEnd().endsWith("|") -> {
                val tableRows = mutableListOf<List<String>>()
                while (i < lines.size &&
                    lines[i].trimStart().startsWith("|") &&
                    lines[i].trimEnd().endsWith("|")
                ) {
                    val cells = lines[i].split("|")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() && !it.all { c -> c == '-' || c == ':' } }
                    if (cells.isNotEmpty()) tableRows.add(cells)
                    i++
                }
                if (tableRows.isNotEmpty()) blocks.add(MdBlock.Table(tableRows))
            }

            // Heading
            line.trimStart().startsWith("#") && !line.trimStart().startsWith("####") -> {
                val trimmed = line.trimStart()
                val level = trimmed.takeWhile { it == '#' }.length
                val text = trimmed.drop(level).trim()
                if (text.isNotEmpty()) blocks.add(MdBlock.Heading(text, level.coerceIn(1, 3)))
                i++
            }

            // Blockquote
            line.trimStart().startsWith(">") -> {
                val quoteLines = mutableListOf<String>()
                while (i < lines.size && lines[i].trimStart().startsWith(">")) {
                    quoteLines.add(lines[i].trimStart().removePrefix(">").trim())
                    i++
                }
                blocks.add(MdBlock.BlockQuote(quoteLines.joinToString(" ")))
            }

            // Horizontal rule
            line.trim().matches(Regex("^-{3,}$|^\\*{3,}$|^_{3,}$")) -> {
                blocks.add(MdBlock.HorizontalRule)
                i++
            }

            // Unordered list (-, *, +)
            line.trimStart().matches(Regex("^[-*+]\\s.*")) -> {
                while (i < lines.size && lines[i].trimStart().matches(Regex("^[-*+]\\s.*"))) {
                    val text = lines[i].trimStart().replaceFirst(Regex("^[-*+]\\s"), "")
                    blocks.add(MdBlock.ListItem(text, false))
                    i++
                }
            }

            // Ordered list
            line.trimStart().matches(Regex("^\\d+\\.\\s.*")) -> {
                var idx = 1
                while (i < lines.size && lines[i].trimStart().matches(Regex("^\\d+\\.\\s.*"))) {
                    val text = lines[i].trimStart().replaceFirst(Regex("^\\d+\\.\\s"), "")
                    blocks.add(MdBlock.ListItem(text, true, idx))
                    idx++
                    i++
                }
            }

            // Empty line
            line.isBlank() -> i++

            // Paragraph
            else -> {
                val paraLines = mutableListOf<String>()
                while (i < lines.size && lines[i].isNotBlank() &&
                    !lines[i].trimStart().startsWith("$$") &&
                    !lines[i].trimStart().startsWith("```") &&
                    !lines[i].trimStart().startsWith("|") &&
                    !lines[i].trimStart().startsWith("#") &&
                    !lines[i].trimStart().startsWith(">") &&
                    !lines[i].trimStart().matches(Regex("^[-*+]\\s")) &&
                    !lines[i].trimStart().matches(Regex("^\\d+\\.\\s")) &&
                    !lines[i].trim().matches(Regex("^-{3,}$|^\\*{3,}$|^_{3,}$"))
                ) {
                    paraLines.add(lines[i])
                    i++
                }
                val p = paraLines.joinToString(" ").trim()
                if (p.isNotEmpty()) blocks.add(MdBlock.Paragraph(p))
            }
        }
    }
    return blocks
}

// ─── Inline Markdown → AnnotatedString ──────────────────────────

private fun renderInlineMarkdown(
    text: String,
    defaultColor: Color,
    linkColor: Color,
    fontSize: Float,
) = buildAnnotatedString {
    val chars = text.toCharArray()
    var pos = 0
    val n = chars.size

    fun peek(offset: Int): Char? = if (pos + offset < n) chars[pos + offset] else null
    fun advance(count: Int = 1) { pos += count }

    while (pos < n) {
        val c = chars[pos]
        when {
            // Inline LaTeX $$...$$ (double-dollar, block-style in paragraph)
            c == '$' && peek(1) == '$' && peek(2) != '$' -> {
                val endIdx = text.indexOf("$$", pos + 2)
                if (endIdx > pos + 2) {
                    val formula = text.substring(pos + 2, endIdx)
                    withStyle(SpanStyle(
                        background = linkColor.copy(alpha = 0.08f),
                    )) {
                        append(" ")
                        append(LatexRenderer.render(formula, linkColor))
                        append(" ")
                    }
                    pos = endIdx + 2
                } else {
                    append(c); advance()
                }
            }

            // Inline LaTeX $...$ (single-dollar)
            c == '$' && peek(1) != '$' -> {
                val endIdx = text.indexOf('$', pos + 1)
                if (endIdx > pos + 1) {
                    val formula = text.substring(pos + 1, endIdx)
                    withStyle(SpanStyle(
                        background = linkColor.copy(alpha = 0.06f),
                    )) {
                        append(LatexRenderer.render(formula, linkColor))
                    }
                    pos = endIdx + 1
                } else {
                    append(c); advance()
                }
            }

            // Image ![alt](url)
            c == '!' && peek(1) == '[' -> {
                val closeBracket = text.indexOf(']', pos + 2)
                val openParen = if (closeBracket > 0) closeBracket + 1 else -1
                if (openParen > 0 && openParen < n && chars[openParen] == '(') {
                    val closeParen = text.indexOf(')', openParen + 1)
                    if (closeParen > openParen) {
                        val alt = text.substring(pos + 2, closeBracket)
                        withStyle(SpanStyle(color = linkColor, fontStyle = FontStyle.Italic)) {
                            append("[Image: $alt]")
                        }
                        pos = closeParen + 1
                        continue
                    }
                }
                append(c)
                advance()
            }

            // Link [text](url)
            c == '[' -> {
                val closeBracket = text.indexOf(']', pos + 1)
                val openParen = if (closeBracket > 0) closeBracket + 1 else -1
                if (openParen > 0 && openParen < n && chars[openParen] == '(') {
                    val closeParen = text.indexOf(')', openParen + 1)
                    if (closeParen > openParen) {
                        val linkText = text.substring(pos + 1, closeBracket)
                        val url = text.substring(openParen + 1, closeParen)
                        withStyle(SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline,
                        )) {
                            append(linkText)
                        }
                        pushStringAnnotation("url", url)
                        pop()
                        pos = closeParen + 1
                        continue
                    }
                }
                append(c)
                advance()
            }

            // Bold-italic *** or **_
            (c == '*' || c == '_') && peek(1) == c && peek(2) == c -> {
                val marker = "$c$c$c"
                val endIdx = text.indexOf(marker, pos + 3)
                if (endIdx > pos) {
                    val inner = text.substring(pos + 3, endIdx)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                        append(inner)
                    }
                    pos = endIdx + 3
                } else {
                    append(c); advance()
                }
            }

            // Bold ** or __
            (c == '*' && peek(1) == '*' && peek(2) != '*') ||
            (c == '_' && peek(1) == '_' && peek(2) != '_') -> {
                val marker = "$c$c"
                val endIdx = text.indexOf(marker, pos + 2)
                if (endIdx > pos) {
                    val inner = text.substring(pos + 2, endIdx)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(inner)
                    }
                    pos = endIdx + 2
                } else {
                    append(c); advance()
                }
            }

            // Italic * or _ (single, not followed by same char)
            (c == '*' && peek(1) != '*') || (c == '_' && peek(1) != '_') -> {
                val marker = "$c"
                val endIdx = text.indexOf(marker, pos + 1)
                if (endIdx > pos + 1) {
                    val inner = text.substring(pos + 1, endIdx)
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(inner)
                    }
                    pos = endIdx + 1
                } else {
                    append(c); advance()
                }
            }

            // Strikethrough ~~
            c == '~' && peek(1) == '~' -> {
                val endIdx = text.indexOf("~~", pos + 2)
                if (endIdx > pos) {
                    val inner = text.substring(pos + 2, endIdx)
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        append(inner)
                    }
                    pos = endIdx + 2
                } else {
                    append(c); advance()
                }
            }

            // Inline code `
            c == '`' -> {
                val endIdx = text.indexOf('`', pos + 1)
                if (endIdx > pos) {
                    val code = text.substring(pos + 1, endIdx)
                    withStyle(SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color(0x33000000),
                    )) {
                        append(code)
                    }
                    pos = endIdx + 1
                } else {
                    append(c); advance()
                }
            }

            // Plain char
            else -> {
                append(c)
                advance()
            }
        }
    }
}
