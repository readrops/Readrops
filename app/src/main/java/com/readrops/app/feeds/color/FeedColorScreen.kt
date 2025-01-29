package com.readrops.app.feeds.color

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.readrops.app.R
import com.readrops.app.util.components.AndroidScreen
import com.readrops.app.util.components.FeedIcon
import com.readrops.app.util.components.SelectableIconText
import com.readrops.app.util.theme.LargeSpacer
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.ShortSpacer
import com.readrops.app.util.theme.spacing
import com.readrops.db.entities.Feed
import org.koin.core.parameter.parametersOf

class FeedColorScreen(val feed: Feed) : AndroidScreen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<FeedColorScreenModel> { parametersOf(feed) }

        val state by screenModel.state.collectAsStateWithLifecycle()

        val snackbarHostState = remember { SnackbarHostState() }
        val defaultColor = MaterialTheme.colorScheme.primary

        if (state.canExit) {
            navigator.pop()
        }

        LaunchedEffect(state.error) {
            if (state.error != null) {
                snackbarHostState.showSnackbar(state.error!!)
                screenModel.resetError()
            }
        }

        if (state.showColorPicker) {
            ColorPickerDialog(
                color = state.newColor ?: state.currentColor,
                onValidate = {
                    screenModel.setNewColor(it)
                    screenModel.closeColorPickerDialog()
                },
                onDismiss = { screenModel.closeColorPickerDialog() }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.feed_color)) },
                    navigationIcon = {
                        IconButton(
                            onClick = { navigator.pop() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = null
                            )
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(vertical = MaterialTheme.spacing.shortSpacing)
            ) {
                Text(
                    text = stringResource(R.string.preview),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.mediumSpacing)
                )

                MediumSpacer()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.mediumSpacing)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        FeedIcon(
                            iconUrl = feed.iconUrl,
                            name = feed.name.orEmpty(),
                        )

                        ShortSpacer()

                        Text(
                            text = feed.name!!,
                            style = MaterialTheme.typography.titleMedium,
                            color = state.newColor ?: state.currentColor
                            ?: MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (state.newColor != null) {
                        ShortSpacer()

                        TextButton(
                            onClick = { screenModel.resetColor() },
                        ) {
                            Text(text = stringResource(R.string.reset))
                        }
                    }
                }

                LargeSpacer()

                Text(
                    text = stringResource(R.string.actions),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.mediumSpacing)
                )

                SelectableIconText(
                    icon = painterResource(R.drawable.ic_sync),
                    text = stringResource(R.string.reload_color_favicon),
                    style = MaterialTheme.typography.titleSmall,
                    padding = MaterialTheme.spacing.mediumSpacing,
                    spacing = MaterialTheme.spacing.mediumSpacing,
                    iconSize = 24.dp,
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = { screenModel.reloadColor(context) },
                )

                SelectableIconText(
                    icon = painterResource(R.drawable.ic_reset_color),
                    text = stringResource(R.string.use_default_color),
                    style = MaterialTheme.typography.titleSmall,
                    padding = MaterialTheme.spacing.mediumSpacing,
                    spacing = MaterialTheme.spacing.mediumSpacing,
                    iconSize = 24.dp,
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = { screenModel.setNewColor(defaultColor) },
                )

                SelectableIconText(
                    icon = painterResource(R.drawable.ic_color),
                    text = stringResource(R.string.select_color),
                    style = MaterialTheme.typography.titleSmall,
                    padding = MaterialTheme.spacing.mediumSpacing,
                    spacing = MaterialTheme.spacing.mediumSpacing,
                    iconSize = 24.dp,
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = { screenModel.showColorPickerDialog() }
                )

                /*SelectableIconText(
                    icon = painterResource(R.drawable.ic_download),
                    text = stringResource(R.string.load_color_new_favicon),
                    style = MaterialTheme.typography.titleSmall,
                    padding = MaterialTheme.spacing.mediumSpacing,
                    spacing = MaterialTheme.spacing.mediumSpacing,
                    iconSize = 24.dp,
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = { }
                )*/

                LargeSpacer()

                Button(
                    onClick = { screenModel.validate() },
                    enabled = state.newColor != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.mediumSpacing)
                ) {
                    Text(text = stringResource(R.string.validate))
                }
            }
        }
    }
}