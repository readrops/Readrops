package com.readrops.app.more.preferences.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import com.readrops.app.R
import com.readrops.app.util.Preference
import com.readrops.app.util.ShareIntentTextRenderer
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.ShortSpacer
import com.readrops.db.pojo.ItemWithFeed
import kotlinx.coroutines.launch

@Composable
fun CustomShareIntentTextWidget(
    preference: Preference<String>,
    template: String,
    exampleItem: ItemWithFeed,
    onDismiss: () -> Unit,
) {
    var localTemplate by remember { mutableStateOf(template) }
    var generateTemplate by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val renderer = remember { ShareIntentTextRenderer(exampleItem) }

    PreferenceBaseDialog(
        title = stringResource(R.string.use_custom_share_intent_tpl),
        onDismiss = onDismiss
    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = AnnotatedString.fromHtml(
                    stringResource(R.string.use_custom_share_intent_tpl_explenation),
                    linkStyles = TextLinkStyles(
                        style = SpanStyle(
                            textDecoration = TextDecoration.Underline,
                            fontStyle = FontStyle.Italic,
                            color = Color.Blue
                        )
                    )
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = AlertDialogDefaults.textContentColor
            )

            MediumSpacer()

            TextField(
                value = (
                        if (generateTemplate) renderer.renderOrError(localTemplate)
                        else localTemplate
                        ),
                onValueChange = { localTemplate = it },
                readOnly = generateTemplate,
                minLines = 3,
                modifier = Modifier.focusRequester(focusRequester),
            )

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            ShortSpacer()

            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                TextButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        localTemplate = """
                            {{ title|remove_author|capitalize }} — {{ feedName }}
                            
                            {{ url }}
                        """.trimIndent()
                    },
                ) {
                    Text(text = stringResource(R.string.try_the_default_template))
                }

                TextButton(
                    modifier = Modifier.weight(1f),
                    onClick = { generateTemplate = !generateTemplate },
                ) {
                    Text(
                        text = (
                            if (generateTemplate) stringResource(R.string.edit_template)
                            else stringResource(R.string.render_template)
                        )
                    )
                }
            }

            MediumSpacer()

            Text(
                AnnotatedString.fromHtml(
                    stringResource(
                        R.string.example_item_explanation,
                        renderer.context.keys.joinToString(transform = { "<tt>$it</tt>" }),
                        renderer.documentation
                    ),
                    linkStyles = TextLinkStyles(
                        style = SpanStyle(
                            textDecoration = TextDecoration.Underline,
                            fontStyle = FontStyle.Italic,
                            color = Color.Blue
                        )
                    )
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = AlertDialogDefaults.textContentColor
            )

            MediumSpacer()

            Row(modifier = Modifier.align(Alignment.End)) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(id = R.string.back))
                }

                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            preference.write(localTemplate)
                            onDismiss()
                        }
                    },
                ) {
                    Text(text = stringResource(id = R.string.save))
                }
            }
        }
    }
}
