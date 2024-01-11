package com.readrops.app.compose

import java.io.InputStream

object TestUtils {

    fun loadResource(path: String): InputStream =
        javaClass.classLoader?.getResourceAsStream(path)!!
}