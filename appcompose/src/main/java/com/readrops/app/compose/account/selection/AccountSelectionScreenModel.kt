package com.readrops.app.compose.account.selection

import android.content.Context
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.db.Database
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class AccountSelectionScreenModel(
        private val database: Database,
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : StateScreenModel<NavState>(NavState.Idle), KoinComponent {

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
            mutableState.update { NavState.GoToAccountCredentialsScreen(accountType) }
        }
    }

    fun resetNavState() {
        mutableState.update { NavState.Idle }
    }

    private fun createLocalAccount() {
        val context = get<Context>()
        val account = Account(
                url = null,
                accountName = context.getString(AccountType.LOCAL.typeName),
                accountType = AccountType.LOCAL,
                isCurrentAccount = true
        )

        screenModelScope.launch(dispatcher) {
            database.newAccountDao().insert(account)

            mutableState.update { NavState.GoToHomeScreen }
        }
    }
}

sealed class NavState {
    object Idle : NavState()
    object GoToHomeScreen : NavState()
    class GoToAccountCredentialsScreen(val accountType: AccountType) : NavState()
}