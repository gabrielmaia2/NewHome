package com.newhome

import android.app.Application
import android.content.Context

class NewHomeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val context = applicationContext

        // TODO inicializar providers aqui

        // TODO sempre que der um erro mostrar ele usando toast
    }
}