package com.readrops.api.opml.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "head", strict = false)
data class Head(@field:Element(required = false) var title: String?) {

    /**
     * empty constructor required by SimpleXML
     */
    constructor() : this(null)
}