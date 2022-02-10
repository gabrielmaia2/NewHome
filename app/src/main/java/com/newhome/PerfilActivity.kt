package com.newhome

import android.content.Intent
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
import androidx.appcompat.app.AppCompatActivity
import com.newhome.dto.Usuario

class PerfilActivity : AppCompatActivity() {
    private lateinit var perfilImage: ImageView

    private lateinit var nomePerfilText: TextView
    private lateinit var descricaoPerfilText: TextView

    private lateinit var animaisAdotadosButton: Button
    private lateinit var animaisPostosAdocaoButton: Button
    private lateinit var sairPerfilButton: Button

    private lateinit var usuario: Usuario
    private var eProprioPerfil = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Perfil"

        perfilImage = findViewById(R.id.perfilImage)

        nomePerfilText = findViewById(R.id.nomePerfilText)
        descricaoPerfilText = findViewById(R.id.descricaoPerfilText)

        animaisAdotadosButton = findViewById(R.id.animaisAdotadosButton)
        animaisPostosAdocaoButton = findViewById(R.id.animaisPostosAdocaoButton)
        sairPerfilButton = findViewById(R.id.sairPerfilButton)

        animaisAdotadosButton.setOnClickListener { onVerAnimaisAdotados() }
        animaisPostosAdocaoButton.setOnClickListener { onVerAnimaisPostosAdocao() }
        sairPerfilButton.setOnClickListener { onSair() }

        animaisAdotadosButton.visibility = View.GONE
        sairPerfilButton.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        carregarDados()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(!eProprioPerfil) return super.onCreateOptionsMenu(menu)

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
        var id = intent.getStringExtra("id") ?: ""

        if (id == "") id = NewHomeApplication.idUsuarioAtual
        eProprioPerfil = id == NewHomeApplication.idUsuarioAtual

        NewHomeApplication.usuarioProvider.getUsuario(id, { usuario ->
            onDadosCarregados(usuario)
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun onDadosCarregados(usuario: Usuario) {
        this.usuario = usuario

        nomePerfilText.text = usuario.nome
        descricaoPerfilText.text = usuario.detalhes
        perfilImage.setImageBitmap(usuario.imagem)

        if (eProprioPerfil) {
            animaisAdotadosButton.visibility = View.VISIBLE
            sairPerfilButton.visibility = View.VISIBLE
        }
    }

    private fun onVerAnimaisAdotados() {
        // vai pra lista de animais filtrando apenas os adotados

        val intent = Intent(applicationContext, ListarAnimaisActivity::class.java)
        intent.putExtra("tipo", "adotados")
        startActivity(intent)
    }

    private fun onVerAnimaisPostosAdocao() {
        // vai pra lista de animais filtrando apenas os postos em adocao

        val intent = Intent(applicationContext, ListarAnimaisActivity::class.java)
        intent.putExtra("tipo", "postosAdocao")
        if (!eProprioPerfil) {
            intent.putExtra("usuarioId", usuario.id)
        }
        startActivity(intent)
    }

    private fun onSair() {
        NewHomeApplication.contaProvider.sair({
            val intent = Intent(applicationContext, StartActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun onEditarPerfilClick() {
        // vai para a tela de editar perfil

        if (!eProprioPerfil) {
            return
        }

        val intent = Intent(applicationContext, EditarPerfilActivity::class.java)
        startActivity(intent)
    }
}
