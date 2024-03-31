package com.readrops.app.compose.account

import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.app.compose.base.TabScreenModel
import com.readrops.db.Database
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    fun closeDialog() = _accountState.update { it.copy(dialog = null) }

    fun deleteAccount() {
        screenModelScope.launch(Dispatchers.IO) {
            database.newAccountDao()
                .delete(currentAccount!!)

            _closeHome.update { true }
        }
    }
}

data class AccountState(
    val account: Account = Account(accountName = "account", accountType = AccountType.LOCAL),
    val dialog: DialogState? = null,
)

sealed interface DialogState {
    object DeleteAccount : DialogState
    object NewAccount : DialogState
}