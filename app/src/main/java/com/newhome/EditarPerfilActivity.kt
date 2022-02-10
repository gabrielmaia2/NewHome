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
import com.newhome.dto.Usuario
import java.io.File
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

class EditarPerfilActivity : AppCompatActivity() {
    private lateinit var editPerfilImage: ImageView

    private lateinit var editPerfilIcon: ImageView
    private lateinit var nomePerfilEditText: EditText

    private lateinit var descricaoPerfilEditText: EditText
    private lateinit var editPerfilButton: Button

    private lateinit var cancelarEditPerfilButton: Button

    private lateinit var usuario: Usuario

    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Editar perfil"

        editPerfilImage = findViewById(R.id.editPerfilImage)
        editPerfilIcon = findViewById(R.id.editPerfilIcon)

        nomePerfilEditText = findViewById(R.id.nomePerfilEditText)
        descricaoPerfilEditText = findViewById(R.id.descricaoPerfilEditText)

        editPerfilButton = findViewById(R.id.editPerfilButton)
        cancelarEditPerfilButton = findViewById(R.id.cancelarEditPerfilButton)

        carregarDados()
        setTakePictureLauncher()
        editPerfilImage.setOnClickListener { onEditarImagem() }
        editPerfilButton.setOnClickListener { onConcluir() }
        cancelarEditPerfilButton.setOnClickListener { onCancelar() }
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

                usuario.imagem = bitmap
                editPerfilImage.setImageBitmap(bitmap)
            }
    }

    private fun carregarDados() {
        // carrega dados do usuario e preenche campos na tela

        NewHomeApplication.usuarioProvider.getUsuarioAtual({ usuario ->
            this.usuario = usuario
            nomePerfilEditText.setText(usuario.nome)
            descricaoPerfilEditText.setText(usuario.detalhes)
            editPerfilImage.setImageBitmap(usuario.imagem)
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
        // conclui a edicao e retorna para a tela do perfil enviando o novo usuario editado

        usuario.nome = nomePerfilEditText.text.toString()
        usuario.detalhes = descricaoPerfilEditText.text.toString()

        NewHomeApplication.usuarioProvider.editarUsuarioAtual(usuario, {
            Toast.makeText(applicationContext, "Perfil editado com sucesso.", Toast.LENGTH_SHORT)
                .show()
            finish()
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun onCancelar() {
        // volta pra tela do perfil sem editar nada

        finish()
    }
}
