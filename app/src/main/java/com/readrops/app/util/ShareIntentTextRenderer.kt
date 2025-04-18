package com.readrops.app.util

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.ui.util.fastJoinToString
import com.readrops.db.entities.Item
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.extension.AbstractExtension
import io.pebbletemplates.pebble.extension.Filter
import io.pebbletemplates.pebble.loader.StringLoader
import io.pebbletemplates.pebble.template.EvaluationContext
import io.pebbletemplates.pebble.template.PebbleTemplate
import org.koin.core.component.KoinComponent
import java.io.StringWriter
import com.readrops.app.R
import org.koin.core.component.get


abstract class DocumentedFilter : Filter, KoinComponent {
    val documentation by lazy { generateDocumentation(get<Context>()) }
    protected abstract fun generateDocumentation(context: Context): String
}

@VisibleForTesting
class RemoveAuthorFilter : DocumentedFilter() {
    override fun apply(
        input: Any?,
        args: Map<String?, Any?>?,
        self: PebbleTemplate?,
        context: EvaluationContext?,
        lineNumber: Int
    ): Any? {
        val author = context?.getVariable("author")?.toString()
        if (input == null || author.isNullOrBlank()) return input
        return filter(input.toString(), author)
    }

    override fun getArgumentNames(): List<String?>? = null

    override fun generateDocumentation(context: Context) =
        context.getString(R.string.remove_author_documentation)

    companion object {
        /**
         * Regex that matches any character that someone may use as a separator between title
         * and author like `-`, `—`, `|`, etc., excluding the single quote (`'`) and semicolon.
         */
        private val sepToken = "[\\S&&[^\\p{L}]&&[^\\d]]"

        @VisibleForTesting
        fun filter(title: String, author: String) = title.replace(
            "((\\s*$sepToken\\s*)($author))|(($author)(\\s*$sepToken\\s*))"
                .toRegex(RegexOption.IGNORE_CASE),
            ""
        )
    }
}

@VisibleForTesting
class FrenchTypography : DocumentedFilter() {
    override fun apply(
        input: Any?,
        args: Map<String?, Any?>?,
        self: PebbleTemplate?,
        context: EvaluationContext?,
        lineNumber: Int
    ): Any? = input?.toString()?.let(::filter)

    override fun getArgumentNames(): List<String?>? = null

    override fun generateDocumentation(context: Context) = context.getString(
        R.string.fr_typo_documentation,
        (leftTokens + rightTokens).joinToString { context.getString(R.string.localised_quotes, it) }
    )

    companion object {
        private val leftTokens = listOf("«")
        private val rightTokens = listOf("»", "!", "?", ";", ":")
        private val leftRegex = "([${leftTokens.joinToString("")}]+)\\s+".toRegex()
        private val rightRegex = "\\s+([${rightTokens.joinToString("")}]+)".toRegex()

        @VisibleForTesting
        fun filter(input: String) = input.replace(leftRegex, "$1 ").replace(rightRegex, " $1")
    }
}

class ShareIntentTextRenderer(private val item: Item): KoinComponent {
    val documentation by lazy {
        filters.entries.joinToString(prefix = "<br/>", separator = ",<br/>") { (key, filter) ->
            val str = get<Context>().getString(
                R.string.localised_dict_item,
                "<tt>$key</tt>",
                filter.documentation
            )
            "<br/>\u2022\t$str"
        }
    }

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
        private val filters: Map<String, DocumentedFilter> = mapOf(
            "remove_author" to RemoveAuthorFilter(),
            "fr_typo" to FrenchTypography()
        )

        private val renderer = PebbleEngine
            .Builder()
            .loader(StringLoader())
            .extension(object : AbstractExtension() {
                override fun getFilters(): Map<String, Filter> = this@Companion.filters
            })
            .newLineTrimming(false)
            .build()
    }
}