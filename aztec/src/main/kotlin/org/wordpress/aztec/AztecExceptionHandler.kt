package org.wordpress.aztec

import org.wordpress.android.util.AppLog
import java.lang.Thread.UncaughtExceptionHandler

class AztecExceptionHandler(private val logHelper: ExceptionHandlerHelper?, private val visualEditor: AztecText) : UncaughtExceptionHandler {

    interface ExceptionHandlerHelper {
        fun shouldLog(ex: Throwable) : Boolean
    }

    // Store the current exception handler
    private val rootHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    init {
        // we replace the exception handler now with us -- we will properly dispatch the exceptions ...
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        // Check if we should log the content or not
        var shouldLog = true
        try {
            shouldLog = logHelper?.shouldLog(ex) ?: true
        } catch (e: Throwable) {
            AppLog.w(AppLog.T.EDITOR, "There was an exception in the Logger Helper. Set the logging to true" )
        }

        if (shouldLog) {
            // Try to report the HTML code of the content, but do not report exceptions that can occur logging the content
            try {
                AppLog.e(AppLog.T.EDITOR, "HTML Content of Aztec Editor before the crash " + visualEditor.toPlainHtml(false))
            } catch (e: Throwable) {
                AppLog.e(AppLog.T.EDITOR, "Visual Content of Aztec Editor before the crash " + visualEditor.text)
            }
        }

        rootHandler?.uncaughtException(thread, ex)
    }

    fun restoreDefaultHandler() {
        Thread.setDefaultUncaughtExceptionHandler(rootHandler)
    }
}
