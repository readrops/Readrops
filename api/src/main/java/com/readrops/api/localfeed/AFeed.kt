package com.readrops.api.localfeed

/*
 A simple class to give an abstract level to rss/atom/json feed classes
 */
abstract class AFeed {
    var etag: String? = null
    var lastModified: String? = null
}