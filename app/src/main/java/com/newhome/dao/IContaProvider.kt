package com.newhome.dao

import android.content.Context
import com.newhome.dto.ContaLogin
import com.newhome.dto.NovaConta

interface IContaProvider {
    fun cadastrar(
        novaConta: NovaConta,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun logar(
        contaLogin: ContaLogin,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun tentarUsarContaLogada(
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun sair(
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun excluirConta(
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    )
}
