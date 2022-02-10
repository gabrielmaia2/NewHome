package com.newhome

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.newhome.dto.Animal
import java.io.File
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

class NovoAnimalActivity : AppCompatActivity() {
    private lateinit var adicionarAnimalImage: ImageView
    private lateinit var adicionarAnimalEditIcon: ImageView

    private lateinit var nomeNovoAnimalText: EditText
    private lateinit var descricaoNovoAnimalText: EditText

    private lateinit var localMapaButton: Button
    private lateinit var concluirAdicionarAnimalButton: Button
    private lateinit var cancelarAdicionarAnimalButton: Button

    private var imagem: Bitmap? = null

    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novo_animal)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Adicionar animal"

        adicionarAnimalImage = findViewById(R.id.adicionarAnimalImage)
        adicionarAnimalEditIcon = findViewById(R.id.adicionarAnimalEditIcon)

        nomeNovoAnimalText = findViewById(R.id.nomeNovoAnimalText)
        descricaoNovoAnimalText = findViewById(R.id.descricaoNovoAnimalText)

        localMapaButton = findViewById(R.id.localMapaButton)
        concluirAdicionarAnimalButton = findViewById(R.id.concluirAdicionarAnimalButton)
        cancelarAdicionarAnimalButton = findViewById(R.id.cancelarAdicionarAnimalButton)

        // TODO setar localMapaButton
        // TODO trocar o texto do mapa de colocar pra editar quando colocar localizacao

        setTakePictureLauncher()
        adicionarAnimalImage.setOnClickListener { onAdicionarImagem() }
        concluirAdicionarAnimalButton.setOnClickListener { onConcluir() }
        cancelarAdicionarAnimalButton.setOnClickListener { onCancelar() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onCancelar()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setTakePictureLauncher() {
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != RESULT_OK) return@registerForActivityResult

                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(photoFile!!.absolutePath, options)

                // determina quanto mudar a escala da imagem
                val side = NewHomeApplication.imageSideLength
                val scaleFactor: Int =
                    max(1, min(options.outWidth / side, options.outHeight / side))

                options.inJustDecodeBounds = false
                options.inSampleSize = scaleFactor
                val bitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath, options)

                this.imagem = bitmap
                adicionarAnimalImage.setImageBitmap(bitmap)
            }
    }

    private fun onAdicionarImagem() {
        // tira foto e guarda para foto do perfil

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // assegura que tem uma atividade de camera pra tratar do intent
        takePictureIntent.resolveActivity(packageManager)?.also {
            // cria um novo arquivo pra guardar a foto que vai ser tirada
            try {
                photoFile = Util.criarArquivoImagem(this)

                val photoURI = FileProvider.getUriForFile(
                    this,
                    "com.newhome.fileprovider",
                    photoFile!!
                )

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

                takePictureLauncher.launch(takePictureIntent)
            } catch (e: IOException) {
                // erro ao criar arquivo
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onConcluir() {
        // conclui a edicao e retorna para a tela de animal dono enviando o novo animal criado

        val animal = Animal()
        animal.imagem = imagem
        animal.nome = nomeNovoAnimalText.text.toString()
        animal.detalhes = descricaoNovoAnimalText.text.toString()
        // TODO enviar posicao no mapa

        NewHomeApplication.animalProvider.adicionarAnimal(animal, {
            Toast.makeText(applicationContext, "Animal adicionado com sucesso.", Toast.LENGTH_SHORT)
                .show()
            finish()
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun onCancelar() {
        // volta pra tela de lista sem adicionar animal

        finish()
    }
}
