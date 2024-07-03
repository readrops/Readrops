package com.readrops.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.graphics.toArgb
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import com.readrops.app.account.selection.AccountSelectionScreen
import com.readrops.app.account.selection.AccountSelectionScreenModel
import com.readrops.app.home.HomeScreen
import com.readrops.app.util.theme.ReadropsTheme
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
                    val navigationBarElevation = NavigationBarDefaults.Elevation

                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
                        navigationBarStyle = SystemBarStyle.light(
                            MaterialTheme.colorScheme.surfaceColorAtElevation(navigationBarElevation)
                                .toArgb(),
                            MaterialTheme.colorScheme.surfaceColorAtElevation(navigationBarElevation)
                                .toArgb()
                        )
                    )

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