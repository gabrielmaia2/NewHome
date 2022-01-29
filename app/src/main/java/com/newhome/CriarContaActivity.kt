package com.newhome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText

class CriarContaActivity : AppCompatActivity() {
    private lateinit var nomeText: EditText
    private lateinit var idadeText: EditText
    private lateinit var emailText: EditText
    private lateinit var senhaText: EditText

    private lateinit var cadastrarButton: Button
    private lateinit var fazerLoginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_conta)
        supportActionBar?.hide()

        nomeText = findViewById(R.id.nomeCriarContaText)
        idadeText = findViewById(R.id.idadeCriarContaText)
        emailText = findViewById(R.id.emailCriarContaText)
        senhaText = findViewById(R.id.senhaCriarContaText)

        cadastrarButton = findViewById(R.id.cadastrarCriarContaButton)
        fazerLoginButton = findViewById(R.id.fazerLoginCriarContaButton)

        cadastrarButton.setOnClickListener { onCadastrar() }
        fazerLoginButton.setOnClickListener { onFazerLogin() }
    }

    private fun onCadastrar() {
        // cadastra e vai pra lista de animais

        // TODO tentar cadastrar (se der certo vai pra lista senao continua nessa tela e diz o erro)

        val intent = Intent(applicationContext, ListarAnimaisActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun onFazerLogin() {
        // vai pra tela de login

        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
    }
}
