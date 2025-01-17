package com.readrops.app.feeds.color

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.readrops.app.R
import com.readrops.app.util.DefaultPreview
import com.readrops.app.util.components.dialog.BaseDialog
import com.readrops.app.util.theme.LargeSpacer
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.ReadropsTheme
import com.readrops.app.util.theme.ShortSpacer
import com.readrops.app.util.theme.spacing

@Composable
fun ColorPickerDialog(
    color: Color?,
    onValidate: (Color) -> Unit,
    onDismiss: () -> Unit,
) {
    val controller = rememberColorPickerController()

    BaseDialog(
        title = stringResource(R.string.select_color),
        icon = painterResource(R.drawable.ic_color),
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = MaterialTheme.spacing.mediumSpacing)
                    .height(200.dp),
                controller = controller,
                initialColor = color
            )

            AlphaSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = MaterialTheme.spacing.shortSpacing)
                    .height(24.dp),
                controller = controller,
            )

            BrightnessSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = MaterialTheme.spacing.shortSpacing)
                    .height(24.dp),
                controller = controller,
            )

            MediumSpacer()

            Box(
                modifier = Modifier
                    .background(controller.selectedColor.value, RectangleShape)
                    .size(48.dp)
                    .align(Alignment.CenterHorizontally)
            )

            LargeSpacer()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(text = stringResource(R.string.back))
                }

                ShortSpacer()

                TextButton(
                    onClick = { onValidate(controller.selectedColor.value) }
                ) {
                    Text(text = stringResource(R.string.validate))
                }
            }

        }
    }
}

@DefaultPreview
@Composable
private fun ColorPickerDialogPreview() {
    ReadropsTheme {
        ColorPickerDialog(
            color = null,
            onValidate = {},
            onDismiss = {},
        )
    }
}