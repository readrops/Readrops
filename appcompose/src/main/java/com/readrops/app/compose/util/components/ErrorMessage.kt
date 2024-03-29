package com.readrops.app.compose.util.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.readrops.app.compose.R
import com.readrops.app.compose.util.theme.ShortSpacer
import com.readrops.app.compose.util.theme.VeryShortSpacer
import com.readrops.app.compose.util.theme.spacing

@Composable
fun ErrorMessage(
    exception: Exception?
) {
    CenteredColumn {
        Icon(
            painter = painterResource(id = R.drawable.ic_error),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(MaterialTheme.spacing.veryLargeSpacing)
        )

        ShortSpacer()

        Text(
            text = "An error occurred",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
        )

        VeryShortSpacer()

        if (exception != null) {
            val name = exception.javaClass.simpleName
            val message = exception.message

            Text(
                text = "$name: $message",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
    }
}