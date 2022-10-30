package com.newhome.viewmodels

import androidx.lifecycle.ViewModel
import com.newhome.NewHomeApplication

class StartViewModel : ViewModel() {
    fun tentarUsarContaLogada() {
        NewHomeApplication.contaService.tentarUsarContaLogada()
    }

    suspend fun carregarUsuarioAtual() {
        NewHomeApplication.usuarioService.carregarUsuarioAtual().await()
    }
}
