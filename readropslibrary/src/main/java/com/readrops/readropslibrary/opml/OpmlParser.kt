package com.readrops.readropslibrary.opml

import android.content.Context
import android.net.Uri
import com.readrops.readropslibrary.opml.model.Opml
import com.readrops.readropslibrary.utils.LibUtils
import io.reactivex.Single
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister

class OpmlParser {

    companion object {
        @JvmStatic
        fun parse(uri: Uri, context: Context): Single<Opml> {
            return Single.create { emitter ->
                val fileString = LibUtils.fileToString(uri, context)
                val serializer: Serializer = Persister()

                val opml: Opml = serializer.read(Opml::class.java, fileString)

                emitter.onSuccess(opml)
            }
        }
    }


}