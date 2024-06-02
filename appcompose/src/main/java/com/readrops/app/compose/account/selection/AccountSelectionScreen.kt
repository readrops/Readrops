package com.readrops.app.compose.account.selection

import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.readrops.app.compose.R
import com.readrops.app.compose.account.credentials.AccountCredentialsScreen
import com.readrops.app.compose.account.credentials.AccountCredentialsScreenMode
import com.readrops.app.compose.home.HomeScreen
import com.readrops.app.compose.util.components.AndroidScreen
import com.readrops.app.compose.util.components.SelectableImageText
import com.readrops.app.compose.util.theme.LargeSpacer
import com.readrops.app.compose.util.theme.ShortSpacer
import com.readrops.app.compose.util.theme.spacing
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType

class AccountSelectionScreen : AndroidScreen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = getScreenModel<AccountSelectionScreenModel>()
        val state by screenModel.state.collectAsStateWithLifecycle()

        when (state) {
            is NavState.GoToHomeScreen -> {
                // using replace makes the app crash due to a screen key conflict
                navigator.replaceAll(HomeScreen())
            }

            is NavState.GoToAccountCredentialsScreen -> {
                val accountType = (state as NavState.GoToAccountCredentialsScreen).accountType
                val account = Account(accountType = accountType, accountName = stringResource(id = accountType.typeName))

                navigator.push(AccountCredentialsScreen(account, AccountCredentialsScreenMode.NEW_CREDENTIALS))
                screenModel.resetNavState()
            }

            else -> {}
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.spacing.mediumSpacing)
        ) {
            Image(
                painter = adaptiveIconPainterResource(id = R.mipmap.ic_launcher),
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )

            ShortSpacer()

            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineLarge
            )

            LargeSpacer()

            Card {
                Column(
                    modifier = Modifier.padding(MaterialTheme.spacing.largeSpacing)
                ) {
                    Text(
                        text = stringResource(id = R.string.choose_account),
                        style = MaterialTheme.typography.labelLarge
                    )

                    ShortSpacer()

                    AccountType.values().forEach { accountType ->
                        SelectableImageText(
                            image = adaptiveIconPainterResource(id = accountType.iconRes),
                            text = stringResource(id = accountType.typeName),
                            style = MaterialTheme.typography.titleLarge,
                            onClick = { screenModel.createAccount(accountType) },
                            spacing = MaterialTheme.spacing.shortSpacing,
                        )
                    }

                    SelectableImageText(
                        image = adaptiveIconPainterResource(id = R.mipmap.ic_launcher),
                        text = stringResource(id = R.string.opml_import),
                        style = MaterialTheme.typography.titleLarge,
                        onClick = { },
                        spacing = MaterialTheme.spacing.shortSpacing,
                    )
                }
            }
        }
    }
}

// from https://gist.github.com/tkuenneth/ddf598663f041dc79960cda503d14448
@Composable
fun adaptiveIconPainterResource(@DrawableRes id: Int): Painter {
    val res = LocalContext.current.resources
    val theme = LocalContext.current.theme

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Android O supports adaptive icons, try loading this first (even though this is least likely to be the format).
        val adaptiveIcon = ResourcesCompat.getDrawable(res, id, theme) as? AdaptiveIconDrawable
        if (adaptiveIcon != null) {
            BitmapPainter(adaptiveIcon.toBitmap().asImageBitmap())
        } else {
            // We couldn't load the drawable as an Adaptive Icon, just use painterResource
            painterResource(id)
        }
    } else {
        // We're not on Android O or later, just use painterResource
        painterResource(id)
    }
}