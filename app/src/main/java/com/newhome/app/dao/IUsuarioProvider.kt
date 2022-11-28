package com.newhome.app.dao

import android.graphics.Bitmap
import com.newhome.app.dto.NewUser
import kotlinx.coroutines.Deferred
import com.newhome.app.dto.UserData

interface IUsuarioProvider {
    suspend fun getUser(id: String): Deferred<UserData>

    suspend fun createUser(usuario: NewUser): Deferred<Unit>

    suspend fun updateUser(usuario: UserData): Deferred<Unit>

    suspend fun deleteUser(id: String): Deferred<Unit>

    suspend fun getUserImage(id: String): Deferred<Bitmap>

    suspend fun setUserImage(id: String, image: Bitmap?): Deferred<Unit>

    suspend fun addAnimalIdToList(id: String, animalId: String, list: AnimalList): Deferred<Unit>

    suspend fun removeAnimalIdFromList(id: String, animalId: String, list: AnimalList): Deferred<Unit>
}
