package com.newhome

import android.app.Application
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.newhome.dao.firebase.*
import com.newhome.services.IAnimalService
import com.newhome.services.IContaService
import com.newhome.services.ISolicitacaoService
import com.newhome.services.IUsuarioService
import com.newhome.services.concrete.AnimalService
import com.newhome.services.concrete.ContaService
import com.newhome.services.concrete.SolicitacaoService
import com.newhome.services.concrete.UsuarioService

class NewHomeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val context = applicationContext

        val imageProvider = FirebaseImageProvider(context)

        val contaProvider = FirebaseContaProvider(context)
        val usuarioProvider = FirebaseUsuarioProvider(Firebase.firestore, imageProvider)
        val animalProvider = FirebaseAnimalProvider(imageProvider)
        val solicitacaoProvider = FirebaseSolicitacaoProvider()

        contaService = ContaService(usuarioProvider, contaProvider)
        usuarioService = UsuarioService(usuarioProvider, contaProvider)
        animalService = AnimalService(animalProvider, usuarioProvider)
        solicitacaoService = SolicitacaoService(solicitacaoProvider, usuarioProvider, animalProvider)

        instance = this
    }

    companion object {
        private lateinit var instance: NewHomeApplication

        var imageSideLength: Int = 1000; private set

        lateinit var contaService: IContaService; private set
        lateinit var usuarioService: IUsuarioService; private set
        lateinit var animalService: IAnimalService; private set
        lateinit var solicitacaoService: ISolicitacaoService; private set
    }
}
