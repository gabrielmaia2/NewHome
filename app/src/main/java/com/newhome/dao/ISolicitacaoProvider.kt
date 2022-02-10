package com.newhome.dao

import com.newhome.dto.Solicitacao
import com.newhome.dto.SolicitacaoID
import com.newhome.dto.SolicitacaoPreview
import com.newhome.dto.StatusSolicitacao

interface ISolicitacaoProvider {
    fun getTodasSolicitacoes(
        onSuccess: (solicitacoes: List<SolicitacaoPreview>) -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun getTodasSolicitacoesAnimal(
        animalId: String,
        onSuccess: (solicitacoes: List<SolicitacaoPreview>) -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun getSolicitacao(
        solicitacaoId: SolicitacaoID,
        onSuccess: (solicitacao: Solicitacao) -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun getStatusSolicitacao(
        solicitacaoId: SolicitacaoID,
        onSuccess: (status: StatusSolicitacao) -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun solicitarAnimal(
        animalId: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun aceitarSolicitacao(
        solicitacaoId: SolicitacaoID,
        detalhesAdocao: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun rejeitarSolicitacao(
        solicitacaoId: SolicitacaoID,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    // adotador cancela
    fun cancelarSolicitacao(
        animalId: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    // dono cancela
    fun cancelarSolicitacaoAceita(
        animalId: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    )
}
