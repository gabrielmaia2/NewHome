package com.newhome.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.newhome.app.dto.Usuario
import com.newhome.app.utils.DialogDisplayer
import com.newhome.app.utils.LoadingDialog
import kotlinx.coroutines.launch

class PerfilActivity : AppCompatActivity() {
    private lateinit var perfilImage: ImageView

    private lateinit var nomePerfilText: TextView
    private lateinit var descricaoPerfilText: TextView

    private lateinit var animaisAdotadosButton: Button
    private lateinit var animaisPostosAdocaoButton: Button
    private lateinit var sairPerfilButton: Button

    private lateinit var usuario: Usuario
    private var eProprioPerfil = false

    private val loadingDialog = LoadingDialog(this)
    private val dialog = LoadingDialog(this)
    private lateinit var dialogDisplayer: DialogDisplayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Perfil"

        dialogDisplayer = DialogDisplayer(applicationContext)

        perfilImage = findViewById(R.id.perfilImage)

        nomePerfilText = findViewById(R.id.nomePerfilText)
        descricaoPerfilText = findViewById(R.id.descricaoPerfilText)

        animaisAdotadosButton = findViewById(R.id.animaisAdotadosButton)
        animaisPostosAdocaoButton = findViewById(R.id.animaisPostosAdocaoButton)
        sairPerfilButton = findViewById(R.id.sairPerfilButton)

        animaisAdotadosButton.setOnClickListener { onVerAnimaisAdotados() }
        animaisPostosAdocaoButton.setOnClickListener { onVerAnimaisPostosAdocao() }
        sairPerfilButton.setOnClickListener { lifecycleScope.launch { onSair() } }

        sairPerfilButton.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { carregarDados() }
    }

    override fun onPause() {
        super.onPause()
        loadingDialog.stop()
        dialog.stop()
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

    private suspend fun carregarDados() {
        val id =
            intent.getStringExtra("id") ?: NewHomeApplication.usuarioService.getUsuarioAtual().id

        eProprioPerfil = id == NewHomeApplication.usuarioService.getUsuarioAtual().id

        if (eProprioPerfil) {
            usuario = NewHomeApplication.usuarioService.getUsuarioAtual()
        } else {
            loadingDialog.start()

            try {
                val u = NewHomeApplication.usuarioService.getUsuario(id).await()
                usuario = Usuario.fromData(u)
                perfilImage.setImageBitmap(usuario.imagem)
            } catch (e: Exception) {
                dialogDisplayer.display("Falha ao carregar perfil", e)
                finish()
                loadingDialog.stop()
                return
            }

            loadingDialog.stop()
        }

        nomePerfilText.text = usuario.nome
        descricaoPerfilText.text = usuario.detalhes
        perfilImage.setImageBitmap(usuario.imagem)

        sairPerfilButton.visibility = View.VISIBLE
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

    private suspend fun onSair() {
        dialog.start()

        try {
            NewHomeApplication.contaService.sair().await()
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao tentar sair", e)
            dialog.stop()
            return
        }

        val intent = Intent(applicationContext, StartActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        dialog.stop()
    }

    private fun onEditarPerfilClick() {
        // vai para a tela de editar perfil

        if (!eProprioPerfil)
            return

        val intent = Intent(applicationContext, EditarPerfilActivity::class.java)
        startActivity(intent)
    }
}
