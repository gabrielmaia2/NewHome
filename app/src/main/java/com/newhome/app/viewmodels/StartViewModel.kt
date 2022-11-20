package com.newhome.app.viewmodels

import androidx.lifecycle.ViewModel
import com.newhome.app.NewHomeApplication

class StartViewModel : ViewModel() {
    fun tentarUsarContaLogada() {
        NewHomeApplication.contaService.tentarUsarContaLogada()
    }

    suspend fun carregarUsuarioAtual() {
        NewHomeApplication.usuarioService.carregarUsuarioAtual().await()
    }
}
