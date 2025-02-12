package com.readrops.app.util

import androidx.annotation.VisibleForTesting
import com.readrops.db.entities.Item
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.extension.AbstractExtension
import io.pebbletemplates.pebble.extension.Filter
import io.pebbletemplates.pebble.loader.StringLoader
import io.pebbletemplates.pebble.template.EvaluationContext
import io.pebbletemplates.pebble.template.PebbleTemplate
import java.io.StringWriter

@VisibleForTesting
class RemoveAuthorFilter(): Filter {
    override fun apply(
        input: Any?,
        args: Map<String?, Any?>?,
        self: PebbleTemplate?,
        context: EvaluationContext?,
        lineNumber: Int
    ): Any? {
        val author = context?.getVariable("author")?.toString()
        if(input == null || author.isNullOrBlank()) return input
        return filter(input.toString(), author)
    }

    override fun getArgumentNames(): List<String?>? = null

    companion object {
        /**
         * Regex that matches any character that someone may use as a separator between title
         * and author like `-`, `—`, `|`, etc., excluding the single quote (`'`) and semicolon.
         */
        private val sepToken = "[\\S&&[^\\p{L}]&&[^\\d]]"

        @VisibleForTesting()
        fun filter(title: String, author: String) = title.replace(
            "((\\s*$sepToken\\s*)($author))|(($author)(\\s*$sepToken\\s*))"
                .toRegex(RegexOption.IGNORE_CASE),
            ""
        )
    }
}

private class FilterExtension(): AbstractExtension() {
    override fun getFilters(): Map<String?, Filter?>? {
        return mapOf("remove_author" to RemoveAuthorFilter())
    }
}

class ShareIntentTextRenderer(private val item: Item) {
    val context
        get() = mapOf(
            "title" to item.title,
            "author" to item.author,
            "url" to item.link,
            "content" to item.content
        )

    private fun renderSafe(template: String) = runCatching {
        val result = StringWriter()
        renderer.getTemplate(template).evaluate(result, context)
        result.toString()
    }

    fun renderOrError(template: String) = renderSafe(template).getOrElse { it.toString() }
    fun render(template: String) = renderSafe(template).getOrDefault(item.link)

    companion object {
        private val renderer = PebbleEngine
            .Builder()
            .loader(StringLoader())
            .extension(FilterExtension())
            .newLineTrimming(false)
            .build()
    }
}