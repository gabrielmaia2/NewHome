package com.newhome

import android.content.Intent
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

class EditarAnimalActivity : AppCompatActivity() {
    private lateinit var animalEditImage: ImageView

    private lateinit var nomeAnimalEditText: EditText

    private lateinit var descricaoAnimalEditText: EditText
    private lateinit var editarMapaAnimalButton: Button

    private lateinit var concluirEditarAnimalButton: Button
    private lateinit var cancelarEditarAnimalButton: Button

    private lateinit var animal: Animal

    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_animal)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Editar animal"

        animalEditImage = findViewById(R.id.animalEditImage)

        nomeAnimalEditText = findViewById(R.id.nomeAnimalEditText)
        descricaoAnimalEditText = findViewById(R.id.descricaoAnimalEditText)

        editarMapaAnimalButton = findViewById(R.id.editarMapaAnimalButton)
        concluirEditarAnimalButton = findViewById(R.id.concluirEditarAnimalButton)
        cancelarEditarAnimalButton = findViewById(R.id.cancelarEditarAnimalButton)

        // TODO setar mapa listener e retornar nova posicao do mapa quando terminar de editar

        carregarDados()
        setTakePictureLauncher()
        animalEditImage.setOnClickListener { onEditarImagem() }
        concluirEditarAnimalButton.setOnClickListener { onConcluir() }
        cancelarEditarAnimalButton.setOnClickListener { onCancelar() }
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

                animal.imagem = bitmap
                animalEditImage.setImageBitmap(bitmap)
            }
    }

    private fun carregarDados() {
        // carrega os dados do database e preenche os campos

        val id = intent.getStringExtra("id")!!
        NewHomeApplication.animalProvider.getAnimal(id, { animal ->
            this.animal = animal
            nomeAnimalEditText.setText(animal.nome)
            descricaoAnimalEditText.setText(animal.detalhes)
            animalEditImage.setImageBitmap(animal.imagem)
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun onEditarImagem() {
        // tira foto e guarda para alterar foto do perfil depois

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
        // conclui a edicao e retorna para a tela de animal dono enviando o novo animal editado

        animal.nome = nomeAnimalEditText.text.toString()
        animal.detalhes = descricaoAnimalEditText.text.toString()
        // TODO enviar posicao no mapa

        NewHomeApplication.animalProvider.editarAnimal(animal, {
            Toast.makeText(applicationContext, "Animal editado com sucesso.", Toast.LENGTH_SHORT).show()
            finish()
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun onCancelar() {
        // volta pra tela de animal dono sem editar nada

        finish()
    }
}
