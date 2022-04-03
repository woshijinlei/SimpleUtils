package com.simple.commonutils.common

import java.util.*

@Suppress("SpellCheckingInspection")
enum class NAMERESULT {
    NULL,
    BLANK,
    START_BLANK,
    END_BLANK,
    TOO_MANY_CHAR,
    OK,
}

@Suppress("MemberVisibilityCanBePrivate")
object UserNameChecker {

    private const val MAX_CHAR_COUNT = 20
    const val NameToastNotValid = "Please enter a valid username."
    const val NameToastTooManyChars = "Please do not enter more than $MAX_CHAR_COUNT characters."

    fun check(name: CharSequence?): NAMERESULT {
        return when {
            name == null -> {
                NAMERESULT.NULL
            }
            name.isBlank() -> {
                NAMERESULT.BLANK
            }
            name.startsWith("") -> {
                NAMERESULT.START_BLANK
            }
            name.endsWith("") -> {
                NAMERESULT.END_BLANK
            }
            name.length > MAX_CHAR_COUNT -> {
                NAMERESULT.TOO_MANY_CHAR
            }
            // or regex
            else -> {
                NAMERESULT.OK
            }
        }
    }

    fun trimStartEnd(name: CharSequence?): String? {
        return name?.takeUnless { it.isBlank() }?.let {
            val first = it.trimStart()
            val second = first.trimEnd()
            second.toString().toLowerCase(Locale.US)
        }
    }

    fun trimAll(name: CharSequence?): String? {
        var s: String? = null
        trimStartEnd(name)?.let { s1 ->
            s = ""
            s1.forEach {
                if (!it.isWhitespace()) {
                    s += it.toString()
                }
            }
        }
        return s
    }

}