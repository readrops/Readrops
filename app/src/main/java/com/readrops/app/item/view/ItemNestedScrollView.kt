package com.readrops.app.item.view

import android.annotation.SuppressLint
import android.content.Context
import android.widget.RelativeLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView

@SuppressLint("ResourceType", "ViewConstructor")
class ItemNestedScrollView(
    context: Context,
    onGlobalLayoutListener: (viewHeight: Int, contentHeight: Int) -> Unit,
    onUrlClick: (String) -> Unit,
    onImageLongPress: (String) -> Unit,
    composeViewContent: @Composable () -> Unit
) : NestedScrollView(context) {

    init {
        addView(
            RelativeLayout(context).apply {
                ViewCompat.setNestedScrollingEnabled(this, true)

                val composeView = ComposeView(context).apply {
                    id = 1

                    setContent {
                        composeViewContent()
                    }
                }

                val composeViewParams = RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
                )
                composeViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
                composeView.layoutParams = composeViewParams

                val webView = ItemWebView(
                    context = context,
                    onUrlClick = onUrlClick,
                    onImageLongPress = onImageLongPress
                ).apply {
                    id = 2
                    ViewCompat.setNestedScrollingEnabled(this, true)
                }

                val webViewParams = RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
                )

                webViewParams.addRule(RelativeLayout.BELOW, composeView.id)
                webView.layoutParams = webViewParams

                addView(composeView)
                addView(webView)
            }
        )

        viewTreeObserver.addOnGlobalLayoutListener {
            val viewHeight = this.measuredHeight
            val contentHeight = getChildAt(0).height

            onGlobalLayoutListener(viewHeight, contentHeight)
        }
    }
}