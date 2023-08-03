package com.readrops.app.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.transitions.FadeTransition
import cafe.adriel.voyager.transitions.SlideTransition
import com.readrops.app.compose.account.AccountSelectionScreen
import com.readrops.app.compose.account.AccountTab
import com.readrops.app.compose.account.AccountViewModel
import com.readrops.app.compose.feeds.FeedTab
import com.readrops.app.compose.home.HomeScreen
import com.readrops.app.compose.more.MoreTab
import com.readrops.app.compose.timelime.TimelineTab
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = getViewModel<AccountViewModel>()
        val accountExists = viewModel.accountExists()

        setContent {
            ReadropsTheme {
                Navigator(
                        screen = if (accountExists) HomeScreen() else AccountSelectionScreen()
                ) { navigator ->
                    FadeTransition(navigator)
                }
            }
        }
    }
}