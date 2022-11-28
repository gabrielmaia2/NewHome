package com.newhome.app.dto

import android.graphics.Bitmap
import kotlinx.coroutines.Deferred

class UsuarioAsync(
    var id: String = "",
    var name: String = "",
    var details: String = "",
    var getImage: Deferred<Bitmap>? = null
) {
    companion object {
        fun fromData(data: UserData): UsuarioAsync {
            return UsuarioAsync(data.id, data.name, data.details)
        }
    }
}
