package com.newhome

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.newhome.dto.ContaLogin

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

        val contaLogin = ContaLogin()
        contaLogin.email = (loginLoginText.text?.toString() ?: "").trim()
        contaLogin.senha = senhaLoginText.text?.toString() ?: ""

        NewHomeApplication.contaProvider.logar(contaLogin, {
            // clear task ta dizendo que esse intent é pra limpar as outras task
            // new task usa a atual como a raiz

            NewHomeApplication.carregarUsuarioAtual {
                Toast.makeText(applicationContext, "Logado com sucesso.", Toast.LENGTH_SHORT).show()

                val intent = Intent(applicationContext, ListarAnimaisActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun onCadastrar() {
        // vai pra tela de cadastrar

        val intent = Intent(applicationContext, CriarContaActivity::class.java)
        startActivity(intent)
    }
}
