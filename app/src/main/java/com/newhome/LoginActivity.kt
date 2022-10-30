package com.newhome

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.newhome.dto.Credenciais
import com.newhome.utils.DialogDisplayer
import com.newhome.utils.LoadingDialog
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var loginLoginText: EditText
    private lateinit var senhaLoginText: EditText

    private lateinit var cadastrarLoginButton: Button
    private lateinit var fazerLoginLoginButton: Button

    private val dialog = LoadingDialog(this)
    private lateinit var dialogDisplayer: DialogDisplayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        dialogDisplayer = DialogDisplayer(applicationContext)

        loginLoginText = findViewById(R.id.loginLoginText)
        senhaLoginText = findViewById(R.id.senhaLoginText)

        cadastrarLoginButton = findViewById(R.id.cadastrarLoginButton)
        fazerLoginLoginButton = findViewById(R.id.fazerLoginLoginButton)

        fazerLoginLoginButton.setOnClickListener { lifecycleScope.launch { onFazerLogin() } }
        cadastrarLoginButton.setOnClickListener { onCadastrar() }
    }

    override fun onPause() {
        super.onPause()
        dialog.stop()
    }

    private suspend fun onFazerLogin() {
        // loga e vai pra lista de animais

        dialog.start()

        val credenciais = Credenciais()
        credenciais.email = (loginLoginText.text?.toString() ?: "").trim()
        credenciais.senha = senhaLoginText.text?.toString() ?: ""

        try {
            NewHomeApplication.contaService.logar(credenciais).await()
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao fazer login", e)
            dialog.stop()
            return
        }

        // clear task ta dizendo que esse intent é pra limpar as outras task
        // new task usa a atual como a raiz

        try {
            NewHomeApplication.usuarioService.carregarUsuarioAtual().await()
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao carregar usuário", e)

            val intent = Intent(applicationContext, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            dialog.stop()
            return
        }

        dialogDisplayer.display("Logado com sucesso")

        val intent = Intent(applicationContext, ListarAnimaisActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        dialog.stop()
    }

    private fun onCadastrar() {
        // vai pra tela de cadastrar

        val intent = Intent(applicationContext, CriarContaActivity::class.java)
        startActivity(intent)
    }
}
