package com.readrops.app.home

import android.content.Context
import android.content.SharedPreferences
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.api.services.Credentials
import com.readrops.api.utils.AuthInterceptor
import com.readrops.app.repositories.BaseRepository
import com.readrops.app.util.accounterror.AccountError
import com.readrops.db.Database
import com.readrops.db.entities.account.Account
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

/**
 * Custom screenModel for Tab screens handling account change
 */
abstract class TabScreenModel(
    private val database: Database,
    private val context: Context,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ScreenModel, KoinComponent {

    /**
     * Repository intended to be rebuilt when the current account changes
     */
    protected var repository: BaseRepository? = null

    protected var currentAccount: Account? = null

    protected var accountError: AccountError? = null

    private val _accountEvent = MutableSharedFlow<Account>()
    protected val accountEvent =
        _accountEvent.shareIn(scope = screenModelScope, started = SharingStarted.Eagerly)

    init {
        screenModelScope.launch(dispatcher) {
            database.accountDao()
                .selectCurrentAccount()
                .distinctUntilChanged()
                .collect { account ->
                    if (account != null) {
                        if (!account.isLocal) {
                            if (account.login == null || account.password == null) {
                                val encryptedPreferences = get<SharedPreferences>()

                                account.login =
                                    encryptedPreferences.getString(account.loginKey, null)
                                account.password =
                                    encryptedPreferences.getString(account.passwordKey, null)
                            }

                            // very important to avoid credentials conflicts between accounts
                            get<AuthInterceptor>().credentials = Credentials.toCredentials(account)
                        }

                        currentAccount = account
                        repository = get(parameters = { parametersOf(account) })
                        accountError = AccountError.from(account, context)

                        _accountEvent.emit(account)
                    }
                }
        }
    }
}