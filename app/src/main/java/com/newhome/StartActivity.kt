package com.newhome

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.newhome.utils.DialogDisplayer
import com.newhome.viewmodels.StartViewModel
import kotlinx.coroutines.launch

class StartActivity : AppCompatActivity() {
    private val viewModel: StartViewModel by viewModels()

    private lateinit var dialogDisplayer: DialogDisplayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        supportActionBar?.hide()

        dialogDisplayer = DialogDisplayer(applicationContext)
    }

    // inicia a activity
    // rodar a função
    // pegando o objeto e rodando dentro dessa função
    override fun onStart() {
        super.onStart()

        setInterval()
    }

    // handler é pra rodar a função depois de um tempo 3 segundos 3mil milisegundo
    // instanciando o handler e  passando o looper pra dentro dele
    // this é o objeto que to usando noo momento
    // this tem uma variavel com o mesmo nome da classe por isso usa o this
    private fun setInterval() {
        Handler(Looper.getMainLooper()).postDelayed({ lifecycleScope.launch { goToApp() } }, 0)
    }

    // cria um itentet para a tela de login e depois vai para a prox tela atividade
    // this pode passar tbm o objeto para outra função
    // cria instancia intent e guarando dentro do intent
    // intent é a variavel que leva para outra tela
    private suspend fun goToApp() {
        try {
            viewModel.tentarUsarContaLogada()
        } catch (e: Exception) {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        try {
            viewModel.carregarUsuarioAtual()
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao carregar usuário", e)

            val intent = Intent(applicationContext, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            return
        }

        val intent = Intent(applicationContext, ListarAnimaisActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent) // passando a variavel dentro dela (inicia a atividade)
        finish() // fecha a atual pra nao poder voltar depois com o botao de voltar
    }
}
