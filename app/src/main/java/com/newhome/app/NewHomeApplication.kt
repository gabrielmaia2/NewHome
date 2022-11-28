package com.newhome.app

import android.app.Application
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.newhome.app.dao.firebase.*
import com.newhome.app.services.IAnimalService
import com.newhome.app.services.IContaService
import com.newhome.app.services.ISolicitacaoService
import com.newhome.app.services.IUsuarioService
import com.newhome.app.services.concrete.*

class NewHomeApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val context = applicationContext
        val auth = FirebaseAuth.getInstance()
        val authUI = AuthUI.getInstance()

        val imageProvider = FirebaseImageProvider(context, Firebase.storage)

        val contaProvider = FirebaseContaProvider(auth, authUI,context)
        val usuarioProvider = FirebaseUsuarioProvider(Firebase.firestore, imageProvider)
        val animalProvider = FirebaseAnimalProvider(Firebase.firestore, imageProvider)
        val animalProviderTemp = FirebaseAnimalProviderTemp(Firebase.firestore, imageProvider)
        val solicitacaoProvider = FirebaseSolicitacaoProvider()

        contaService = ContaService(usuarioProvider, contaProvider)
        usuarioService = UsuarioService(usuarioProvider, contaProvider)
        animalService = AnimalService(animalProvider, usuarioProvider, contaProvider, imageProvider)
        solicitacaoService = SolicitacaoService(solicitacaoProvider, usuarioProvider, animalProviderTemp)

        instance = this
    }

    companion object {
        private lateinit var instance: NewHomeApplication

        val imageSideLength: Int = 1000

        lateinit var contaService: IContaService; private set
        lateinit var usuarioService: IUsuarioService; private set
        lateinit var animalService: IAnimalService; private set
        lateinit var solicitacaoService: ISolicitacaoService; private set
    }
}
