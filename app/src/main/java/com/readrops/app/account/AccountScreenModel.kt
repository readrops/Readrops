package com.readrops.app.account

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.core.net.toFile
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.api.opml.OPMLParser
import com.readrops.app.home.TabScreenModel
import com.readrops.app.repositories.ErrorResult
import com.readrops.app.repositories.GetFoldersWithFeeds
import com.readrops.app.util.components.TextFieldError
import com.readrops.app.util.components.dialog.TextFieldDialogState
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import com.readrops.db.filters.MainFilter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.get

class AccountScreenModel(
    private val database: Database,
    context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TabScreenModel(database, context) {

    private val _closeHome = MutableStateFlow(false)
    val closeHome = _closeHome.asStateFlow()

    private val _accountState = MutableStateFlow(AccountState())
    val accountState = _accountState.asStateFlow()

    init {
        screenModelScope.launch(dispatcher) {
            accountEvent.collect { account ->
                _accountState.update {
                    it.copy(
                        account = account
                    )
                }
            }
        }

        screenModelScope.launch(dispatcher) {
            database.accountDao().selectAllAccounts()
                .map { it.filter { account -> !account.isCurrentAccount } }
                .collect { accounts ->
                    _accountState.update { it.copy(accounts = accounts) }
                }
        }
    }

    fun openDialog(dialog: DialogState) {
        if (dialog is DialogState.RenameAccount) {
            _accountState.update { it.copy(renameAccountState = TextFieldDialogState(value = dialog.name)) }
        }

        _accountState.update { it.copy(dialog = dialog) }
    }

    fun closeDialog(dialog: DialogState? = null) {
        if (dialog is DialogState.ErrorList) {
            _accountState.update { it.copy(synchronizationErrors = null) }
        } else if (dialog is DialogState.Error) {
            _accountState.update { it.copy(error = null) }
        }

        _accountState.update { it.copy(dialog = null) }
    }

    fun deleteAccount() {
        screenModelScope.launch(dispatcher) {
            database.accountDao()
                .delete(currentAccount!!)

            if (_accountState.value.accounts.isNotEmpty()) {
                database.accountDao().updateCurrentAccount(_accountState.value.accounts.first().id)
            } else {
                _closeHome.update { true }
            }
        }
    }

    fun exportOPMLFile(uri: Uri, context: Context) {
        screenModelScope.launch(dispatcher) {
            val stream = context.contentResolver.openOutputStream(uri)
            if (stream == null) {
                _accountState.update {
                    it.copy(
                        error = accountError?.genericMessage(NoSuchFileException(uri.toFile()))
                    )
                }
                return@launch
            }

            val foldersAndFeeds =
                GetFoldersWithFeeds(database).get(
                    currentAccount!!.id,
                    MainFilter.ALL,
                    currentAccount!!.config.useSeparateState
                ).first()

            OPMLParser.write(foldersAndFeeds, stream)

            _accountState.update {
                it.copy(
                    opmlExportSuccess = true,
                    opmlExportUri = uri
                )
            }
        }
    }

    fun parseOPMLFile(uri: Uri, context: Context) {
        screenModelScope.launch(dispatcher) {
            val foldersAndFeeds: Map<Folder?, List<Feed>>

            try {
                val stream = context.contentResolver.openInputStream(uri)
                if (stream == null) {
                    _accountState.update {
                        it.copy(
                            error = accountError?.genericMessage(NoSuchFileException(uri.toFile()))
                        )
                    }
                    return@launch
                }

                foldersAndFeeds = OPMLParser.read(stream)
            } catch (e: Exception) {
                _accountState.update { it.copy(error = accountError?.genericMessage(e)) }
                return@launch
            }

            openDialog(
                DialogState.OPMLImport(
                    currentFeed = foldersAndFeeds.values.first().first().name,
                    feedCount = 0,
                    feedMax = foldersAndFeeds.values.flatten().size
                )
            )

            val errors = repository?.insertOPMLFoldersAndFeeds(
                foldersAndFeeds = foldersAndFeeds,
                onUpdate = { feed ->
                    _accountState.update {
                        val dialog = (it.dialog as DialogState.OPMLImport)

                        it.copy(
                            dialog = dialog.copy(
                                currentFeed = feed.name,
                                feedCount = dialog.feedCount + 1
                            )
                        )
                    }
                }
            )

            closeDialog()

            _accountState.update {
                it.copy(synchronizationErrors = if (errors!!.isNotEmpty()) errors else null)
            }
        }
    }

    fun resetOPMLState() =
        _accountState.update { it.copy(opmlExportUri = null, opmlExportSuccess = false) }

    fun resetCloseHome() = _closeHome.update { false }

    fun updateCurrentAccount(account: Account) {
        screenModelScope.launch(dispatcher) {
            database.accountDao().updateCurrentAccount(account.id)
        }
    }

    fun createLocalAccount() {
        val context = get<Context>()
        val account = Account(
            name = context.getString(AccountType.LOCAL.nameRes),
            type = AccountType.LOCAL,
            isCurrentAccount = true
        )

        screenModelScope.launch(dispatcher) {
            database.accountDao().insert(account)
        }
    }

    fun setAccountRenameStateName(name: String) = _accountState.update {
        it.copy(
            renameAccountState = it.renameAccountState.copy(
                value = name,
                textFieldError = null
            )
        )
    }

    fun renameAccount() = with(_accountState) {
        if (value.renameAccountState.value.isEmpty()) {
            update { it.copy(renameAccountState = it.renameAccountState.copy(textFieldError = TextFieldError.EmptyField)) }
            return@with
        }

        screenModelScope.launch(dispatcher) {
            database.accountDao().renameAccount(value.account.id, value.renameAccountState.value)
            closeDialog()
        }
    }
}

@Stable
data class AccountState(
    val account: Account = Account(name = "account", type = AccountType.LOCAL),
    val dialog: DialogState? = null,
    val synchronizationErrors: ErrorResult? = null,
    val error: String? = null,
    val opmlExportSuccess: Boolean = false,
    val opmlExportUri: Uri? = null,
    val accounts: List<Account> = emptyList(),
    val renameAccountState: TextFieldDialogState = TextFieldDialogState()
)

sealed interface DialogState {
    data object DeleteAccount : DialogState
    data object NewAccount : DialogState
    data class OPMLImport(val currentFeed: String?, val feedCount: Int, val feedMax: Int) :
        DialogState

    data class ErrorList(val errorResult: ErrorResult) : DialogState
    data class Error(val error: String) : DialogState

    data object OPMLChoice : DialogState

    data class RenameAccount(val name: String) : DialogState
}