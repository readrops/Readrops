package com.readrops.app.compose.account.selection

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readrops.db.Database
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class AccountSelectionViewModel(
        private val database: Database,
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel(), KoinComponent {

    private val _navState = MutableStateFlow<NavState>(NavState.Idle)
    val navState = _navState.asStateFlow()

    fun accountExists(): Boolean {
        val accountCount = runBlocking {
            database.newAccountDao().selectAccountCount()
        }

        return accountCount > 0
    }

    fun createAccount(accountType: AccountType) {
        if (accountType == AccountType.LOCAL) {
            createLocalAccount()
        } else {
            _navState.update { NavState.GoToAccountCredentialsScreen(accountType) }
        }
    }

    fun resetNavState() {
        _navState.update { NavState.Idle }
    }

    private fun createLocalAccount() {
        val context = get<Context>()
        val account = Account(
                url = null,
                accountName = context.getString(AccountType.LOCAL.typeName),
                accountType = AccountType.LOCAL,
                isCurrentAccount = true
        )

        viewModelScope.launch(dispatcher) {
            database.newAccountDao().insert(account)

            _navState.update { NavState.GoToHomeScreen }
        }
    }


    sealed class NavState {
        object Idle : NavState()
        object GoToHomeScreen : NavState()
        class GoToAccountCredentialsScreen(val accountType: AccountType) : NavState()
    }
}