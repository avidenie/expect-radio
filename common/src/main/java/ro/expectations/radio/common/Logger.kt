package ro.expectations.radio.common

import android.util.Log


class Logger {

    companion object {

        fun v(tag: String, vararg messages: Any) {
            if (BuildConfig.DEBUG) {
                log(tag, Log.VERBOSE, null, *messages)
            }
        }

        fun d(tag: String, vararg messages: Any) {
            if (BuildConfig.DEBUG) {
                log(tag, Log.DEBUG, null, *messages)
            }
        }

        fun i(tag: String, vararg messages: Any) {
            log(tag, Log.INFO, null, *messages)
        }

        fun w(tag: String, vararg messages: Any) {
            log(tag, Log.WARN, null, *messages)
        }

        fun w(tag: String, t: Throwable, vararg messages: Any) {
            log(tag, Log.WARN, t, *messages)
        }

        fun e(tag: String, vararg messages: Any) {
            log(tag, Log.ERROR, null, *messages)
        }

        fun e(tag: String, t: Throwable, vararg messages: Any) {
            log(tag, Log.ERROR, t, *messages)
        }

        private fun log(tag: String, level: Int, t: Throwable?, vararg messages: Any) {
            if (Log.isLoggable(tag, level)) {
                val message: String
                message = if (t == null && messages.size == 1) {
                    messages[0].toString()
                } else {
                    val sb = StringBuilder()
                    for (m in messages) {
                        sb.append(m)
                    }
                    if (t != null) {
                        sb.append("\n").append(Log.getStackTraceString(t))
                    }
                    sb.toString()
                }
                Log.println(level, tag, message)
            }
        }
    }
}