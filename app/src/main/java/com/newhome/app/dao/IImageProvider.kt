package com.newhome.app.dao

import android.graphics.Bitmap
import kotlinx.coroutines.Deferred

interface IImageProvider {
    fun getDefaultBitmap(): Bitmap

    suspend fun saveImage(path: String, bitmap: Bitmap?): Deferred<Unit>

    suspend fun getImage(path: String): Deferred<Bitmap>

    suspend fun removeImage(path: String): Deferred<Unit>

    suspend fun getImageOrDefault(path: String): Deferred<Bitmap>

    suspend fun getAnimalImage(id: String): Deferred<Bitmap>

    suspend fun getUserImage(id: String): Deferred<Bitmap>

    suspend fun saveAnimalImage(id: String, image: Bitmap?): Deferred<Unit>

    suspend fun saveUserImage(id: String, image: Bitmap?): Deferred<Unit>
}
