package com.readrops.app.more

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.readrops.app.BuildConfig
import com.readrops.app.R
import com.readrops.app.account.selection.adaptiveIconPainterResource
import com.readrops.app.util.components.SelectableIconText
import com.readrops.app.util.theme.LargeSpacer
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.ShortSpacer
import com.readrops.app.util.theme.spacing

object MoreTab : Tab {

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 4u,
            title = "More"
        )


    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            LargeSpacer()

            Image(
                painter = adaptiveIconPainterResource(id = R.mipmap.ic_launcher_round),
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )

            MediumSpacer()

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge
            )

            ShortSpacer()

            Text(
                text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.labelLarge
            )

            LargeSpacer()

            SelectableIconText(
                icon = painterResource(id = R.drawable.ic_settings),
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.titleMedium,
                spacing = MaterialTheme.spacing.mediumSpacing,
                padding = MaterialTheme.spacing.mediumSpacing,
                onClick = { }
            )

            SelectableIconText(
                icon = painterResource(id = R.drawable.ic_library),
                text = stringResource(id = R.string.open_source_libraries),
                style = MaterialTheme.typography.titleMedium,
                spacing = MaterialTheme.spacing.mediumSpacing,
                padding = MaterialTheme.spacing.mediumSpacing,
                onClick = { navigator.push(AboutLibrariesScreen()) }
            )
        }
    }
}