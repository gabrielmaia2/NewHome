package com.newhome.app.utils

import android.content.Context
import android.widget.Toast

class DialogDisplayer(private val context: Context) {
    fun display(message: String, long: Boolean = false) {
        val length = if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        Toast.makeText(context, message, length).show()
    }

    fun display(message: String, e: Exception, long: Boolean = false) {
        display(message + ". Erro: " + e.message, long)
    }

    fun display(e: Exception, long: Boolean = false) {
        display("Erro: " + e.message, long)
    }
}
