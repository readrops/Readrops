package com.readrops.app.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.*
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.readrops.app.compose.account.selection.AccountSelectionScreen
import com.readrops.app.compose.account.selection.AccountSelectionViewModel
import com.readrops.app.compose.home.HomeScreen
import com.readrops.app.compose.util.theme.ReadropsTheme
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = getViewModel<AccountSelectionViewModel>()
        val accountExists = viewModel.accountExists()

        setContent {
            ReadropsTheme {
                Navigator(
                        screen = if (accountExists) HomeScreen() else AccountSelectionScreen()
                ) {
                    CurrentScreen()
                }
            }
        }
    }
}