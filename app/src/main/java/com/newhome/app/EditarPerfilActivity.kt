package com.newhome.app

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.newhome.app.R
import com.newhome.app.dto.Usuario
import com.newhome.app.utils.DialogDisplayer
import com.newhome.app.utils.LoadingDialog
import com.newhome.app.utils.PictureTaker
import kotlinx.coroutines.launch

class EditarPerfilActivity : AppCompatActivity() {
    private lateinit var editPerfilImage: ImageView

    private lateinit var editPerfilIcon: ImageView
    private lateinit var nomePerfilEditText: EditText

    private lateinit var descricaoPerfilEditText: EditText
    private lateinit var editPerfilButton: Button

    private lateinit var cancelarEditPerfilButton: Button

    private lateinit var usuario: Usuario

    private lateinit var pictureTaker: PictureTaker

    private val dialog = LoadingDialog(this)
    private lateinit var dialogDisplayer: DialogDisplayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Editar perfil"

        dialogDisplayer = DialogDisplayer(applicationContext)

        editPerfilImage = findViewById(R.id.editPerfilImage)
        editPerfilIcon = findViewById(R.id.editPerfilIcon)

        nomePerfilEditText = findViewById(R.id.nomePerfilEditText)
        descricaoPerfilEditText = findViewById(R.id.descricaoPerfilEditText)

        editPerfilButton = findViewById(R.id.editPerfilButton)
        cancelarEditPerfilButton = findViewById(R.id.cancelarEditPerfilButton)

        carregarDados()
        createPictureTaker()
        editPerfilImage.setOnClickListener { pictureTaker.takePicture() }
        editPerfilButton.setOnClickListener { lifecycleScope.launch { onConcluir() } }
        cancelarEditPerfilButton.setOnClickListener { onCancelar() }
    }

    override fun onPause() {
        super.onPause()
        dialog.stop()
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

    private fun carregarDados() {
        // carrega dados do usuario e preenche campos na tela

        this.usuario = NewHomeApplication.usuarioService.getUsuarioAtual()
        nomePerfilEditText.setText(this.usuario.nome)
        descricaoPerfilEditText.setText(this.usuario.detalhes)
        editPerfilImage.setImageBitmap(this.usuario.imagem)
    }

    private fun createPictureTaker() {
        pictureTaker = PictureTaker(this, { bitmap ->
            usuario.imagem = bitmap
            editPerfilImage.setImageBitmap(bitmap)
        }, { e ->
            dialogDisplayer.display("Falha ao acessar câmera", e)
            finish()
        })
    }

    private suspend fun onConcluir() {
        // conclui a edicao e retorna para a tela do perfil enviando o novo usuario editado

        dialog.start()

        usuario.nome = nomePerfilEditText.text.toString()
        usuario.detalhes = descricaoPerfilEditText.text.toString()

        try {
            NewHomeApplication.usuarioService.editarUsuarioAtual(usuario).await()
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao editar usuário", e)
            dialog.stop()
            return
        }

        try {
            NewHomeApplication.usuarioService.carregarUsuarioAtual().await()
        } catch (e: Exception) {
            val message =
                "Perfil editado com sucesso, mas houve falha ao carregar novos dados"
            dialogDisplayer.display(message, e)
            dialog.stop()
            finish()
            return
        }

        dialogDisplayer.display("Perfil editado com sucesso")
        finish()
        dialog.stop()
    }

    private fun onCancelar() {
        // volta pra tela do perfil sem editar nada
        finish()
    }
}
