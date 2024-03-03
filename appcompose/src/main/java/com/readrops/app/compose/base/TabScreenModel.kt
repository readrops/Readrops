package com.readrops.app.compose.base

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.app.compose.repositories.BaseRepository
import com.readrops.db.Database
import com.readrops.db.entities.account.Account
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

/**
 * Custom ViewModel for Tab screens handling account change
 */
abstract class TabScreenModel(
    private val database: Database,
) : ScreenModel, KoinComponent {

    /**
     * Repository intended to be rebuilt when the current account changes
     */
    protected var repository: BaseRepository? = null

    protected var currentAccount: Account? = null

    protected val accountEvent = MutableSharedFlow<Account>()

    init {
        screenModelScope.launch {
            database.newAccountDao()
                .selectCurrentAccount()
                .distinctUntilChanged()
                .collect { account ->
                    if (account != null) {
                        currentAccount = account
                        repository = get(parameters = { parametersOf(account) })

                        accountEvent.emit(account)
                    }
                }
        }
    }

}