package com.newhome.app.dao.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storageMetadata
import com.newhome.app.R
import com.newhome.app.dao.IImageProvider
import com.newhome.app.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

open class FirebaseImageProvider(private val context: Context, storage: FirebaseStorage) :
    IImageProvider {
    var cache: HashMap<String, ByteArray> = HashMap()

    private val storageRef = storage.reference

    private var defaultBitmap: Bitmap? = null

    override fun getDefaultBitmap(): Bitmap {
        var bmp = defaultBitmap
        if (bmp != null) return bmp

        bmp = BitmapFactory.decodeResource(context.resources, R.drawable.image_default)

        defaultBitmap = bmp
        return bmp
    }

    override suspend fun saveImage(path: String, bitmap: Bitmap?) =
        CoroutineScope(Dispatchers.Main).async {
            if (bitmap == null) {
                return@async
            }
            if (bitmap == getDefaultBitmap()) {
                throw Exception("Trying to save the default bitmap on firebase.")
            }

            val imgRef = storageRef.child("${path}.jpg")

            val data = Utils.bitmapToJPEGByteArray(bitmap, 95)

            val sha256sum = Utils.sha256(data)
            val metadata = storageMetadata {
                setCustomMetadata("sha256sum", sha256sum)
            }
            imgRef.putBytes(data, metadata).await()

            cache[sha256sum] = data
        }

    override suspend fun getImage(path: String): Deferred<Bitmap> =
        CoroutineScope(Dispatchers.Main).async {
            val imgRef = storageRef.child("$path.jpg")

            val oneGigabyte: Long = 1024 * 1024 * 1024
            var sha256 = imgRef.metadata.await().getCustomMetadata("sha256sum")
            var bytes = if (sha256 != null) cache[sha256] else null

            if (bytes == null) {
                val bytesTask = imgRef.getBytes(oneGigabyte)
                val mdTask = imgRef.metadata

                sha256 = mdTask.await().getCustomMetadata("sha256sum")
                bytes = bytesTask.await()
            }

            bytes!!
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (sha256 != null) cache[sha256] = bytes

            return@async bitmap
        }

    override suspend fun removeImage(path: String) =
        CoroutineScope(Dispatchers.Main).async {
            val imgRef = storageRef.child("$path.jpg")

            try {
                val mdTask = imgRef.metadata
                val deleteTask = imgRef.delete()

                val sha256 = mdTask.await().getCustomMetadata("sha256sum")
                deleteTask.await()
                if (sha256 != null) cache.remove(sha256)
            } catch (e: StorageException) {
                if (e.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) {
                    throw e
                }
            }
        }

    override suspend fun getImageOrDefault(path: String): Deferred<Bitmap> =
        CoroutineScope(Dispatchers.Main).async {
            val imagem = try {
                getImage(path).await()
            } catch (e: Exception) {
                getDefaultBitmap()
            }
            return@async imagem
        }

    override suspend fun getAnimalImage(id: String): Deferred<Bitmap> =
        getImageOrDefault("animais/${id}")

    override suspend fun getUserImage(id: String): Deferred<Bitmap> =
        getImageOrDefault("usuarios/${id}")

    override suspend fun saveAnimalImage(id: String, image: Bitmap?): Deferred<Unit> =
        saveImage("animais/${id}", image)

    override suspend fun saveUserImage(id: String, image: Bitmap?): Deferred<Unit> =
        saveImage("usuarios/${id}", image)
}
