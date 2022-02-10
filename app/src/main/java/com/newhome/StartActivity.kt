package com.newhome

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        supportActionBar?.hide()
    }

    // inicia a activity
    // rodar a função
    // pegando o objeto e rodando dentro dessa função
    override fun onStart() {
        super.onStart()

        setIntervalo()
    }

    // handler é pra rodar a função depois de um tempo 3 segundos 3mil milisegundo
    // instanciando o handler e  passando o looper pra dentro dele
    // this é o objeto que to usando noo momento
    // this tem uma variavel com o mesmo nome da classe por isso usa o this
    private fun setIntervalo() {
        Handler(Looper.getMainLooper()).postDelayed(this::goToApp, 3000)
    }

    // cria um itentet para a tela de login e depois vai para a prox tela atividade
    // this pode passar tbm o objeto para outra função
    // cria instancia intent e guarando dentro do intent
    // intent é a variavel que leva para outra tela
    private fun goToApp() {
        NewHomeApplication.contaProvider.tentarUsarContaLogada({
            val intent = Intent(applicationContext, ListarAnimaisActivity::class.java)
            startActivity(intent) // passando a variavel dentro dela (inicia a atividade)
            finish() // fecha a atual pra nao poder voltar depois com o botao de voltar
        }, {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
        })
    }
}
