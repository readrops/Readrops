package com.readrops.app.compose.account.credentials

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.readrops.app.compose.R
import com.readrops.app.compose.util.components.AndroidScreen
import com.readrops.app.compose.util.theme.ShortSpacer
import com.readrops.app.compose.util.theme.VeryLargeSpacer
import com.readrops.app.compose.util.theme.spacing
import com.readrops.db.entities.account.AccountType
import org.koin.core.parameter.parametersOf

class AccountCredentialsScreen(
    private val accountType: AccountType
) : AndroidScreen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel =
            getScreenModel<AccountCredentialsScreenModel>(parameters = { parametersOf(accountType) })

        val state by screenModel.state.collectAsStateWithLifecycle()

        Box(
            modifier = Modifier.imePadding()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize()
                    .padding(MaterialTheme.spacing.largeSpacing)
                    .verticalScroll(rememberScrollState())
            ) {
                Image(
                    painter = painterResource(id = accountType.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )

                ShortSpacer()

                Text(
                    text = stringResource(id = accountType.typeName),
                    style = MaterialTheme.typography.headlineMedium
                )

                VeryLargeSpacer()

                OutlinedTextField(
                    value = state.name,
                    onValueChange = { screenModel.onEvent(Event.NameEvent(it)) },
                    label = { Text(text = stringResource(id = R.string.account_name)) },
                    singleLine = true,
                    isError = state.isNameError,
                    supportingText = { Text(text = state.nameError?.errorText().orEmpty()) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                ShortSpacer()

                OutlinedTextField(
                    value = state.url,
                    onValueChange = { screenModel.onEvent(Event.URLEvent(it)) },
                    label = { Text(text = stringResource(id = R.string.account_url)) },
                    singleLine = true,
                    isError = state.isUrlError,
                    supportingText = { Text(text = state.urlError?.errorText().orEmpty()) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                ShortSpacer()

                OutlinedTextField(
                    value = state.login,
                    onValueChange = { screenModel.onEvent(Event.LoginEvent(it)) },
                    label = { Text(text = stringResource(id = R.string.login)) },
                    singleLine = true,
                    isError = state.isLoginError,
                    supportingText = { Text(text = state.passwordError?.errorText().orEmpty()) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                ShortSpacer()

                OutlinedTextField(
                    value = state.password,
                    onValueChange = { screenModel.onEvent(Event.PasswordEvent(it)) },
                    label = { Text(text = stringResource(id = R.string.password)) },
                    trailingIcon = {
                        IconButton(
                            onClick = { screenModel.setPasswordVisibility(!state.isPasswordVisible) }
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (state.isPasswordVisible)
                                        R.drawable.ic_visible_off
                                    else R.drawable.ic_visible
                                ),
                                contentDescription = null
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (state.isPasswordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    isError = state.isPasswordError,
                    supportingText = { Text(text = state.passwordError?.errorText().orEmpty()) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                ShortSpacer()

                Button(
                    onClick = { screenModel.login() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isLoginStarted) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(text = stringResource(id = R.string.validate))
                    }
                }
            }
        }
    }
}
