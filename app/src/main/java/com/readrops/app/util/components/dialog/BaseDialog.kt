package com.readrops.app.util.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseDialog(
    title: String,
    icon: Painter,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            tonalElevation = AlertDialogDefaults.TonalElevation,
            shape = AlertDialogDefaults.shape,
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                        .padding(MaterialTheme.spacing.largeSpacing)
            ) {
                Icon(
                    painter = icon,
                    tint = AlertDialogDefaults.iconContentColor,
                    contentDescription = null,
                    modifier = Modifier.size(MaterialTheme.spacing.largeSpacing)
                )

                MediumSpacer()

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = AlertDialogDefaults.titleContentColor
                )

                MediumSpacer()

                content()
            }
        }
    }
}