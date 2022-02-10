package com.newhome

import android.app.Application
import android.content.Intent
import android.widget.Toast
import com.newhome.dao.IAnimalProvider
import com.newhome.dao.IContaProvider
import com.newhome.dao.ISolicitacaoProvider
import com.newhome.dao.IUsuarioProvider
import com.newhome.dao.firebase.FirebaseAnimalProvider
import com.newhome.dao.firebase.FirebaseContaProvider
import com.newhome.dao.firebase.FirebaseSolicitacaoProvider
import com.newhome.dao.firebase.FirebaseUsuarioProvider
import com.newhome.dto.Usuario

class NewHomeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val context = applicationContext

        contaProvider = FirebaseContaProvider(context)
        animalProvider = FirebaseAnimalProvider(context)
        solicitacaoProvider = FirebaseSolicitacaoProvider(context)
        usuarioProvider = FirebaseUsuarioProvider(context)

        instance = this
    }

    companion object {
        private lateinit var instance: NewHomeApplication

        var imageSideLength: Int = 1000; private set
        lateinit var contaProvider: IContaProvider; private set
        lateinit var animalProvider: IAnimalProvider; private set
        lateinit var solicitacaoProvider: ISolicitacaoProvider; private set
        lateinit var usuarioProvider: IUsuarioProvider; private set
        lateinit var idUsuarioAtual: String; private set

        fun carregarUsuarioAtual(onSuccess: () -> Unit) {
            usuarioProvider.getUsuarioAtual({ usuario ->
                idUsuarioAtual = usuario.id
                onSuccess()
            }, {
                Toast.makeText(
                    instance.applicationContext,
                    "Ocorreu um erro ao carregar usuário.",
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(instance.applicationContext, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                instance.startActivity(intent)
            })
        }
    }
}
