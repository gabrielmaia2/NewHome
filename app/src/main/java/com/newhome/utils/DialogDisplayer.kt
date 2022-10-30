package com.newhome.utils

import android.content.Context
import android.widget.Toast

class DialogDisplayer(private val context: Context) {
    fun display(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun display(message: String, e: Exception) {
        display(message + ". Erro: " + e.message)
    }

    fun display(e: Exception) {
        display("Erro: " + e.message)
    }
}
