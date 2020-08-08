package com.readrops.api.opml.model

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "outline", strict = false)
data class Outline(@field:Attribute(required = false) private var title: String?,
                   @field:Attribute(required = false) private var text: String?,
                   @field:Attribute(required = false) var type: String?,
                   @field:Attribute(required = false) var xmlUrl: String?,
                   @field:Attribute(required = false) var htmlUrl: String?,
                   @field:ElementList(inline = true, required = false) var outlines: List<Outline>?) {

    /**
     * empty constructor required by SimpleXML
     */
    constructor() : this(
            null,
            null,
            null,
            null,
            null,
            null)

    constructor(title: String?) : this(title, title, null, null, null, null)

    constructor(title: String?, xmlUrl: String, htmlUrl: String?) : this(title, title, "rss", xmlUrl, htmlUrl, null)

    val name: String?
        get() = title ?: text
}