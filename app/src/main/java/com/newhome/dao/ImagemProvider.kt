package com.newhome.dao

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.content.res.AppCompatResources
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.newhome.R
import java.io.ByteArrayOutputStream

class ImagemProvider {
    companion object {
        private val cache: HashMap<String, ByteArray> = HashMap()

        private val storage = Firebase.storage
        private val storageRef = storage.reference

        fun getDefaultBitmap(context: Context): Bitmap {
            val drawable = AppCompatResources.getDrawable(context, R.drawable.image_default)!!
            return (drawable as BitmapDrawable).bitmap
        }

        fun saveImageToFirebase(
            path: String,
            bitmap: Bitmap?,
            onSuccess: () -> Unit,
            onFailure: (e: Exception) -> Unit
        ) {
            if (bitmap == null) {
                onSuccess()
                return
            }

            val imgRef = storageRef.child("${path}.jpg")

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, baos)
            val data = baos.toByteArray()

            imgRef.putBytes(data)
                .addOnSuccessListener {
                    cache[path] = data
                    onSuccess()
                }
                .addOnFailureListener(onFailure)
        }

        fun getImageFromFirebase(
            path: String,
            onSuccess: (bitmap: Bitmap) -> Unit,
            onFailure: (e: Exception) -> Unit
        ) {
            val cached = cache[path]
            if (cached != null) {
                val bitmap = BitmapFactory.decodeByteArray(cached, 0, cached.size)
                onSuccess(bitmap)
                return
            }

            val imgRef = storageRef.child("$path.jpg")

            val ONE_GIGABYTE: Long = 1024 * 1024 * 1024

            imgRef.getBytes(ONE_GIGABYTE)
                .addOnSuccessListener { bytes ->
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    cache[path] = bytes
                    onSuccess(bitmap)
                }.addOnFailureListener(onFailure)
        }

        fun removeImageFromFirebase(
            path: String,
            onSuccess: () -> Unit,
            onFailure: (e: Exception) -> Unit
        ) {
            val imgRef = storageRef.child("$path.jpg")

            imgRef.delete()
                .addOnSuccessListener {
                    cache.remove(path)
                    onSuccess()
                }
                .addOnFailureListener(onFailure)
        }
    }
}
