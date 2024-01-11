package com.readrops.app.compose.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readrops.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountViewModel(
    private val database: Database
) : ViewModel()  {

    private val _closeHome = MutableStateFlow(false)
    val closeHome = _closeHome.asStateFlow()


    fun deleteAccount() {
        viewModelScope.launch(Dispatchers.IO) {
            database.newAccountDao()
                    .deleteAllAccounts()

            _closeHome.update { true }
        }
    }

}