package com.readrops.readropslibrary.opml.model

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "outline", strict = false)
data class Outline(@field:Attribute(required = false) var title: String?,
                   @field:Attribute(required = false) var text: String?,
                   @field:Attribute(required = false) var type: String?,
                   @field:Attribute(required = false) var xmlUrl: String?,
                   @field:Attribute(required = false) var htmlUrl: String?,
                   @field:ElementList(inline = true, required = false) var outlines: List<Outline>?) {

    constructor() : this(
            null,
            null,
            null,
            null,
            null,
            null)

    fun hasSubElements(): Boolean {
        return !outlines.isNullOrEmpty()
    }
}