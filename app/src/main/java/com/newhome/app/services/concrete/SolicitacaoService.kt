package com.newhome.app.services.concrete

import com.newhome.app.dao.IAnimalProvider
import com.newhome.app.dao.ISolicitacaoProvider
import com.newhome.app.dao.IUsuarioProvider
import com.newhome.app.dto.SolicitacaoAsync
import com.newhome.app.dto.SolicitacaoID
import com.newhome.app.dto.SolicitacaoPreviewAsync
import com.newhome.app.dto.StatusSolicitacao
import com.newhome.app.services.ISolicitacaoService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class SolicitacaoService(
    private val solicitacaoProvider: ISolicitacaoProvider,
    private val usuarioProvider: IUsuarioProvider,
    private val animalProvider: IAnimalProvider
) : ISolicitacaoService {
    override suspend fun getTodasSolicitacoes(): Deferred<List<SolicitacaoPreviewAsync>> =
        CoroutineScope(Dispatchers.Main).async {
            solicitacaoProvider.getTodasSolicitacoes().await().map { s ->
                SolicitacaoPreviewAsync(
                    s.id,
                    s.titulo,
                    s.descricao,
                    usuarioProvider.getUserImage(s.id!!.solicitadorID)
                )
            }
        }

    override suspend fun getTodasSolicitacoesAnimal(animalId: String): Deferred<List<SolicitacaoPreviewAsync>> =
        CoroutineScope(Dispatchers.Main).async {
            solicitacaoProvider.getTodasSolicitacoesAnimal(animalId).await().map { s ->
                SolicitacaoPreviewAsync(
                    s.id,
                    s.titulo,
                    s.descricao,
                    usuarioProvider.getUserImage(s.id!!.solicitadorID)
                )
            }
        }

    override suspend fun getSolicitacao(solicitacaoId: SolicitacaoID): Deferred<SolicitacaoAsync> =
        CoroutineScope(Dispatchers.Main).async {
            val solicitadorTask = usuarioProvider.getUserImage(solicitacaoId.solicitadorID)
            val animalTask = animalProvider.getImagemAnimal(solicitacaoId.animalID)
            val s = solicitacaoProvider.getSolicitacao(solicitacaoId).await()

            val solicitacao = SolicitacaoAsync.fromData(s)
            solicitacao.solicitador!!.getImagem = solicitadorTask
            solicitacao.animal!!.getImagem = animalTask

            return@async solicitacao
        }

    override suspend fun getStatusSolicitacao(animalId: String): Deferred<StatusSolicitacao> =
        CoroutineScope(Dispatchers.Main).async {
            solicitacaoProvider.getStatusSolicitacao(animalId).await()
        }

    override suspend fun solicitarAnimal(animalId: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            solicitacaoProvider.solicitarAnimal(animalId).await()
        }

    override suspend fun aceitarSolicitacao(
        solicitacaoId: SolicitacaoID,
        detalhesAdocao: String
    ): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            solicitacaoProvider.aceitarSolicitacao(solicitacaoId, detalhesAdocao).await()
        }

    override suspend fun rejeitarSolicitacao(solicitacaoId: SolicitacaoID): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            solicitacaoProvider.rejeitarSolicitacao(solicitacaoId).await()
        }

    override suspend fun cancelarSolicitacao(animalId: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            solicitacaoProvider.cancelarSolicitacao(animalId).await()
        }

    override suspend fun cancelarSolicitacaoAceita(animalId: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            solicitacaoProvider.cancelarSolicitacaoAceita(animalId).await()
        }
}