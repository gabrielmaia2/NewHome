package com.newhome.app.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.net.URL
import java.security.MessageDigest

class Utils {
    companion object {
        suspend fun decodeBitmap(path: String): Bitmap = withContext(Dispatchers.IO) {
            val url = URL(path)
            return@withContext BitmapFactory.decodeStream(url.openStream())
        }

        fun sha256(input: ByteArray): String {
            val md = MessageDigest.getInstance("SHA-256")
            return BigInteger(1, md.digest(input)).toString(16).padStart(64, '0')
        }

        fun bitmapToJPEGByteArray(bitmap: Bitmap, quality: Int): ByteArray {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, baos)
            return baos.toByteArray()
        }
    }
}
