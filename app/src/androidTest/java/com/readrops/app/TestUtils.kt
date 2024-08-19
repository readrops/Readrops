package com.readrops.app

import java.io.InputStream

object TestUtils {

    fun loadResource(path: String): InputStream =
        javaClass.classLoader?.getResourceAsStream(path)!!
}