package com.readrops.app.account

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.core.net.toFile
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.api.opml.OPMLParser
import com.readrops.app.base.TabScreenModel
import com.readrops.app.repositories.ErrorResult
import com.readrops.app.repositories.GetFoldersWithFeeds
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import com.readrops.db.filters.MainFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountScreenModel(
    private val database: Database
) : TabScreenModel(database) {

    private val _closeHome = MutableStateFlow(false)
    val closeHome = _closeHome.asStateFlow()

    private val _accountState = MutableStateFlow(AccountState())
    val accountState = _accountState.asStateFlow()

    init {
        screenModelScope.launch(Dispatchers.IO) {
            accountEvent.collect { account ->
                _accountState.update {
                    it.copy(
                        account = account
                    )
                }
            }
        }
    }

    fun openDialog(dialog: DialogState) = _accountState.update { it.copy(dialog = dialog) }

    fun closeDialog(dialog: DialogState? = null) {
        if (dialog is DialogState.ErrorList) {
            _accountState.update { it.copy(synchronizationErrors = null) }
        } else if (dialog is DialogState.Error) {
            _accountState.update { it.copy(error = null) }
        }

        _accountState.update { it.copy(dialog = null) }
    }

    fun deleteAccount() {
        screenModelScope.launch(Dispatchers.IO) {
            database.accountDao()
                .delete(currentAccount!!)

            _closeHome.update { true }
        }
    }

    fun exportOPMLFile(uri: Uri, context: Context) {
        screenModelScope.launch {
            val stream = context.contentResolver.openOutputStream(uri)
            if (stream == null) {
                _accountState.update { it.copy(error = NoSuchFileException(uri.toFile())) }
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
        screenModelScope.launch(Dispatchers.IO) {
            val foldersAndFeeds: Map<Folder?, List<Feed>>

            try {
                val stream = context.contentResolver.openInputStream(uri)
                if (stream == null) {
                    _accountState.update { it.copy(error = NoSuchFileException(uri.toFile())) }
                    return@launch
                }

                foldersAndFeeds = OPMLParser.read(stream)
            } catch (e: Exception) {
                _accountState.update { it.copy(error = e) }
                return@launch
            }

            openDialog(
                DialogState.OPMLImport(
                    currentFeed = foldersAndFeeds.values.first().first().name!!,
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
                                currentFeed = feed.name!!,
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
}

@Stable
data class AccountState(
    val account: Account = Account(accountName = "account", accountType = AccountType.LOCAL),
    val dialog: DialogState? = null,
    val synchronizationErrors: ErrorResult? = null,
    val error: Exception? = null,
    val opmlExportSuccess: Boolean = false,
    val opmlExportUri: Uri? = null,
)

sealed interface DialogState {
    object DeleteAccount : DialogState
    object NewAccount : DialogState
    data class OPMLImport(val currentFeed: String, val feedCount: Int, val feedMax: Int) :
        DialogState

    data class ErrorList(val errorResult: ErrorResult) : DialogState
    data class Error(val exception: Exception) : DialogState

    object OPMLChoice : DialogState
}