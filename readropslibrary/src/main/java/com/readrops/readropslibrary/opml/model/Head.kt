package com.readrops.readropslibrary.opml.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "head", strict = false)
data class Head(@field:Element(required = false) var title: String?) {

    constructor() : this(null)
}