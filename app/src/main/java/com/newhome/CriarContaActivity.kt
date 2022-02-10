package com.newhome

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.newhome.dto.NovaConta

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

        val novaConta = NovaConta()
        novaConta.email = (emailText.text?.toString() ?: "").trim()
        novaConta.senha = senhaText.text?.toString() ?: ""
        novaConta.nome = nomeText.text?.toString() ?: ""
        novaConta.idade = (idadeText.text?.toString() ?: "0").toInt()

        NewHomeApplication.contaProvider.cadastrar(novaConta, {
            Toast.makeText(applicationContext, "Cadastrado com sucesso.", Toast.LENGTH_SHORT).show()

            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun onFazerLogin() {
        // vai pra tela de login

        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
    }
}
