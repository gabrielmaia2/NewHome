package com.newhome.app.dao

import com.newhome.app.dto.SolicitacaoData
import com.newhome.app.dto.SolicitacaoID
import com.newhome.app.dto.SolicitacaoPreviewData
import com.newhome.app.dto.StatusSolicitacao
import kotlinx.coroutines.Deferred

interface ISolicitacaoProvider : IStoreProvider {
    suspend fun solicitarAnimal(animalId: String): Deferred<Unit>

    suspend fun aceitarSolicitacao(
        solicitacaoId: SolicitacaoID,
        detalhesAdocao: String
    ): Deferred<Unit>

    suspend fun rejeitarSolicitacao(solicitacaoId: SolicitacaoID): Deferred<Unit>

    // adotador cancela
    suspend fun cancelarSolicitacao(animalId: String): Deferred<Unit>

    // dono cancela
    suspend fun cancelarSolicitacaoAceita(animalId: String): Deferred<Unit>
}
