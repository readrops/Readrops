package com.readrops.app.account.selection

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.api.opml.OPMLParser
import com.readrops.app.repositories.BaseRepository
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

class AccountSelectionScreenModel(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : StateScreenModel<AccountSelectionState>(AccountSelectionState()), KoinComponent {

    fun accountExists(): Boolean {
        val accountCount = runBlocking {
            database.accountDao().selectAccountCount()
        }

        return accountCount > 0
    }

    fun createAccount(accountType: AccountType) {
        if (accountType == AccountType.LOCAL) {
            screenModelScope.launch(dispatcher) {
                createLocalAccount()
                mutableState.update { it.copy(navigation = Navigation.HomeScreen) }
            }
        } else {
            mutableState.update {
                it.copy(navigation = Navigation.AccountCredentialsScreen(accountType))
            }
        }
    }

    fun resetNavigation() {
        mutableState.update { it.copy(navigation = null) }
    }

    private suspend fun createLocalAccount(): Account {
        val context = get<Context>()
        val account = Account(
            url = null,
            name = context.getString(AccountType.LOCAL.nameRes),
            type = AccountType.LOCAL,
            isCurrentAccount = true
        )

        account.id = database.accountDao().insert(account).toInt()
        return account
    }

    fun parseOPMLFile(uri: Uri, context: Context) {
        screenModelScope.launch(dispatcher) {
            val foldersAndFeeds: Map<Folder?, List<Feed>>

            try {
                val stream = context.contentResolver.openInputStream(uri)
                if (stream == null) {
                    mutableState.update { it.copy(exception = NoSuchFileException(uri.toFile())) }
                    return@launch
                }

                foldersAndFeeds = OPMLParser.read(stream)
            } catch (e: Exception) {
                mutableState.update { it.copy(exception = e) }
                return@launch
            }

            mutableState.update {
                it.copy(
                    dialog = DialogState.OPMLImport,
                    currentFeed = foldersAndFeeds.values.first().first().name,
                    feedCount = 0,
                    feedMax = foldersAndFeeds.values.flatten().size
                )
            }

            val account = createLocalAccount()
            val repository = get<BaseRepository> { parametersOf(account) }

            repository.insertOPMLFoldersAndFeeds(
                foldersAndFeeds = foldersAndFeeds,
                onUpdate = { feed ->
                    mutableState.update {
                        it.copy(
                            currentFeed = feed.name,
                            feedCount = it.feedCount + 1
                        )
                    }
                }
            )

            mutableState.update {
                it.copy(
                    dialog = null,
                    navigation = Navigation.HomeScreen
                )
            }
        }
    }

    fun resetException() = mutableState.update { it.copy(exception = null) }

    fun openDialog(dialog: DialogState) = mutableState.update { it.copy(dialog = dialog) }

    fun closeDialog() = mutableState.update { it.copy(dialog = null) }
}

data class AccountSelectionState(
    val exception: Exception? = null,
    val currentFeed: String? = null,
    val feedCount: Int = 0,
    val feedMax: Int = 0,
    val dialog: DialogState? = null,
    val navigation: Navigation? = null
)

sealed class Navigation {
    data object HomeScreen : Navigation()
    data class AccountCredentialsScreen(val type: AccountType) : Navigation()
}

sealed interface DialogState {
    data object OPMLImport : DialogState
    data class AccountWarning(val type: AccountType) : DialogState
}