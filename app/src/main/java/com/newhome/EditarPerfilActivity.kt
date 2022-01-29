package com.newhome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.newhome.dto.Animal
import com.newhome.dto.Usuario

class EditarPerfilActivity : AppCompatActivity() {
    lateinit var usuario: Usuario

    lateinit var editPerfilImage: ImageView
    lateinit var editPerfilIcon: ImageView

    lateinit var nomePerfilEditText: EditText
    lateinit var descricaoPerfilEditText: EditText

    lateinit var editPerfilButton: Button
    lateinit var cancelarEditPerfilButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Editar perfil"

        usuario = intent.getSerializableExtra("usuario") as Usuario

        editPerfilImage = findViewById(R.id.editPerfilImage)
        editPerfilIcon = findViewById(R.id.editPerfilIcon)

        nomePerfilEditText = findViewById(R.id.nomePerfilEditText)
        descricaoPerfilEditText = findViewById(R.id.descricaoPerfilEditText)

        editPerfilButton = findViewById(R.id.editPerfilButton)
        cancelarEditPerfilButton = findViewById(R.id.cancelarEditPerfilButton)

        preencherCampos()
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

    private fun preencherCampos() {
        // preenche os campos do usuario na tela

        nomePerfilEditText.setText(usuario.nome)
        descricaoPerfilEditText.setText(usuario.detalhes)
        Util.tryLoadDrawableAsync(applicationContext, usuario.imagemURL) { drawable ->
            editPerfilImage.setImageDrawable(drawable)
        }
    }

    private fun onConcluir() {
        // conclui a edicao e retorna para a tela do perfil enviando o novo usuario editado

        // TODO enviar imagem pro database e colocar url aqui
        usuario.imagemURL = ""
        usuario.nome = nomePerfilEditText.text.toString()
        usuario.detalhes = descricaoPerfilEditText.text.toString()

        val intent = Intent(applicationContext, PerfilActivity::class.java)
        intent.putExtra("usuario", usuario)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun onCancelar() {
        // volta pra tela do perfil sem editar nada

        val intent = Intent(applicationContext, PerfilActivity::class.java)
        setResult(RESULT_CANCELED, intent)
        finish()
    }
}