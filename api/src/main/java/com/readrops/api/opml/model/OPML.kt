package com.readrops.api.opml.model

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Order
import org.simpleframework.xml.Root

@Order(elements = ["head", "body"])
@Root(name = "opml", strict = false)
data class OPML(@field:Attribute(required = true) var version: String?,
                @field:Element(required = false) var head: Head?,
                @field:Element(required = true) var body: Body?) {

    /**
     * empty constructor required by SimpleXML
     */
    constructor() : this(null, null, null)

}