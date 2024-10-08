package com.readrops.app.account

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.readrops.api.utils.ApiUtils
import com.readrops.app.R
import com.readrops.app.account.credentials.AccountCredentialsScreen
import com.readrops.app.account.credentials.AccountCredentialsScreenMode
import com.readrops.app.account.selection.AccountSelectionDialog
import com.readrops.app.account.selection.AccountSelectionScreen
import com.readrops.app.account.selection.adaptiveIconPainterResource
import com.readrops.app.notifications.NotificationsScreen
import com.readrops.app.repositories.ErrorResult
import com.readrops.app.timelime.ErrorListDialog
import com.readrops.app.util.components.SelectableIconText
import com.readrops.app.util.components.SelectableImageText
import com.readrops.app.util.components.ThreeDotsMenu
import com.readrops.app.util.components.dialog.ErrorDialog
import com.readrops.app.util.components.dialog.TextFieldDialog
import com.readrops.app.util.components.dialog.TwoChoicesDialog
import com.readrops.app.util.theme.LargeSpacer
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.VeryShortSpacer
import com.readrops.app.util.theme.spacing
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType

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
            screenModel.resetCloseHome()
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
                    message = context.getString(R.string.opml_export_success), 
                    actionLabel = context.resources.getString(R.string.open)
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

        AccountDialogs(
            state = state,
            screenModel = screenModel
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.account)) }
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = MaterialTheme.spacing.mediumSpacing)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = adaptiveIconPainterResource(id = state.account.type!!.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )

                        MediumSpacer()

                        Column {
                            Text(
                                text = state.account.name!!,
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (state.account.displayedName != null) {
                                VeryShortSpacer()

                                Text(
                                    text = state.account.displayedName!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (state.account.isLocal) {
                        ThreeDotsMenu(
                            items = mapOf(1 to stringResource(id = R.string.rename_account)),
                            onItemClick = {
                                screenModel.openDialog(DialogState.RenameAccount(state.account.name!!))
                            },
                        )
                    }
                }

                LargeSpacer()

                if (!state.account.isLocal) {
                    SelectableIconText(
                        icon = painterResource(id = R.drawable.ic_person),
                        text = stringResource(R.string.credentials),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
                        spacing = MaterialTheme.spacing.largeSpacing,
                        padding = MaterialTheme.spacing.mediumSpacing,
                        tint = MaterialTheme.colorScheme.primary,
                        iconSize = 24.dp,
                        onClick = {
                            navigator.push(
                                AccountCredentialsScreen(
                                    state.account,
                                    AccountCredentialsScreenMode.EDIT_CREDENTIALS
                                )
                            )
                        }
                    )
                }

                SelectableIconText(
                    icon = painterResource(id = R.drawable.ic_notifications),
                    text = stringResource(R.string.notifications),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
                    spacing = MaterialTheme.spacing.largeSpacing,
                    padding = MaterialTheme.spacing.mediumSpacing,
                    tint = MaterialTheme.colorScheme.primary,
                    iconSize = 24.dp,
                    onClick = { navigator.push(NotificationsScreen(state.account)) }
                )

                if (state.account.isLocal) {
                    SelectableIconText(
                        icon = painterResource(id = R.drawable.ic_import_export),
                        text = stringResource(R.string.opml_import_export),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
                        spacing = MaterialTheme.spacing.largeSpacing,
                        padding = MaterialTheme.spacing.mediumSpacing,
                        tint = MaterialTheme.colorScheme.primary,
                        iconSize = 24.dp,
                        onClick = { screenModel.openDialog(DialogState.OPMLChoice) }
                    )
                }

                SelectableIconText(
                    icon = rememberVectorPainter(image = Icons.Default.AccountCircle),
                    text = stringResource(R.string.delete_account),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
                    spacing = MaterialTheme.spacing.largeSpacing,
                    padding = MaterialTheme.spacing.mediumSpacing,
                    color = MaterialTheme.colorScheme.error,
                    tint = MaterialTheme.colorScheme.error,
                    iconSize = 24.dp,
                    onClick = { screenModel.openDialog(DialogState.DeleteAccount) }
                )

                if (state.accounts.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(MaterialTheme.spacing.mediumSpacing)
                    )

                    Text(
                        text = stringResource(id = R.string.other_accounts),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.mediumSpacing)
                    )

                    VeryShortSpacer()

                    for (account in state.accounts) {
                        SelectableImageText(
                            image = adaptiveIconPainterResource(id = account.type!!.iconRes),
                            text = account.name!!,
                            style = MaterialTheme.typography.titleMedium,
                            padding = MaterialTheme.spacing.mediumSpacing,
                            spacing = MaterialTheme.spacing.mediumSpacing,
                            imageSize = 24.dp,
                            onClick = { screenModel.updateCurrentAccount(account) }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun AccountDialogs(state: AccountState, screenModel: AccountScreenModel) {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow

        val opmlImportLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                uri?.let { screenModel.parseOPMLFile(uri, context) }
            }

        val opmlExportLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/xml")) { uri ->
                uri?.let { screenModel.exportOPMLFile(uri, context) }
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

                        if (accountType == AccountType.LOCAL) {
                            screenModel.createLocalAccount()
                        } else {
                            val account = Account(
                                type = accountType,
                                name = context.resources.getString(accountType.nameRes)
                            )
                            navigator.push(
                                AccountCredentialsScreen(
                                    account,
                                    AccountCredentialsScreenMode.NEW_CREDENTIALS
                                )
                            )
                        }

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
                    errorResult = dialog.errorResult as ErrorResult, // cast needed by assembleRelease
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

            is DialogState.RenameAccount -> {
                TextFieldDialog(
                    title = stringResource(id = R.string.rename_account),
                    icon = painterResource(id = R.drawable.ic_person),
                    label = stringResource(id = R.string.name),
                    state = state.renameAccountState,
                    onValueChange = { screenModel.setAccountRenameStateName(it) },
                    onValidate = { screenModel.renameAccount() },
                    onDismiss = { screenModel.closeDialog() }
                )
            }

            else -> {}
        }
    }
}