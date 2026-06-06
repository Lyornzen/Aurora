package com.aurora.app.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

/**
 * Converts LaTeX math to Unicode + AnnotatedString.
 * Supports: \frac, \sqrt[n], \int_{}^{}, \sum_{}^{}, \prod_{}^{}, \lim,
 * matrices, cases, Greek letters, named functions, etc.
 */
object LatexRenderer {

    // ─── Symbols ────────────────────────────────────────────────

    private val CMDS = linkedMapOf(
        // Named functions (roman) — checked before generic symbols
        "\\sin" to "sin", "\\cos" to "cos", "\\tan" to "tan",
        "\\csc" to "csc", "\\sec" to "sec", "\\cot" to "cot",
        "\\arcsin" to "arcsin", "\\arccos" to "arccos", "\\arctan" to "arctan",
        "\\sinh" to "sinh", "\\cosh" to "cosh", "\\tanh" to "tanh",
        "\\log" to "log", "\\ln" to "ln", "\\lg" to "lg",
        "\\exp" to "exp",
        "\\lim" to "lim", "\\limsup" to "lim sup", "\\liminf" to "lim inf",
        "\\max" to "max", "\\min" to "min",
        "\\sup" to "sup", "\\inf" to "inf",
        "\\det" to "det", "\\gcd" to "gcd", "\\Pr" to "Pr",
        "\\dim" to "dim", "\\ker" to "ker", "\\deg" to "deg",
        "\\hom" to "hom", "\\arg" to "arg",
        // Greek lowercase
        "\\alpha" to "\u03B1", "\\beta" to "\u03B2", "\\gamma" to "\u03B3",
        "\\delta" to "\u03B4", "\\epsilon" to "\u03B5", "\\zeta" to "\u03B6",
        "\\eta" to "\u03B7", "\\theta" to "\u03B8", "\\iota" to "\u03B9",
        "\\kappa" to "\u03BA", "\\lambda" to "\u03BB", "\\mu" to "\u03BC",
        "\\nu" to "\u03BD", "\\xi" to "\u03BE", "\\pi" to "\u03C0",
        "\\rho" to "\u03C1", "\\sigma" to "\u03C3", "\\tau" to "\u03C4",
        "\\upsilon" to "\u03C5", "\\phi" to "\u03C6", "\\chi" to "\u03C7",
        "\\psi" to "\u03C8", "\\omega" to "\u03C9",
        "\\varepsilon" to "\u03B5", "\\vartheta" to "\u03D1",
        "\\varphi" to "\u03C6",
        // Greek uppercase
        "\\Gamma" to "\u0393", "\\Delta" to "\u0394", "\\Theta" to "\u0398",
        "\\Lambda" to "\u039B", "\\Xi" to "\u039E", "\\Pi" to "\u03A0",
        "\\Sigma" to "\u03A3", "\\Upsilon" to "\u03A5", "\\Phi" to "\u03A6",
        "\\Psi" to "\u03A8", "\\Omega" to "\u03A9",
        // Big operators (with limits support via _ and ^)
        "\\int" to "\u222B", "\\iint" to "\u222C", "\\iiint" to "\u222D",
        "\\oint" to "\u222E", "\\sum" to "\u2211", "\\prod" to "\u220F",
        "\\coprod" to "\u2210", "\\bigcup" to "\u22C3", "\\bigcap" to "\u22C2",
        "\\bigvee" to "\u22C1", "\\bigwedge" to "\u22C0", "\\bigoplus" to "\u2A01",
        "\\bigotimes" to "\u2A02",
        // Binary operators / relations
        "\\partial" to "\u2202", "\\nabla" to "\u2207",
        "\\infty" to "\u221E", "\\pm" to "\u00B1", "\\mp" to "\u2213",
        "\\times" to "\u00D7", "\\div" to "\u00F7", "\\cdot" to "\u00B7",
        "\\leq" to "\u2264", "\\geq" to "\u2265", "\\neq" to "\u2260",
        "\\approx" to "\u2248", "\\equiv" to "\u2261", "\\sim" to "\u223C",
        "\\simeq" to "\u2243", "\\cong" to "\u2245",
        "\\propto" to "\u221D", "\\ll" to "\u226A", "\\gg" to "\u226B",
        "\\mid" to "\u2223", "\\nmid" to "\u2224",
        "\\oplus" to "\u2295", "\\ominus" to "\u2296", "\\otimes" to "\u2297",
        "\\oslash" to "\u2298", "\\odot" to "\u2299",
        // Arrows
        "\\to" to "\u2192", "\\rightarrow" to "\u2192", "\\leftarrow" to "\u2190",
        "\\Rightarrow" to "\u21D2", "\\Leftarrow" to "\u21D0",
        "\\Leftrightarrow" to "\u21D4", "\\leftrightarrow" to "\u2194",
        "\\mapsto" to "\u21A6", "\\longmapsto" to "\u27FC",
        "\\uparrow" to "\u2191", "\\downarrow" to "\u2193",
        "\\longrightarrow" to "\u27F6", "\\longleftarrow" to "\u27F5",
        // Sets / logic
        "\\in" to "\u2208", "\\notin" to "\u2209", "\\ni" to "\u220B",
        "\\subset" to "\u2282", "\\supset" to "\u2283",
        "\\subseteq" to "\u2286", "\\supseteq" to "\u2287",
        "\\cup" to "\u222A", "\\cap" to "\u2229",
        "\\setminus" to "\u2216",
        "\\emptyset" to "\u2205", "\\varnothing" to "\u2205",
        "\\forall" to "\u2200", "\\exists" to "\u2203", "\\nexists" to "\u2204",
        "\\neg" to "\u00AC", "\\lnot" to "\u00AC",
        "\\land" to "\u2227", "\\lor" to "\u2228",
        "\\top" to "\u22A4", "\\bot" to "\u22A5",
        // Brackets
        "\\langle" to "\u27E8", "\\rangle" to "\u27E9",
        "\\lceil" to "\u2308", "\\rceil" to "\u2309",
        "\\lfloor" to "\u230A", "\\rfloor" to "\u230B",
        // Misc
        "\\ldots" to "\u2026", "\\cdots" to "\u22EF", "\\vdots" to "\u22EE",
        "\\ddots" to "\u22F1",
        "\\angle" to "\u2220", "\\parallel" to "\u2225", "\\perp" to "\u27C2",
        "\\circ" to "\u2218", "\\hbar" to "\u0127", "\\imath" to "\u0131",
        "\\jmath" to "\u0237", "\\ell" to "\u2113", "\\wp" to "\u2118",
        "\\Re" to "\u211C", "\\Im" to "\u2111", "\\aleph" to "\u2135",
        "\\prime" to "\u2032", "\\backslash" to "\\",
        "\\Box" to "\u25A1", "\\Diamond" to "\u25C7",
        "\\triangle" to "\u25B3", "\\triangledown" to "\u25BD",
    )

    // Longer keys first (e.g. \\longmapsto before \\mapsto)
    private val SORTED = CMDS.keys.sortedByDescending { it.length }

    // Commands that get limits (subscript/superscript below/above in display mode)
    private val LIMIT_OPS = setOf("\\int", "\\iint", "\\iiint", "\\oint",
        "\\sum", "\\prod", "\\coprod", "\\bigcup", "\\bigcap",
        "\\bigvee", "\\bigwedge", "\\bigoplus", "\\bigotimes",
        "\\lim", "\\limsup", "\\liminf", "\\max", "\\min", "\\sup", "\\inf", "\\det", "\\gcd", "\\Pr")

    // Named functions that should be roman (non-italic)
    private val ROMAN_FUNCS = setOf("\\sin", "\\cos", "\\tan", "\\csc", "\\sec", "\\cot",
        "\\arcsin", "\\arccos", "\\arctan", "\\sinh", "\\cosh", "\\tanh",
        "\\log", "\\ln", "\\lg", "\\exp", "\\lim", "\\limsup", "\\liminf",
        "\\max", "\\min", "\\sup", "\\inf", "\\det", "\\gcd", "\\Pr",
        "\\dim", "\\ker", "\\deg", "\\hom", "\\arg")

    // ─── Public API ─────────────────────────────────────────────

    fun render(formula: String, color: androidx.compose.ui.graphics.Color): AnnotatedString {
        return buildAnnotatedString {
            var rem = formula
                .replace("\n", " ")
                .replace("\\,", "").replace("\\;", "").replace("\\:", "").replace("\\ ", "")
                .replace("\\left", "").replace("\\right", "") // strip sizing hints
                .trim()

            while (rem.isNotEmpty()) {
                when {
                    // ── Environments: \begin{...} ... \end{...} ──
                    rem.startsWith("\\begin{") -> renderEnvironment(rem, color).let { (s, r) ->
                        append(s); rem = r
                    }

                    // ── \frac{num}{den} ──
                    rem.startsWith("\\frac{") -> {
                        val (num, den, rest) = bracePair(rem, 6)
                        if (num != null && den != null) {
                            withStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 11.sp)) {
                                append(render(num, color))
                            }
                            append("\u2044") // fraction slash ⁄
                            withStyle(SpanStyle(baselineShift = BaselineShift.Subscript, fontSize = 11.sp)) {
                                append(render(den, color))
                            }
                            rem = rest
                        } else { append("\\frac"); rem = rem.drop(6) }
                    }

                    // ── \sqrt[n]{x} or \sqrt{x} ──
                    rem.startsWith("\\sqrt") -> {
                        val afterSqrt = rem.drop(5) // skip "\sqrt"
                        if (afterSqrt.startsWith("[")) {
                            // nth root
                            val closeB = afterSqrt.indexOf(']')
                            if (closeB > 1 && afterSqrt.length > closeB + 1 && afterSqrt[closeB + 1] == '{') {
                                val n = afterSqrt.substring(1, closeB)
                                val (inner, _, rest) = bracePair(rem, 5 + closeB + 1)
                                if (inner != null) {
                                    withStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 10.sp)) {
                                        append(render(n, color))
                                    }
                                    append("\u221A")
                                    append(render(inner, color))
                                    rem = rest
                                }
                            } else { append("\u221A"); rem = rem.drop(5) }
                        } else if (afterSqrt.startsWith("{")) {
                            val (inner, _, rest) = bracePair(rem, 6)
                            if (inner != null) {
                                append("\u221A")
                                append(render(inner, color))
                                rem = rest
                            } else { append("\u221A"); rem = rem.drop(6) }
                        } else {
                            append("\u221A"); rem = rem.drop(5)
                        }
                    }

                    // ── Superscript ^{...} ──
                    rem.startsWith("^{") -> {
                        val (sup, _, rest) = bracePair(rem, 2)
                        if (sup != null) {
                            withStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 11.sp)) {
                                append(render(sup, color))
                            }
                            rem = rest
                        } else { append('^'); rem = rem.drop(1) }
                    }
                    rem.startsWith("^") && rem.length > 1 && rem[1] != '{' && rem[1] != ' ' -> {
                        withStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 11.sp)) {
                            append(rem[1].toString())
                        }
                        rem = rem.drop(2)
                    }

                    // ── Subscript _{...} ──
                    rem.startsWith("_{") -> {
                        val (sub, _, rest) = bracePair(rem, 2)
                        if (sub != null) {
                            withStyle(SpanStyle(baselineShift = BaselineShift.Subscript, fontSize = 11.sp)) {
                                append(render(sub, color))
                            }
                            rem = rest
                        } else { append('_'); rem = rem.drop(1) }
                    }
                    rem.startsWith("_") && rem.length > 1 && rem[1] != '{' && rem[1] != ' ' -> {
                        withStyle(SpanStyle(baselineShift = BaselineShift.Subscript, fontSize = 11.sp)) {
                            append(rem[1].toString())
                        }
                        rem = rem.drop(2)
                    }

                    // ── Named command ──
                    rem.startsWith("\\") -> {
                        var matched = false
                        for (key in SORTED) {
                            if (rem.startsWith(key)) {
                                val value = CMDS[key]!!
                                if (key in ROMAN_FUNCS) {
                                    withStyle(SpanStyle(fontStyle = FontStyle.Normal)) {
                                        append(value)
                                    }
                                } else {
                                    append(value)
                                }
                                rem = rem.drop(key.length)
                                matched = true
                                break
                            }
                        }
                        if (!matched) {
                            // Unknown backslash command — drop backslash
                            if (rem.length > 1) { append(rem[1]); rem = rem.drop(2) }
                            else { append('\\'); rem = rem.drop(1) }
                        }
                    }

                    // ── Plain text ──
                    else -> { append(rem[0]); rem = rem.drop(1) }
                }
            }
        }
    }

    // ─── Environments ───────────────────────────────────────────

    private fun renderEnvironment(
        text: String, color: androidx.compose.ui.graphics.Color
    ): Pair<AnnotatedString, String> {
        // Find matching \end{...}
        val envStart = text.substringAfter("\\begin{").substringBefore("}")
        val envType = envStart.trim()
        val beginTag = "\\begin{$envStart}"
        val endTag = "\\end{$envStart}"
        val beginIdx = text.indexOf(beginTag)
        val endIdx = text.indexOf(endTag, beginIdx + beginTag.length)

        if (endIdx < 0) {
            return Pair(buildAnnotatedString { append("\\begin{...}") }, text.drop(7))
        }

        val body = text.substring(beginIdx + beginTag.length, endIdx)
        val rest = text.drop(endIdx + endTag.length)

        return when (envType) {
            "cases" -> renderCases(body, color) to rest
            "matrix" -> renderMatrix(body, "", color) to rest
            "pmatrix" -> renderMatrix(body, "()", color) to rest
            "bmatrix" -> renderMatrix(body, "[]", color) to rest
            "Bmatrix" -> renderMatrix(body, "{}", color) to rest
            "vmatrix" -> renderMatrix(body, "||", color) to rest
            "Vmatrix" -> renderMatrix(body, "\u2016\u2016", color) to rest
            else -> Pair(buildAnnotatedString { append("[matrix]") }, rest)
        }
    }

    private fun renderCases(body: String, color: androidx.compose.ui.graphics.Color): AnnotatedString {
        return buildAnnotatedString {
            append("{ ")
            withStyle(SpanStyle(baselineShift = BaselineShift.Subscript, fontSize = 12.sp)) {
                val rows = body.split("\\\\")
                rows.forEachIndexed { i, row ->
                    val parts = row.split("&", limit = 2)
                    val expr = parts.getOrElse(0) { "" }.trim()
                    val cond = parts.getOrElse(1) { "" }.trim()
                    append(render(expr, color))
                    append("  ")
                    append(render(cond, color))
                    if (i < rows.size - 1) append("\n")
                }
            }
            append(" ")
        }
    }

    private fun renderMatrix(body: String, bracket: String, color: androidx.compose.ui.graphics.Color): AnnotatedString {
        val leftB = when {
            bracket.contains("(") -> "("
            bracket.contains("[") -> "["
            bracket.contains("{") -> "{"
            bracket.contains("|") -> "|"
            bracket.contains("\u2016") -> "\u2016"
            else -> ""
        }
        val rightB = when {
            bracket.contains(")") -> ")"
            bracket.contains("]") -> "]"
            bracket.contains("}") -> "}"
            bracket.contains("|") -> "|"
            bracket.contains("\u2016") -> "\u2016"
            else -> ""
        }
        return buildAnnotatedString {
            append(leftB)
            val rows = body.split("\\\\")
            rows.forEachIndexed { i, row ->
                if (i > 0) append("\n")
                val cells = row.split("&")
                cells.forEachIndexed { j, cell ->
                    if (j > 0) append("  ")
                    append(render(cell.trim(), color))
                }
            }
            append(rightB)
        }
    }

    // ─── Helpers ────────────────────────────────────────────────

    private fun bracePair(text: String, startAt: Int): Triple<String?, String?, String> {
        var pos = startAt
        if (pos >= text.length || text[pos] != '{') return Triple(null, null, text.drop(startAt))
        pos++
        var depth = 1
        val sb = StringBuilder()
        while (pos < text.length && depth > 0) {
            when (text[pos]) {
                '{' -> depth++
                '}' -> depth--
            }
            if (depth > 0) sb.append(text[pos])
            pos++
        }
        val first = sb.toString()
        if (pos < text.length && text[pos] == '{') {
            pos++
            depth = 1
            val sb2 = StringBuilder()
            while (pos < text.length && depth > 0) {
                when (text[pos]) {
                    '{' -> depth++
                    '}' -> depth--
                }
                if (depth > 0) sb2.append(text[pos])
                pos++
            }
            return Triple(first, sb2.toString(), text.drop(pos))
        }
        return Triple(first, null, text.drop(pos))
    }
}
