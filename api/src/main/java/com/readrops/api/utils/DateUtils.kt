package com.readrops.api.utils

import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatterBuilder
import java.util.*

object DateUtils {

    private val TAG = DateUtils::class.java.simpleName

    /**
     * Base of common RSS 2 date formats.
     * Examples :
     * Fri, 04 Jan 2019 22:21:46 GMT
     * Fri, 04 Jan 2019 22:21:46 +0000
     */
    private const val RSS_2_BASE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss"

    private const val GMT_PATTERN = "ZZZ"

    private const val OFFSET_PATTERN = "Z"

    private const val ISO_PATTERN = ".SSSZZ"

    private const val EDT_PATTERN = "zzz"

    /**
     * Date pattern for format : 2019-01-04T22:21:46+00:00
     */
    private const val ATOM_JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

    @JvmStatic
    fun parse(value: String?): LocalDateTime? = if (value == null) {
        null
    } else try {
        val formatter = DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormat.forPattern("$RSS_2_BASE_PATTERN ").parser) // with timezone
                .appendOptional(DateTimeFormat.forPattern(RSS_2_BASE_PATTERN).parser) // no timezone, important order here
                .appendOptional(DateTimeFormat.forPattern(ATOM_JSON_DATE_FORMAT).parser)
                .appendOptional(DateTimeFormat.forPattern(GMT_PATTERN).parser)
                .appendOptional(DateTimeFormat.forPattern(OFFSET_PATTERN).parser)
                .appendOptional(DateTimeFormat.forPattern(ISO_PATTERN).parser)
                .appendOptional(DateTimeFormat.forPattern(EDT_PATTERN).parser)
                .toFormatter()
                .withLocale(Locale.ENGLISH)
                .withOffsetParsed()

        formatter.parseLocalDateTime(value)
    } catch (e: Exception) {
        null
    }

    @JvmStatic
    fun formattedDateByLocal(dateTime: LocalDateTime): String {
        return DateTimeFormat.mediumDate()
                .withLocale(Locale.getDefault())
                .print(dateTime)
    }

    @JvmStatic
    fun formattedDateTimeByLocal(dateTime: LocalDateTime): String {
        return DateTimeFormat.forPattern("dd MMM yyyy · HH:mm")
                .withLocale(Locale.getDefault())
                .print(dateTime)
    }
}