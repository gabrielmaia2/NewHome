package com.newhome.app.dao.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.content.res.AppCompatResources
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storage
import com.newhome.app.R
import com.newhome.app.dao.IImageProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class FirebaseImageProvider(private val context: Context) : IImageProvider {
    private val cache: HashMap<String, ByteArray> = HashMap()

    private val storage = Firebase.storage
    private val storageRef = storage.reference

    private var bitmap: Bitmap? = null

    override fun getDefaultBitmap(): Bitmap {
        var bmp = bitmap
        if (bmp != null) return bmp

        val drawable = AppCompatResources.getDrawable(context, R.drawable.image_default)!!
        bmp = (drawable as BitmapDrawable).bitmap
        bitmap = bmp
        return bmp
    }

    override suspend fun saveImage(path: String, bitmap: Bitmap?) =
        CoroutineScope(Dispatchers.Main).async {
            if (bitmap == null) {
                return@async
            }

            val imgRef = storageRef.child("${path}.jpg")

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, baos)
            val data = baos.toByteArray()

            imgRef.putBytes(data).await()

            cache[path] = data
        }

    override suspend fun getImage(path: String): Deferred<Bitmap> =
        CoroutineScope(Dispatchers.Main).async {
//            val cached = cache[path]
//            if (cached != null) {
//                return@async BitmapFactory.decodeByteArray(cached, 0, cached.size)
//            }

            val imgRef = storageRef.child("$path.jpg")

            val oneGigabyte: Long = 1024 * 1024 * 1024
            val bytes = imgRef.getBytes(oneGigabyte).await()

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            cache[path] = bytes
            return@async bitmap
        }

    override suspend fun removeImage(path: String) =
        CoroutineScope(Dispatchers.Main).async {
            val imgRef = storageRef.child("$path.jpg")

            try {
                imgRef.delete().await()
            } catch (e: StorageException) {
                if (e.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) {
                    throw e
                }
            }
            cache.remove(path)
            return@async
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
}
