package com.newhome.app.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class Utils {
    companion object {
        suspend fun decodeBitmap(path: String): Bitmap = withContext(Dispatchers.IO) {
            val url = URL(path)
            return@withContext BitmapFactory.decodeStream(url.openStream())
        }
    }
}
