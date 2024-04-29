package com.readrops.app.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import com.readrops.app.compose.account.selection.AccountSelectionScreen
import com.readrops.app.compose.account.selection.AccountSelectionScreenModel
import com.readrops.app.compose.home.HomeScreen
import com.readrops.app.compose.util.theme.ReadropsTheme
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class MainActivity : ComponentActivity(), KoinComponent {

    @OptIn(KoinExperimentalAPI::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val screenModel = get<AccountSelectionScreenModel>()
        val accountExists = screenModel.accountExists()

        setContent {
            KoinAndroidContext {
                ReadropsTheme {
                    Navigator(
                        screen = if (accountExists) HomeScreen() else AccountSelectionScreen(),
                        disposeBehavior = NavigatorDisposeBehavior(
                            // prevent screenModels being recreated when opening a screen from a tab
                            disposeNestedNavigators = false
                        )
                    ) {
                        CurrentScreen()
                    }
                }
            }
        }
    }
}