package com.newhome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.newhome.dto.Usuario

class PerfilActivity : AppCompatActivity() {
    private lateinit var usuario: Usuario

    private lateinit var perfilImage: ImageView

    private lateinit var nomePerfilText: TextView
    private lateinit var descricaoPerfilText: TextView

    private lateinit var animaisAdotadosButton: Button
    private lateinit var animaisPostosAdocaoButton: Button

    private var id: String = ""
    private var eProprioPerfil = false

    private lateinit var editPerfilActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Perfil"

        perfilImage = findViewById(R.id.perfilImage)

        nomePerfilText = findViewById(R.id.nomePerfilText)
        descricaoPerfilText = findViewById(R.id.descricaoPerfilText)

        animaisAdotadosButton = findViewById(R.id.editPerfilButton)
        animaisPostosAdocaoButton = findViewById(R.id.cancelarEditPerfilButton)

        carregarDados()
        setEditPerfilActivityLauncher()
        animaisAdotadosButton.setOnClickListener { onVerAnimaisAdotados() }
        animaisPostosAdocaoButton.setOnClickListener { onVerAnimaisPostosAdocao() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (!eProprioPerfil) return super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.editar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editarMenuItem -> {
                onEditarPerfilClick()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun carregarDados() {
        id = intent.getStringExtra("id") ?: ""

        // TODO checar se o perfil e o mesmo da pessoa atual
        eProprioPerfil = id == ""

        // TODO carregar dados do perfil
        usuario = Usuario()
        usuario.nome = "Marcos"
        usuario.detalhes = "Sou uma pessoa muito legal."
        usuario.imagemURL = ""

        nomePerfilText.text = usuario.nome
        descricaoPerfilText.text = usuario.detalhes
        Util.tryLoadDrawableAsync(this, usuario.imagemURL) { drawable ->
            perfilImage.setImageDrawable(drawable)
        }

        if (!eProprioPerfil) {
            animaisAdotadosButton.visibility = View.GONE
        }
    }

    private fun setEditPerfilActivityLauncher() {
        // launcher usado para iniciar a atividade de editar perfil

        editPerfilActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != RESULT_OK) return@registerForActivityResult

                usuario = result.data!!.getSerializableExtra("usuario") as Usuario
                // TODO enviar perfil pro database

                Toast.makeText(this, "Perfil editado com sucesso.", Toast.LENGTH_SHORT).show()

                carregarDados() // atualiza dados do perfil
            }
    }

    private fun onVerAnimaisAdotados() {
        // vai pra lista de animais filtrando apenas os adotados

        val intent = Intent(applicationContext, ListarAnimaisActivity::class.java)
        intent.putExtra("tipo", "adotados")
        startActivity(intent)
        finish()
    }

    private fun onVerAnimaisPostosAdocao() {
        // vai pra lista de animais filtrando apenas os postos em adocao

        val intent = Intent(applicationContext, ListarAnimaisActivity::class.java)
        intent.putExtra("tipo", "postosAdocao")
        if (!eProprioPerfil) {
            intent.putExtra("usuarioId", id)
        }
        startActivity(intent)
    }

    private fun onEditarPerfilClick() {
        // vai para a tela de editar perfil

        val intent = Intent(applicationContext, EditarPerfilActivity::class.java)
        intent.putExtra("usuario", usuario)
        editPerfilActivityLauncher.launch(intent)
    }
}