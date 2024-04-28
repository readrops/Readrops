package com.readrops.app.compose.account.credentials

import android.util.Patterns
import cafe.adriel.voyager.core.model.StateScreenModel
import com.readrops.app.compose.util.components.TextFieldError
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import kotlinx.coroutines.flow.update

class AccountCredentialsScreenModel(
    private val accountType: AccountType
) : StateScreenModel<AccountCredentialsState>(AccountCredentialsState(name = accountType.name)) {

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
        if (validateFields()) {
            mutableState.update { it.copy(isLoginStarted = true) }

            with(state.value) {
                val account = Account(
                    url = url,
                    accountName = name,
                    login = login,
                    password = password,
                    accountType = accountType
                )
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
    val isLoginStarted: Boolean = false
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