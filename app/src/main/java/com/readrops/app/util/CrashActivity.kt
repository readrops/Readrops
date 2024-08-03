package com.readrops.app.util

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readrops.app.R
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.ReadropsTheme
import com.readrops.app.util.theme.ShortSpacer
import com.readrops.app.util.theme.VeryLargeSpacer
import com.readrops.app.util.theme.VeryShortSpacer
import com.readrops.app.util.theme.spacing

class CrashActivity : ComponentActivity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT))

        val throwable = intent.getSerializableExtra(THROWABLE_KEY) as Throwable?

        setContent {
            ReadropsTheme {
                CrashScreen(throwable?.stackTraceToString().orEmpty())
            }
        }
    }

    companion object {
        const val THROWABLE_KEY = "THROWABLE"
    }
}

@Composable
fun CrashScreen(stackTrace: String) {
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(MaterialTheme.spacing.mediumSpacing)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            VeryLargeSpacer()

            Icon(
                painter = painterResource(id = R.drawable.ic_bug),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            MediumSpacer()

            Text(
                text = stringResource(R.string.readrops_crashed),
                style = MaterialTheme.typography.titleLarge
            )

            ShortSpacer()

            Text(
                text = stringResource(R.string.crash_message),
                style = MaterialTheme.typography.bodyMedium,
            )

            MediumSpacer()

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stackTrace,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Justify,
                    lineHeight = 20.sp,
                    modifier = Modifier
                        .padding(MaterialTheme.spacing.mediumSpacing)
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                )
            }

            MediumSpacer()

            Column {
                Button(
                    onClick = {
                        uriHandler.openUri("https://github.com/readrops/Readrops/issues/new")
                        clipboardManager.setText(AnnotatedString(stackTrace))
                        displayToast(context)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.report_error_github))
                }

                VeryShortSpacer()

                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(stackTrace))
                        displayToast(context)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.copy_error_clipboard))
                }
            }
        }
    }
}

fun displayToast(context: Context) {
    Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
}

@DefaultPreview
@Composable
private fun CrashScreenPreview() {
    ReadropsTheme {
        CrashScreen("")
    }
}