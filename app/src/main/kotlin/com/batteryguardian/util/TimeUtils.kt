package com.batteryguardian.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Utility functions for working with time and dates.
 */

object TimeUtils {

    // ==================== Formatters ====================

    /** Time formatter (HH:mm) */
    private val timeFormatter: DateTimeFormatter = 
        DateTimeFormatter.ofPattern("HH:mm")

    /** Date formatter (MMM dd) */
    private val dateFormatter: DateTimeFormatter = 
        DateTimeFormatter.ofPattern("MMM dd")

    /** Date and time formatter (MMM dd, HH:mm) */
    private val dateTimeFormatter: DateTimeFormatter = 
        DateTimeFormatter.ofPattern("MMM dd, HH:mm")

    /** Full date formatter (yyyy-MM-dd HH:mm:ss) */
    private val fullDateFormatter: DateTimeFormatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /** Relative time formatter */
    private val relativeTimeFormatter: DateTimeFormatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

    // ==================== Formatting Functions ====================

    /**
     * Format an Instant as a time string (HH:mm).
     */
    fun formatTime(instant: Instant): String {
        return instant.atZone(ZoneId.systemDefault()).format(timeFormatter)
    }

    /**
     * Format an Instant as a date string (MMM dd).
     */
    fun formatDate(instant: Instant): String {
        return instant.atZone(ZoneId.systemDefault()).format(dateFormatter)
    }

    /**
     * Format an Instant as a date and time string (MMM dd, HH:mm).
     */
    fun formatDateTime(instant: Instant): String {
        return instant.atZone(ZoneId.systemDefault()).format(dateTimeFormatter)
    }

    /**
     * Format an Instant as a full date string (yyyy-MM-dd HH:mm:ss).
     */
    fun formatFullDate(instant: Instant): String {
        return instant.atZone(ZoneId.systemDefault()).format(fullDateFormatter)
    }

    /**
     * Format a Duration as a human-readable string.
     * 
     * Examples:
     * - 2 hours, 30 minutes
     * - 1 hour, 15 minutes
     * - 45 minutes
     * - 30 seconds
     */
    fun formatDuration(duration: Duration): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutesPart()
        val seconds = duration.toSecondsPart()

        return buildString {
            if (hours > 0) {
                append("$hours hour")
                if (hours != 1L) append("s")
                if (minutes > 0 || seconds > 0) append(", ")
            }
            if (minutes > 0) {
                append("$minutes minute")
                if (minutes != 1) append("s")
                if (seconds > 0) append(", ")
            }
            if (seconds > 0 && hours == 0L && minutes == 0) {
                append("$seconds second")
                if (seconds != 1) append("s")
            }
        }.ifEmpty { "0 seconds" }
    }

    /**
     * Format a Duration as a short string.
     * 
     * Examples:
     * - 2h
     * - 30m
     * - 45s
     */
    fun formatDurationShort(duration: Duration): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutesPart()
        val seconds = duration.toSecondsPart()

        return when {
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}m"
            else -> "${seconds}s"
        }
    }

    /**
     * Format a Duration as a digital clock string (HH:mm:ss).
     */
    fun formatDurationDigital(duration: Duration): String {
        val hours = duration.toHours().toString().padStart(2, '0')
        val minutes = duration.toMinutesPart().toString().padStart(2, '0')
        val seconds = duration.toSecondsPart().toString().padStart(2, '0')
        return "$hours:$minutes:$seconds"
    }

    /**
     * Format an Instant as a relative time string.
     * 
     * Examples:
     * - Just now
     * - 5 seconds ago
     * - 2 minutes ago
     * - 1 hour ago
     * - 2 days ago
     * - In 5 minutes
     */
    fun formatRelativeTime(instant: Instant): String {
        val now = Instant.now()
        val duration = Duration.between(instant, now)

        return when {
            duration.isNegative -> "In ${formatDuration(duration.absoluteValue)}"
            duration.toSeconds() < 5 -> "Just now"
            duration.toMinutes() < 1 -> "${duration.toSeconds()} seconds ago"
            duration.toHours() < 1 -> "${duration.toMinutes()} minutes ago"
            duration.toHours() < 24 -> "${duration.toHours()} hours ago"
            duration.toDays() < 7 -> "${duration.toDays()} days ago"
            duration.toDays() < 30 -> "${duration.toDays() / 7} weeks ago"
            duration.toDays() < 365 -> "${duration.toDays() / 30} months ago"
            else -> "${duration.toDays() / 365} years ago"
        }
    }

    // ==================== Time Calculations ====================

    /**
     * Calculate the time until a specific Instant.
     */
    fun timeUntil(target: Instant): Duration {
        return Duration.between(Instant.now(), target)
    }

    /**
     * Calculate the time since a specific Instant.
     */
    fun timeSince(target: Instant): Duration {
        return Duration.between(target, Instant.now())
    }

    /**
     * Check if an Instant is in the past.
     */
    fun isPast(instant: Instant): Boolean {
        return instant.isBefore(Instant.now())
    }

    /**
     * Check if an Instant is in the future.
     */
    fun isFuture(instant: Instant): Boolean {
        return instant.isAfter(Instant.now())
    }

    /**
     * Check if an Instant is today.
     */
    fun isToday(instant: Instant): Boolean {
        val now = LocalDateTime.now()
        val instantDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        return now.year == instantDateTime.year && 
               now.month == instantDateTime.month && 
               now.dayOfMonth == instantDateTime.dayOfMonth
    }

    /**
     * Check if an Instant is yesterday.
     */
    fun isYesterday(instant: Instant): Boolean {
        val now = LocalDateTime.now()
        val instantDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        val yesterday = now.minusDays(1)
        return yesterday.year == instantDateTime.year && 
               yesterday.month == instantDateTime.month && 
               yesterday.dayOfMonth == instantDateTime.dayOfMonth
    }

    /**
     * Check if an Instant is within the last N hours.
     */
    fun isWithinLastHours(instant: Instant, hours: Long): Boolean {
        return timeSince(instant).toHours() <= hours
    }

    /**
     * Check if an Instant is within the last N minutes.
     */
    fun isWithinLastMinutes(instant: Instant, minutes: Long): Boolean {
        return timeSince(instant).toMinutes() <= minutes
    }

    /**
     * Check if an Instant is older than N days.
     */
    fun isOlderThanDays(instant: Instant, days: Long): Boolean {
        return timeSince(instant).toDays() > days
    }

    // ==================== Time Units ====================

    /**
     * Convert hours to milliseconds.
     */
    fun hoursToMillis(hours: Long): Long {
        return hours * 60 * 60 * 1000
    }

    /**
     * Convert minutes to milliseconds.
     */
    fun minutesToMillis(minutes: Long): Long {
        return minutes * 60 * 1000
    }

    /**
     * Convert seconds to milliseconds.
     */
    fun secondsToMillis(seconds: Long): Long {
        return seconds * 1000
    }

    /**
     * Convert milliseconds to hours.
     */
    fun millisToHours(millis: Long): Long {
        return millis / (60 * 60 * 1000)
    }

    /**
     * Convert milliseconds to minutes.
     */
    fun millisToMinutes(millis: Long): Long {
        return millis / (60 * 1000)
    }

    /**
     * Convert milliseconds to seconds.
     */
    fun millisToSeconds(millis: Long): Long {
        return millis / 1000
    }
}
