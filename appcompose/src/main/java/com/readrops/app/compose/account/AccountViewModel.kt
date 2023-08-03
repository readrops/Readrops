package com.readrops.app.compose.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readrops.db.Database
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AccountViewModel(
        private val database: Database,
) : ViewModel() {

    fun accountExists(): Boolean {
        val accountCount = runBlocking {
            database.newAccountDao().selectAccountCount()
        }

        return accountCount > 0
    }
}