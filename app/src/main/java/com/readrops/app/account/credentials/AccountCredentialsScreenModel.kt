package com.readrops.app.account.credentials

import android.content.SharedPreferences
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.app.repositories.BaseRepository
import com.readrops.app.util.Utils
import com.readrops.app.util.components.TextFieldError
import com.readrops.db.Database
import com.readrops.db.entities.account.Account
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

class AccountCredentialsScreenModel(
    private val account: Account,
    private val mode: AccountCredentialsScreenMode,
    private val database: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : StateScreenModel<AccountCredentialsState>(AccountCredentialsState()), KoinComponent {

    init {
        if (mode == AccountCredentialsScreenMode.EDIT_CREDENTIALS) {
            mutableState.update {
                it.copy(
                    name = account.name!!,
                    url = account.url!!,
                    login = account.login!!,
                    password = account.password!!
                )
            }
        } else {
            mutableState.update { it.copy(name = account.name!!) }
        }
    }

    fun onEvent(event: Event): Unit = with(mutableState) {
        when (event) {
            is Event.LoginEvent -> update { it.copy(login = event.value, loginError = null) }
            is Event.NameEvent -> update { it.copy(name = event.value, nameError = null) }
            is Event.PasswordEvent -> update { it.copy(password = event.value, passwordError = null) }
            is Event.URLEvent -> update { it.copy(url = event.value, urlError = null) }
        }
    }

    fun setPasswordVisibility(isVisible: Boolean) {
        mutableState.update { it.copy(isPasswordVisible = isVisible) }
    }

    fun login() {
        screenModelScope.launch(dispatcher) {
            if (validateFields()) {
                mutableState.update { it.copy(isLoginOnGoing = true) }

                with(state.value) {
                    val normalizedUrl = Utils.normalizeUrl(url)

                    val newAccount = account.copy(
                        url = normalizedUrl,
                        name = name,
                        login = login,
                        password = password,
                        type = account.type,
                        isCurrentAccount = true
                    )

                    try {
                        get<BaseRepository> { parametersOf(newAccount) }
                            .login(newAccount)
                    } catch (e: Exception) {
                        mutableState.update {
                            it.copy(
                                loginException = e,
                                isLoginOnGoing = false
                            )
                        }

                        return@launch
                    }

                    if (mode == AccountCredentialsScreenMode.NEW_CREDENTIALS) {
                        newAccount.id = database.accountDao().insert(newAccount).toInt()

                        get<SharedPreferences>().edit()
                            .putString(newAccount.loginKey, newAccount.login)
                            .putString(newAccount.passwordKey, newAccount.password)
                            .apply()
                    } else {
                        database.accountDao().update(newAccount)
                    }

                    mutableState.update { it.copy(exitScreen = true) }
                }
            }
        }
    }

    private fun validateFields(): Boolean = with(mutableState.value) {
        mutableState.update { it.copy(loginException = null) }

        var validate = true

        if (url.isEmpty()) {
            mutableState.update { it.copy(urlError = TextFieldError.EmptyField) }
            validate = false
        }

        if (name.isEmpty()) {
            mutableState.update { it.copy(nameError = TextFieldError.EmptyField) }
            validate = false
        }

        if (login.isEmpty()) {
            mutableState.update { it.copy(loginError = TextFieldError.EmptyField) }
            validate = false
        }

        if (password.isEmpty()) {
            mutableState.update { it.copy(passwordError = TextFieldError.EmptyField) }
            validate = false
        }

        return validate
    }
}

data class AccountCredentialsState(
    val url: String = "https://",
    val urlError: TextFieldError? = null,
    val name: String = "",
    val nameError: TextFieldError? = null,
    val login: String = "",
    val loginError: TextFieldError? = null,
    val password: String = "",
    val passwordError: TextFieldError? = null,
    val isPasswordVisible: Boolean = false,
    val isLoginOnGoing: Boolean = false,
    val exitScreen: Boolean = false,
    val loginException: Exception? = null
) {
    val isUrlError = urlError != null

    val isNameError = nameError != null

    val isLoginError = loginError != null

    val isPasswordError = passwordError != null
}

sealed class Event(val value: String) {
    class URLEvent(value: String) : Event(value)
    class NameEvent(value: String) : Event(value)
    class LoginEvent(value: String) : Event(value)
    class PasswordEvent(value: String) : Event(value)
}