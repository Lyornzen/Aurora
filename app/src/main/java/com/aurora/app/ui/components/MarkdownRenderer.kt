package com.aurora.app.ui.components

import android.annotation.SuppressLint
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * WebView + KaTeX Markdown 渲染器。
 * 使用 Base64 编码安全传递内容，动态测量内容高度避免截断。
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MarkdownRenderer(
    content: String,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var measuredPx by remember { mutableIntStateOf(0) }
    val currentIsDark by rememberUpdatedState(isDarkTheme)

    // Base64 编码：避免手动转义带来的注入和截断风险
    val encoded = remember(content) {
        Base64.encodeToString(content.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
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
                        // 只接受更大的高度值，避免被早期测量覆盖
                        if (px > measuredPx) {
                            measuredPx = px
                        }
                    }
                }, "AndroidBridge")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        val dark = if (currentIsDark) "true" else "false"
                        view?.evaluateJavascript("initRender('$encoded',$dark);", null)
                    }
                }

                loadUrl("file:///android_asset/mark.html")
            }
        },
        update = { view ->
            val dark = if (isDarkTheme) "true" else "false"
            // 重置高度，利用 heightIn(min=48.dp) 避免缩到零
            measuredPx = 0
            view.evaluateJavascript("initRender('$encoded',$dark);", null)
        },
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (measuredPx > 0)
                    // heightIn(min=) 允许内容超出测量值，避免固定高度裁剪
                    with(density) { Modifier.heightIn(min = measuredPx.toDp()) }
                else
                    Modifier.heightIn(min = 48.dp)
            )
    )
}
