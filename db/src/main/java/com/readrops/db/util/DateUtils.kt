package com.readrops.db.util

import android.annotation.SuppressLint
import android.util.Log
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    private val TAG = DateUtils::class.java.simpleName

    /**
     * Base of common RSS 2 date formats.
     * Examples :
     * Fri, 04 Jan 2019 22:21:46 GMT
     * Fri, 04 Jan 2019 22:21:46 +0000
     *
     * Base pattern is "EEE, dd MMM yyyy HH:mm:ss" but as java.time android desugaring is very strict,
     * using the date to guess the day of week prevents some parsing failures
     */
    private const val RSS_2_BASE_PATTERN = "dd MMM yyyy HH:mm:ss"

    private const val GMT_PATTERN = "ZZZ"

    private const val OFFSET_PATTERN = "Z"

    private const val ISO_PATTERN = ".SSSZZ"

    private const val EDT_PATTERN = "zzz"

    private const val ZONE_OFFSET_PATTERN = ".SSSxxx"

    /**
     * Date pattern for format : 2019-01-04T22:21:46+00:00
     */
    private const val ATOM_JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

    val defaultOffset: ZoneOffset
        get() = OffsetDateTime.now(TimeZone.getDefault().toZoneId())
            .offset

    /**
     * Attempts to parse a date string representation.
     * If the provided value is null or the parsing fails, [LocalDateTime.now] is returned.
     * @return parsed date or [LocalDateTime.now]
     */
    @SuppressLint("NewApi") // works with API 21+ so the lint might be buggy
    @JvmStatic
    fun parse(value: String?): LocalDateTime {
        if (value == null) {
            return LocalDateTime.now()
        }

        // RSS2 base pattern, we remove the day of week to avoid some parsing failures
        // from java.time android desugaring version
        val formattedValue = if (value.contains(",") == true) {
            value.removeRange(0..4)
        } else {
            value
        }

        return try {
            val formatter = DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ofPattern("$RSS_2_BASE_PATTERN ")) // with timezone
                .appendOptional(DateTimeFormatter.ofPattern(RSS_2_BASE_PATTERN)) // no timezone, important order here
                .appendOptional(DateTimeFormatter.ofPattern(ATOM_JSON_DATE_FORMAT))
                .appendOptional(DateTimeFormatter.ofPattern(EDT_PATTERN))
                .appendOptional(DateTimeFormatter.ofPattern(ZONE_OFFSET_PATTERN))
                .appendOptional(DateTimeFormatter.ofPattern(GMT_PATTERN))
                .appendOptional(DateTimeFormatter.ofPattern(OFFSET_PATTERN))
                .appendOptional(DateTimeFormatter.ofPattern(ISO_PATTERN))
                .toFormatter()
                .withLocale(Locale.ENGLISH)

            LocalDateTime.from(formatter.parse(formattedValue))
        } catch (e: Exception) {
            Log.e(TAG, "Unable to parse $formattedValue: ${e.message}")
            LocalDateTime.now()
        }
    }

    /**
     * Be aware of giving a second epoch value and not a millisecond one!
     */
    fun fromEpochSeconds(epoch: Long): LocalDateTime {
        return LocalDateTime.ofEpochSecond(epoch, 0, defaultOffset)
    }

    fun formattedDateByLocal(dateTime: LocalDateTime): String {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(Locale.getDefault())
            .format(dateTime)
    }
}