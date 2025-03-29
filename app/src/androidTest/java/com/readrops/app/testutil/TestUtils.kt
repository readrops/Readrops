package com.readrops.app.testutil

import java.io.InputStream

object TestUtils {

    fun loadResource(path: String): InputStream =
        javaClass.classLoader?.getResourceAsStream(path)!!
}