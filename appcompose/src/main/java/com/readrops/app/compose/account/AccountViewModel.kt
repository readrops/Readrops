package com.readrops.app.compose.account

import androidx.lifecycle.viewModelScope
import com.readrops.app.compose.base.TabViewModel
import com.readrops.db.Database
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountViewModel(
    private val database: Database
) : TabViewModel(database)  {

    private val _closeHome = MutableStateFlow(false)
    val closeHome = _closeHome.asStateFlow()

    private val _accountState = MutableStateFlow(AccountState())
    val accountState = _accountState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            accountEvent.collect { account ->
                _accountState.update {
                    it.copy(
                        account = account
                    )
                }
            }
        }
    }


    fun deleteAccount() {
        viewModelScope.launch(Dispatchers.IO) {
            database.newAccountDao()
                    .deleteAllAccounts()

            _closeHome.update { true }
        }
    }
}

data class AccountState(
    val account: Account = Account(accountName = "account", accountType = AccountType.LOCAL),
    val dialog: Unit = Unit,
)