package com.newhome

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

class Util {
    companion object {
        @Throws(IOException::class)
        fun criarArquivoImagem(context: Context): File {
            // cria um nome para a imagem
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

            // cria um arquivo temporario e retorna ele
            return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        }
    }
}
