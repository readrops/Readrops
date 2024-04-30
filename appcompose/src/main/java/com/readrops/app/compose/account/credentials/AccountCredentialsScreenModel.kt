package com.readrops.app.compose.account.credentials

import android.util.Patterns
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.app.compose.repositories.BaseRepository
import com.readrops.app.compose.util.components.TextFieldError
import com.readrops.db.Database
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

class AccountCredentialsScreenModel(
    private val accountType: AccountType,
    private val database: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : StateScreenModel<AccountCredentialsState>(AccountCredentialsState(name = accountType.name)),
    KoinComponent {

    fun onEvent(event: Event): Unit = with(mutableState) {
        when (event) {
            is Event.LoginEvent -> update { it.copy(login = event.value, loginError = null) }
            is Event.NameEvent -> update { it.copy(name = event.value, nameError = null) }
            is Event.PasswordEvent -> update {
                it.copy(
                    password = event.value,
                    passwordError = null
                )
            }

            is Event.URLEvent -> update { it.copy(url = event.value, urlError = null) }
        }
    }

    fun setPasswordVisibility(isVisible: Boolean) {
        mutableState.update { it.copy(isPasswordVisible = isVisible) }
    }

    fun login() {
        if (validateFields()) {
            mutableState.update { it.copy(isLoginOnGoing = true) }

            with(state.value) {
                val account = Account(
                    url = url,
                    accountName = name,
                    login = login,
                    password = password,
                    accountType = accountType,
                    isCurrentAccount = true
                )

                val repository = get<BaseRepository> { parametersOf(account) }

                screenModelScope.launch(dispatcher) {
                    try {
                        repository.login(account)
                    } catch (e: Exception) {
                        mutableState.update {
                            it.copy(
                                loginException = e,
                                isLoginOnGoing = false
                            )
                        }

                        return@launch
                    }

                    database.newAccountDao().insert(account)
                    mutableState.update { it.copy(goToHomeScreen = true) }
                }
            }
        }
    }

    private fun validateFields(): Boolean = with(mutableState.value) {
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

        if (url.isNotEmpty() && !Patterns.WEB_URL.matcher(url).matches()) {
            mutableState.update { it.copy(urlError = TextFieldError.BadUrl) }
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
    val goToHomeScreen: Boolean = false,
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