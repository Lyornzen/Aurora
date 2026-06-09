package com.aurora.app.ui.components

import android.annotation.SuppressLint
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * WebView + KaTeX Markdown renderer.
 * Uses Base64 encoding for safe content transfer and dynamic height measurement.
 * mark.html handles incomplete markdown syntax during streaming (escapes unpaired delimiters).
 * When isStreaming transitions from true→false, calls finalizeRender() for a clean re-parse.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MarkdownRenderer(
    content: String,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    isStreaming: Boolean = false,
) {
    val density = LocalDensity.current
    var measuredPx by remember { mutableIntStateOf(0) }
    val currentIsDark by rememberUpdatedState(isDarkTheme)

    // Base64 encode
    val encoded = remember(content) {
        if (content.isBlank()) ""
        else Base64.encodeToString(content.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    // Skip rendering for empty content
    if (content.isBlank()) return

    // Track WebView reference for finalizeRender
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // When streaming ends, do a clean re-parse (no incomplete-markdown escaping)
    LaunchedEffect(isStreaming) {
        if (!isStreaming && content.isNotBlank()) {
            kotlinx.coroutines.delay(100) // small delay to ensure last content is rendered
            val dark = if (currentIsDark) "true" else "false"
            try {
                webViewRef?.evaluateJavascript("finalizeRender($dark);", null)
            } catch (_: Exception) {}
        }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false

                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onHeight(px: Int) {
                        if (px > measuredPx) {
                            measuredPx = px
                        }
                    }
                }, "AndroidBridge")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        if (encoded.isNotEmpty()) {
                            val dark = if (currentIsDark) "true" else "false"
                            try {
                                view?.evaluateJavascript("initRender('$encoded',$dark);", null)
                            } catch (_: Exception) {}
                        }
                    }
                }

                loadUrl("file:///android_asset/mark.html")
                webViewRef = this
            }
        },
        update = { view ->
            webViewRef = view
            if (encoded.isNotEmpty()) {
                val dark = if (isDarkTheme) "true" else "false"
                measuredPx = 0
                try {
                    view.evaluateJavascript("initRender('$encoded',$dark);", null)
                } catch (_: Exception) {}
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (measuredPx > 0)
                    with(density) { Modifier.heightIn(min = measuredPx.toDp()) }
                else
                    Modifier.heightIn(min = 48.dp)
            )
    )
}
