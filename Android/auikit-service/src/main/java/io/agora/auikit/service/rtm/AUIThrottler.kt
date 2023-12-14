package io.agora.auikit.service.rtm

import android.os.Handler
import android.os.Looper

class AUIThrottler() {
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    fun triggerLastEvent(delay: Long, execute: ()->Unit) {
        runnable?.let {
            handler.removeCallbacks(it)
            runnable = null
        }
        runnable = Runnable {
            execute.invoke()
            runnable = null
        }
        runnable?.let {
            handler.postDelayed(it, delay)
        }
    }

    fun triggerNow() {
        runnable?.let {
            handler.removeCallbacks(it)
            it.run()
        }
    }
}