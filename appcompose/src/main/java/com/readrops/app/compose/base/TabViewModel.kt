package com.readrops.app.compose.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readrops.app.compose.repositories.BaseRepository
import com.readrops.app.compose.repositories.LocalRSSRepository
import com.readrops.db.Database
import com.readrops.db.entities.account.Account
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

/**
 * Custom ViewModel for Tab screens handling account change
 */
abstract class TabViewModel(
        private val database: Database,
) : ViewModel(), KoinComponent {

    /**
     * Repository intended to be rebuilt when the current account changes
     */
    protected var repository: BaseRepository? = null

    protected var currentAccount: Account? = null

    /**
     * This method is called when the repository has been rebuilt from the new current account
     */
    abstract fun invalidate()

    init {
        viewModelScope.launch {
            database.newAccountDao()
                    .selectCurrentAccount()
                    .distinctUntilChanged()
                    .collect { account ->
                        currentAccount = account
                        repository = get(parameters = { parametersOf(account) })

                        invalidate()
                    }
        }
    }

}