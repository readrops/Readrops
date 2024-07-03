package com.readrops.app.util.components

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey

abstract class AndroidScreen : Screen {

    override val key: ScreenKey = uniqueScreenKey
}