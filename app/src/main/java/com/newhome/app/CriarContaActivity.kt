package com.newhome.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.newhome.app.R
import com.newhome.app.dto.NovaConta
import com.newhome.app.utils.DialogDisplayer
import com.newhome.app.utils.LoadingDialog
import kotlinx.coroutines.launch

class CriarContaActivity : AppCompatActivity() {
    private lateinit var nomeText: EditText
    private lateinit var idadeText: EditText
    private lateinit var emailText: EditText
    private lateinit var senhaText: EditText

    private lateinit var cadastrarButton: Button
    private lateinit var fazerLoginButton: Button

    private val dialog = LoadingDialog(this)
    private lateinit var dialogDisplayer: DialogDisplayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_conta)
        supportActionBar?.hide()

        dialogDisplayer = DialogDisplayer(applicationContext)

        nomeText = findViewById(R.id.nomeCriarContaText)
        idadeText = findViewById(R.id.idadeCriarContaText)
        emailText = findViewById(R.id.emailCriarContaText)
        senhaText = findViewById(R.id.senhaCriarContaText)

        cadastrarButton = findViewById(R.id.cadastrarCriarContaButton)
        fazerLoginButton = findViewById(R.id.fazerLoginCriarContaButton)

        cadastrarButton.setOnClickListener { lifecycleScope.launch { onCadastrar() } }
        fazerLoginButton.setOnClickListener { onFazerLogin() }
    }

    override fun onPause() {
        super.onPause()
        dialog.stop()
    }

    private suspend fun onCadastrar() {
        // cadastra e vai pra lista de animais

        dialog.start()

        try {
            val idade = idadeText.text?.toString() ?: "0"

            val novaConta = NovaConta(
                (emailText.text?.toString() ?: "").trim(),
                senhaText.text?.toString() ?: "",
                nomeText.text?.toString() ?: "",
                (if (idade.isEmpty()) "0" else idade).toInt()
            )

            NewHomeApplication.contaService.cadastrar(novaConta).await()
        } catch (e:Exception) {
            dialogDisplayer.display("Falha ao realizar cadastro", e)
            dialog.stop()
            return
        }

        dialogDisplayer.display("Cadastro realizado com sucesso")

        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
        dialog.stop()
    }

    private fun onFazerLogin() {
        // vai pra tela de login

        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
    }
}
