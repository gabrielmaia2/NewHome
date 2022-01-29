package com.newhome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.core.content.IntentCompat

class LoginActivity : AppCompatActivity() {
    private lateinit var loginLoginText: EditText
    private lateinit var senhaLoginText: EditText

    private lateinit var cadastrarLoginButton: Button
    private lateinit var fazerLoginLoginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        loginLoginText = findViewById(R.id.loginLoginText)
        senhaLoginText = findViewById(R.id.senhaLoginText)

        cadastrarLoginButton = findViewById(R.id.cadastrarLoginButton)
        fazerLoginLoginButton = findViewById(R.id.fazerLoginLoginButton)

        fazerLoginLoginButton.setOnClickListener { onFazerLogin() }
        cadastrarLoginButton.setOnClickListener { onCadastrar() }
    }

    private fun onFazerLogin() {
        // loga e vai pra lista de animais

        // TODO tentar logar (se der certo vai pra lista senao continua nessa tela e diz o erro)

        // clear task ta dizendo que esse intent é pra limpar as outras task
        // new task usa a atual como a raiz

        val intent = Intent(applicationContext, ListarAnimaisActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun onCadastrar() {
        // vai pra tela de cadastrar

        val intent = Intent(applicationContext, CriarContaActivity::class.java)
        startActivity(intent)
    }
}
