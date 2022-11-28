package com.newhome.app.dto

import android.graphics.Bitmap

data class Animal(
    var id: String = "",
    var name: String = "",
    var details: String = "",
    var image: Bitmap? = null
) {
    // TODO location on maps

    // para poder pesquisar animal na lista de animais
    override fun toString(): String {
        return name.lowercase() + details.lowercase()
    }

    companion object {
        fun fromData(data: AnimalData): Animal {
            return Animal(data.id, data.name, data.details)
        }

        suspend fun fromData(data: AnimalAsync): Animal {
            return Animal(data.id, data.name, data.details, data.getImage!!.await())
        }
    }
}
