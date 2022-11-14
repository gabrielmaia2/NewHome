package com.newhome.app.utils

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.newhome.app.NewHomeApplication
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

class PictureTaker(
    private val activity: ComponentActivity,
    private val onSuccess: (bitmap: Bitmap) -> Unit,
    onFailure: (e: Exception) -> Unit
) {
    private var photoFile: File? = null
    private var imageUri: Uri? = null
    private var takePictureLauncher: ActivityResultLauncher<Intent>? = null

    init {
        try {
            photoFile = createImageFile()
            imageUri = FileProvider.getUriForFile(
                activity,
                "com.newhome.fileprovider",
                photoFile!!
            )
            takePictureLauncher = createTakePictureLauncher()
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // cria um nome para a imagem
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale("pt", "BR")).format(Date())
        Log.e("TIMESTAMP***", timeStamp)
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        // cria um arquivo temporario e retorna ele
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun createTakePictureLauncher(): ActivityResultLauncher<Intent> {
        return activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != AppCompatActivity.RESULT_OK) return@registerForActivityResult

            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(photoFile!!.absolutePath, options)

            // determina quanto mudar a escala da imagem
            val side = NewHomeApplication.imageSideLength
            val scaleFactor: Int = max(1, min(options.outWidth / side, options.outHeight / side))

            options.inJustDecodeBounds = false
            options.inSampleSize = scaleFactor
            var bitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath, options)

            if (bitmap.width > bitmap.height) {
                var diff = bitmap.width - bitmap.height
                diff /= 2

                bitmap = Bitmap.createBitmap(bitmap, diff, 0, bitmap.height, bitmap.height)
            } else {
                var diff = bitmap.height - bitmap.width
                diff /= 2

                bitmap = Bitmap.createBitmap(bitmap, 0, diff, bitmap.width, bitmap.width)
            }

            try {
                val rotation = getCameraPhotoOrientation()
                val matrix = Matrix()
                matrix.postRotate(rotation.toFloat())
                bitmap =
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            onSuccess(bitmap)
        }
    }

    private fun getCameraPhotoOrientation(): Int {
        activity.contentResolver.notifyChange(imageUri!!, null)
        val imageFile = File(photoFile!!.absolutePath) // TODO ???
        val exif = ExifInterface(imageFile.absolutePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val rotate = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        return rotate
    }

    fun takePicture() {
        // tira foto e guarda para foto do perfil

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // assegura que tem uma atividade de camera pra tratar do intent
        takePictureIntent.resolveActivity(activity.packageManager)?.also {
            // cria um novo arquivo pra guardar a foto que vai ser tirada
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            takePictureLauncher!!.launch(takePictureIntent)
        }
    }
}
