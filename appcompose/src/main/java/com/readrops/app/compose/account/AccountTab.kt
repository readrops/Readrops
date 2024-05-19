package com.readrops.app.compose.account

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.readrops.api.utils.ApiUtils
import com.readrops.app.compose.R
import com.readrops.app.compose.account.credentials.AccountCredentialsScreen
import com.readrops.app.compose.account.selection.AccountSelectionDialog
import com.readrops.app.compose.account.selection.AccountSelectionScreen
import com.readrops.app.compose.account.selection.adaptiveIconPainterResource
import com.readrops.app.compose.timelime.ErrorListDialog
import com.readrops.app.compose.util.components.ErrorDialog
import com.readrops.app.compose.util.components.SelectableIconText
import com.readrops.app.compose.util.components.TwoChoicesDialog
import com.readrops.app.compose.util.theme.LargeSpacer
import com.readrops.app.compose.util.theme.MediumSpacer
import com.readrops.app.compose.util.theme.spacing

object AccountTab : Tab {

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 3u,
            title = stringResource(R.string.account)
        )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val screenModel = getScreenModel<AccountScreenModel>()

        val closeHome by screenModel.closeHome.collectAsStateWithLifecycle()
        val state by screenModel.accountState.collectAsStateWithLifecycle()

        val snackbarHostState = remember { SnackbarHostState() }

        if (closeHome) {
            navigator.replaceAll(AccountSelectionScreen())
        }

        val opmlImportLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                uri?.let { screenModel.parseOPMLFile(uri, context) }
            }

        val opmlExportLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/xml")) { uri ->
                uri?.let { screenModel.exportOPMLFile(uri, context) }
            }

        LaunchedEffect(state.error) {
            if (state.error != null) {
                val action = snackbarHostState.showSnackbar(
                    message = context.resources.getQuantityString(
                        R.plurals.error_occurred,
                        1
                    ),
                    actionLabel = context.getString(R.string.details),
                    duration = SnackbarDuration.Short
                )

                if (action == SnackbarResult.ActionPerformed) {
                    screenModel.openDialog(DialogState.Error(state.error!!))
                } else {
                    screenModel.closeDialog(DialogState.Error(state.error!!))
                }
            }
        }

        LaunchedEffect(state.synchronizationErrors) {
            if (state.synchronizationErrors != null) {
                val action = snackbarHostState.showSnackbar(
                    message = context.resources.getQuantityString(
                        R.plurals.error_occurred,
                        state.synchronizationErrors!!.size
                    ),
                    actionLabel = context.getString(R.string.details),
                    duration = SnackbarDuration.Short
                )

                if (action == SnackbarResult.ActionPerformed) {
                    screenModel.openDialog(DialogState.ErrorList(state.synchronizationErrors!!))
                } else {
                    screenModel.closeDialog(DialogState.ErrorList(state.synchronizationErrors!!))
                }
            }
        }

        LaunchedEffect(state.opmlExportSuccess) {
            if (state.opmlExportSuccess) {
                val action = snackbarHostState.showSnackbar(
                    message = "OPML export success",
                    actionLabel = "Open file"
                )

                if (action == SnackbarResult.ActionPerformed) {
                    Intent().apply {
                        this.action = Intent.ACTION_VIEW
                        setDataAndType(state.opmlExportUri, "text/xml")
                    }.also {
                        context.startActivity(Intent.createChooser(it, null))
                    }

                    screenModel.resetOPMLState()
                } else {
                    screenModel.resetOPMLState()
                }
            }
        }

        when (val dialog = state.dialog) {
            is DialogState.DeleteAccount -> {
                TwoChoicesDialog(
                    title = stringResource(R.string.delete_account),
                    text = stringResource(R.string.delete_account_question),
                    icon = rememberVectorPainter(image = Icons.Default.Delete),
                    confirmText = stringResource(R.string.delete),
                    dismissText = stringResource(R.string.cancel),
                    onDismiss = { screenModel.closeDialog() },
                    onConfirm = {
                        screenModel.closeDialog()
                        screenModel.deleteAccount()
                    }
                )
            }

            is DialogState.NewAccount -> {
                AccountSelectionDialog(
                    onDismiss = { screenModel.closeDialog() },
                    onValidate = { accountType ->
                        screenModel.closeDialog()
                        navigator.push(AccountCredentialsScreen(accountType))
                    }
                )
            }

            is DialogState.OPMLImport -> {
                OPMLImportProgressDialog(
                    currentFeed = dialog.currentFeed,
                    feedCount = dialog.feedCount,
                    feedMax = dialog.feedMax
                )
            }

            is DialogState.ErrorList -> {
                ErrorListDialog(
                    errorResult = dialog.errorResult,
                    onDismiss = { screenModel.closeDialog(dialog) }
                )
            }

            is DialogState.Error -> {
                ErrorDialog(
                    exception = dialog.exception,
                    onDismiss = { screenModel.closeDialog(dialog) }
                )
            }

            is DialogState.OPMLChoice -> {
                OPMLChoiceDialog(
                    onChoice = {
                        if (it == OPML.IMPORT) {
                            opmlImportLauncher.launch(ApiUtils.OPML_MIMETYPES.toTypedArray())
                        } else {
                            opmlExportLauncher.launch("subscriptions.opml")
                        }

                        screenModel.closeDialog()
                    },
                    onDismiss = { screenModel.closeDialog() }
                )

            }

            else -> {}
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.account)) },
                    actions = {
                        IconButton(
                            onClick = { }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { screenModel.openDialog(DialogState.NewAccount) }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add_account),
                        contentDescription = null
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = adaptiveIconPainterResource(id = R.drawable.ic_freshrss),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )

                    MediumSpacer()

                    Text(
                        text = state.account.accountName!!,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                LargeSpacer()

                SelectableIconText(
                    icon = painterResource(id = R.drawable.ic_add_account),
                    text = stringResource(R.string.credentials),
                    style = MaterialTheme.typography.titleMedium,
                    spacing = MaterialTheme.spacing.mediumSpacing,
                    padding = MaterialTheme.spacing.mediumSpacing,
                    onClick = { }
                )

                SelectableIconText(
                    icon = painterResource(id = R.drawable.ic_notifications),
                    text = stringResource(R.string.notifications),
                    style = MaterialTheme.typography.titleMedium,
                    spacing = MaterialTheme.spacing.mediumSpacing,
                    padding = MaterialTheme.spacing.mediumSpacing,
                    onClick = { }
                )

                SelectableIconText(
                    icon = painterResource(id = R.drawable.ic_import_export),
                    text = stringResource(R.string.opml_import_export),
                    style = MaterialTheme.typography.titleMedium,
                    spacing = MaterialTheme.spacing.mediumSpacing,
                    padding = MaterialTheme.spacing.mediumSpacing,
                    onClick = { screenModel.openDialog(DialogState.OPMLChoice) }
                )

                SelectableIconText(
                    icon = rememberVectorPainter(image = Icons.Default.AccountCircle),
                    text = stringResource(R.string.delete_account),
                    style = MaterialTheme.typography.titleMedium,
                    spacing = MaterialTheme.spacing.mediumSpacing,
                    padding = MaterialTheme.spacing.mediumSpacing,
                    color = MaterialTheme.colorScheme.error,
                    tint = MaterialTheme.colorScheme.error,
                    onClick = { screenModel.openDialog(DialogState.DeleteAccount) }
                )
            }
        }
    }
}