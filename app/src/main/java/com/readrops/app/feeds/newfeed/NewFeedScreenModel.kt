package com.readrops.app.feeds.newfeed

import android.content.Context
import android.content.SharedPreferences
import android.util.Patterns
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.api.localfeed.LocalRSSDataSource
import com.readrops.api.services.Credentials
import com.readrops.api.utils.ApiUtils
import com.readrops.api.utils.AuthInterceptor
import com.readrops.api.utils.HtmlParser
import com.readrops.app.R
import com.readrops.app.repositories.BaseRepository
import com.readrops.app.util.accounterror.AccountError
import com.readrops.app.util.components.TextFieldError
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.account.Account
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

class NewFeedScreenModel(
    private val database: Database,
    private val dataSource: LocalRSSDataSource,
    private val context: Context,
    url: String?,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : StateScreenModel<State>(State(url = url.orEmpty())), KoinComponent {

    private val selectedAccountState = MutableStateFlow(state.value.selectedAccount)

    private lateinit var accountError: AccountError

    init {
        screenModelScope.launch(dispatcher) {
            database.accountDao()
                .selectAllAccounts()
                .map { it.filter { account -> account.config.canCreateFeed } }
                .collect { accounts ->
                    val selectedAccount = accounts.find { it.isCurrentAccount }
                        ?: accounts.first()

                    accountError = AccountError.from(selectedAccount, context)
                    selectedAccountState.update { selectedAccount }

                    mutableState.update { newFeedState ->
                        newFeedState.copy(
                            accounts = accounts,
                            selectedAccount = selectedAccount
                        )
                    }
                }
        }

        screenModelScope.launch(dispatcher) {
            selectedAccountState.collect { selectedAccount ->
                if (selectedAccount != null) {
                    val folders = if (selectedAccount.config.addNoFolder) {
                        database.folderDao().selectFolders(selectedAccount.id).first() +
                                Folder(name = context.resources.getString(R.string.no_folder))
                    } else {
                        database.folderDao().selectFolders(selectedAccount.id).first()
                    }

                    val newParsingResults = mutableState.value.parsingResults.map {
                        it.copy(folder = folders.firstOrNull())
                    }

                    mutableState.update {
                        it.copy(
                            folders = folders,
                            selectedFolder = folders.firstOrNull(),
                            parsingResults = newParsingResults
                        )
                    }
                }
            }
        }
    }

    fun validate() {
        val url = mutableState.value.actualUrl

        if (state.value.selectedResultsCount > 0) {
            mutableState.update {
                it.copy(
                    error = null,
                    isLoading = true,
                    parsingResults = state.value.parsingResults.map { parsingResult ->
                        parsingResult.copy(error = null)
                    }
                )
            }

            screenModelScope.launch(dispatcher) {
                insertFeeds(state.value.parsingResults
                    .filter { it.isSelected }
                    .map {
                        Feed(
                            url = it.url,
                            folderId = it.folderId,
                            remoteFolderId = it.folder?.remoteId
                        )
                    })
            }
        } else {
            when {
                url.isEmpty() -> {
                    mutableState.update {
                        it.copy(urlError = TextFieldError.EmptyField)
                    }
                    return
                }

                !Patterns.WEB_URL.matcher(url).matches() -> {
                    mutableState.update {
                        it.copy(urlError = TextFieldError.BadUrl)
                    }
                    return
                }

                else -> loadFeeds()
            }
        }
    }

    private fun loadFeeds() {
        screenModelScope.launch(dispatcher) {
            mutableState.update { it.copy(error = null, isLoading = true) }
            val url = state.value.actualUrl

            try {
                if (dataSource.isUrlRSSResource(url)) {
                    insertFeeds(
                        listOf(
                            Feed(
                                url = url,
                                folderId = state.value.folderId,
                                remoteFolderId = state.value.selectedFolder?.remoteId
                            )
                        )
                    )
                } else {
                    val rssUrls = HtmlParser.getFeedLink(url, get())

                    when {
                        rssUrls.isEmpty() -> mutableState.update {
                            it.copy(urlError = TextFieldError.NoRSSFeed, isLoading = false)
                        }

                        rssUrls.size == 1 -> insertFeeds(
                            listOf(
                                Feed(
                                    url = rssUrls.first().url,
                                    folderId = state.value.folderId,
                                    remoteFolderId = state.value.selectedFolder?.remoteId
                                )
                            )
                        )

                        else -> {
                            val parsingResults = rssUrls.map {
                                ParsingResultState(
                                    url = it.url,
                                    label = it.label,
                                    isSelected = true,
                                    folder = state.value.selectedFolder,
                                    isExpanded = false
                                )
                            }

                            mutableState.update {
                                it.copy(
                                    parsingResults = parsingResults,
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                mutableState.update {
                    it.copy(
                        error = accountError.newFeedMessage(e),
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun insertFeeds(feeds: List<Feed>) {
        val selectedAccount = mutableState.value.selectedAccount

        if (selectedAccount != null && !selectedAccount.isLocal) {
            get<SharedPreferences>().apply {
                selectedAccount.login = getString(selectedAccount.loginKey, null)
                selectedAccount.password = getString(selectedAccount.passwordKey, null)
            }

            get<AuthInterceptor>().apply {
                credentials = Credentials.toCredentials(selectedAccount)
            }
        }

        val repository = get<BaseRepository> { parametersOf(selectedAccount) }

        val errors = repository.insertNewFeeds(
            newFeeds = feeds,
            onUpdate = {}
        )

        if (errors.isEmpty()) {
            mutableState.update { it.copy(popScreen = true) }
        } else {
            if (state.value.selectedResultsCount > 0) {
                val newParsingResults = state.value.parsingResults.map { parsingResult ->
                    val feed = errors.keys.find { feed -> feed.url == parsingResult.url }

                    if (feed != null) {
                        val error = errors[feed]
                        parsingResult.copy(error = accountError.newFeedMessage(error!!))
                    } else {
                        parsingResult
                    }
                }

                mutableState.update {
                    it.copy(
                        parsingResults = newParsingResults,
                        isLoading = false
                    )
                }
            } else {
                mutableState.update {
                    it.copy(
                        error = accountError.newFeedMessage(errors.values.first()),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateUrl(url: String) = mutableState.update { it.copy(url = url, urlError = null) }

    fun updateAccountDropDownExpandStatus(isExpanded: Boolean) =
        mutableState.update { it.copy(isAccountDropdownExpanded = isExpanded) }

    fun updateSelectedAccount(account: Account) {
        mutableState.update {
            it.copy(
                selectedAccount = account,
                isAccountDropdownExpanded = false
            )
        }

        selectedAccountState.update { account }
    }

    fun updateFolderDropdownExpandStatus(isExpanded: Boolean) =
        mutableState.update { it.copy(isFoldersDropdownExpanded = isExpanded) }

    fun updateSelectedFolder(folder: Folder) {
        val newParsingResults = mutableState.value.parsingResults.map {
            it.copy(folder = folder)
        }

        mutableState.update {
            it.copy(
                selectedFolder = folder,
                isFoldersDropdownExpanded = false,
                parsingResults = newParsingResults
            )
        }
    }

    fun updateParsingResultCheckedState(parsingResult: ParsingResultState) {
        val newList = mutableState.value.parsingResults.map {
            if (it == parsingResult) {
                parsingResult.copy(isSelected = !parsingResult.isSelected)
            } else {
                it
            }
        }

        mutableState.update { it.copy(parsingResults = newList) }
    }

    fun updateParsingResultExpandedState(parsingResult: ParsingResultState, isExpanded: Boolean) {
        val newList = mutableState.value.parsingResults.map {
            if (it == parsingResult) {
                parsingResult.copy(isExpanded = isExpanded)
            } else {
                it
            }
        }

        mutableState.update { it.copy(parsingResults = newList) }
    }

    fun updateParsingResultFolder(parsingResult: ParsingResultState, folder: Folder) {
        val newList = mutableState.value.parsingResults.map {
            if (it == parsingResult) {
                parsingResult.copy(folder = folder, isExpanded = false)
            } else {
                it
            }
        }

        mutableState.update { it.copy(parsingResults = newList) }
    }
}

data class State(
    private val url: String = "",
    val selectedAccount: Account? = null,
    val selectedFolder: Folder? = null,
    val accounts: List<Account> = listOf(),
    val folders: List<Folder> = listOf(),
    val isAccountDropdownExpanded: Boolean = false,
    val isFoldersDropdownExpanded: Boolean = false,
    val urlError: TextFieldError? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val popScreen: Boolean = false,
    val parsingResults: List<ParsingResultState> = listOf()
) {
    val isURLError: Boolean get() = urlError != null

    val selectedResultsCount: Int get() = parsingResults.count { it.isSelected }

    val folderId: Int? get() = selectedFolder?.id.takeUnless { it == 0 }

    /**
     * Handles known special cases where RSS source can not be deduced via standard methods but
     * methods to deduce it is known. Currently used to deduce RSS feeds from Youtube playlists
     */
    val actualUrl: String get() = ApiUtils.handleRssSpecialCases(url)
}

data class ParsingResultState(
    val url: String,
    val label: String?,
    val isSelected: Boolean,
    val folder: Folder?,
    val isExpanded: Boolean,
    val error: String? = null
) {
    val folderId: Int? get() = folder?.id.takeUnless { it == 0 }
}