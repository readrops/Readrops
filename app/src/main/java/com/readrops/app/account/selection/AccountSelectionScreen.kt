package com.readrops.app.account.selection

import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.readrops.api.utils.ApiUtils
import com.readrops.app.BuildConfig
import com.readrops.app.MainActivity
import com.readrops.app.R
import com.readrops.app.account.credentials.AccountCredentialsScreen
import com.readrops.app.account.credentials.AccountCredentialsScreenMode
import com.readrops.app.account.dialog.AccountWarningDialog
import com.readrops.app.account.dialog.OPMLImportProgressDialog
import com.readrops.app.home.HomeScreen
import com.readrops.app.util.ErrorMessage
import com.readrops.app.util.components.AndroidScreen
import com.readrops.app.util.components.SelectableImageText
import com.readrops.app.util.theme.LargeSpacer
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.ShortSpacer
import com.readrops.app.util.theme.spacing
import com.readrops.db.entities.account.ACCOUNT_APIS
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType

class AccountSelectionScreen : AndroidScreen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current

        val screenModel = koinScreenModel<AccountSelectionScreenModel>()
        val state by screenModel.state.collectAsStateWithLifecycle()

        val opmlImportLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                uri?.let { screenModel.parseOPMLFile(uri, context) }
            }

        val snackbarHostState = remember { SnackbarHostState() }

        // remove splash screen when opening the app with no account available
        LaunchedEffect(Unit) {
            (context as MainActivity).ready = true
        }

        LaunchedEffect(state.exception) {
            if (state.exception != null) {
                snackbarHostState.showSnackbar(ErrorMessage.get(state.exception!!, context))
                screenModel.resetException()
            }
        }

        when (state.navigation) {
            is Navigation.HomeScreen -> {
                // using replace makes the app crash due to a screen key conflict
                navigator.replaceAll(HomeScreen)
            }

            is Navigation.AccountCredentialsScreen -> {
                val type = (state.navigation as Navigation.AccountCredentialsScreen).type
                val account = Account(
                    type = type,
                    name = stringResource(id = type.nameRes)
                )

                navigator.push(
                    AccountCredentialsScreen(account, AccountCredentialsScreenMode.NEW_CREDENTIALS)
                )
                screenModel.resetNavigation()
            }

            else -> {}
        }

        when (val dialog = state.dialog) {
            is DialogState.AccountWarning -> {
                AccountWarningDialog(
                    type = dialog.type,
                    onConfirm = {
                        screenModel.createAccount(dialog.type)
                        screenModel.closeDialog()
                    },
                    onDismiss = { screenModel.closeDialog() }
                )
            }

            is DialogState.OPMLImport -> {
                OPMLImportProgressDialog(
                    currentFeed = state.currentFeed,
                    feedCount = state.feedCount,
                    feedMax = state.feedMax
                )
            }

            else -> {}
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(MaterialTheme.spacing.mediumSpacing)
                ) {
                    Image(
                        painter = adaptiveIconPainterResource(id = R.mipmap.ic_launcher),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )

                    ShortSpacer()

                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.headlineLarge,
                    )

                    LargeSpacer()

                    Card {
                        Column {
                            Text(
                                text = stringResource(id = R.string.choose_account),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = MaterialTheme.spacing.mediumSpacing)
                            )

                            MediumSpacer()

                            Text(
                                text = stringResource(id = R.string.local),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = MaterialTheme.spacing.mediumSpacing)
                            )

                            SelectableImageText(
                                image = adaptiveIconPainterResource(id = R.mipmap.ic_launcher),
                                text = stringResource(id = AccountType.LOCAL.nameRes),
                                style = MaterialTheme.typography.bodyLarge,
                                spacing = MaterialTheme.spacing.mediumSpacing,
                                padding = MaterialTheme.spacing.mediumSpacing,
                                imageSize = 24.dp,
                                onClick = { screenModel.createAccount(AccountType.LOCAL) }
                            )

                            SelectableImageText(
                                image = adaptiveIconPainterResource(id = R.mipmap.ic_launcher),
                                text = stringResource(id = R.string.opml_import),
                                style = MaterialTheme.typography.bodyLarge,
                                spacing = MaterialTheme.spacing.mediumSpacing,
                                padding = MaterialTheme.spacing.mediumSpacing,
                                imageSize = 24.dp,
                                onClick = { opmlImportLauncher.launch(ApiUtils.OPML_MIMETYPES.toTypedArray()) }
                            )

                            MediumSpacer()

                            Text(
                                text = stringResource(R.string.external),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = MaterialTheme.spacing.mediumSpacing)
                            )

                            listOf(AccountType.FRESHRSS, AccountType.NEXTCLOUD_NEWS)
                                .forEach { accountType ->
                                    SelectableImageText(
                                        image = adaptiveIconPainterResource(id = accountType.iconRes),
                                        text = stringResource(id = accountType.nameRes),
                                        style = MaterialTheme.typography.bodyLarge,
                                        imageSize = 24.dp,
                                        spacing = MaterialTheme.spacing.mediumSpacing,
                                        padding = MaterialTheme.spacing.mediumSpacing,
                                        onClick = { screenModel.createAccount(accountType) }
                                    )
                                }

                            MediumSpacer()

                            Text(
                                text = stringResource(R.string.api),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = MaterialTheme.spacing.mediumSpacing)
                            )

                            ACCOUNT_APIS.forEach { accountType ->
                                    SelectableImageText(
                                        image = adaptiveIconPainterResource(id = accountType.iconRes),
                                        text = stringResource(id = accountType.nameRes),
                                        style = MaterialTheme.typography.bodyLarge,
                                        imageSize = 24.dp,
                                        spacing = MaterialTheme.spacing.mediumSpacing,
                                        padding = MaterialTheme.spacing.mediumSpacing,
                                        onClick = {
                                            screenModel.openDialog(
                                                DialogState.AccountWarning(accountType)
                                            )
                                        }
                                    )
                                }
                        }
                    }
                }

                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = MaterialTheme.spacing.veryShortSpacing)
                )
            }

        }

    }
}

// from https://gist.github.com/tkuenneth/ddf598663f041dc79960cda503d14448
@Composable
fun adaptiveIconPainterResource(@DrawableRes id: Int): Painter {
    val res = LocalContext.current.resources
    val theme = LocalContext.current.theme

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Android O supports adaptive icons, try loading this first (even though this is least likely to be the format).
        val adaptiveIcon = ResourcesCompat.getDrawable(res, id, theme) as? AdaptiveIconDrawable
        if (adaptiveIcon != null) {
            BitmapPainter(adaptiveIcon.toBitmap().asImageBitmap())
        } else {
            // We couldn't load the drawable as an Adaptive Icon, just use painterResource
            painterResource(id)
        }
    } else {
        // We're not on Android O or later, just use painterResource
        painterResource(id)
    }
}