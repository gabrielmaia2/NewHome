package com.newhome.app.dto

import android.graphics.Bitmap

data class User(
    var id: String = "",
    var name: String = "",
    var details: String = "",
    var image: Bitmap? = null
) {
    companion object {
        fun fromData(data: UserData): User {
            return User(data.id, data.name, data.details)
        }

        suspend fun fromData(data: UsuarioAsync): User {
            return User(data.id, data.name, data.details, data.getImage!!.await())
        }
    }
}
