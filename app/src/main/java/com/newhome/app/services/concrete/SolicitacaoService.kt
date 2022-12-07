package com.newhome.app.services.concrete

import com.newhome.app.dao.*
import com.newhome.app.dto.*
import com.newhome.app.services.ISolicitacaoService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class SolicitacaoService(
    private val contaProvider: IContaProvider,
    private val usuarioProvider: IUsuarioProvider,
    private val animalProvider: IAnimalProvider,
    private val solicitacaoProvider: ISolicitacaoProvider,
    private val imageProvider: IImageProvider
) : ISolicitacaoService {
    override suspend fun getTodasSolicitacoes(): Deferred<List<SolicitacaoPreviewAsync>> =
        CoroutineScope(Dispatchers.Main).async {
            val cuid = contaProvider.getContaID()!!
            val pAnimals =
                animalProvider.getAnimalsIdsFromList(cuid, AnimalList.placedForAdoption).await()

            val requests = animalProvider.runTransaction { t ->
                val rs = ArrayList<SolicitacaoPreviewData>()
                for (aid in pAnimals) {
                    val animal = animalProvider.getAnimal(t, aid) ?: continue
                    for (uid in animal.requestersIds) {
                        val requester = usuarioProvider.getUser(t, uid) ?: continue

                        val s = SolicitacaoPreviewData(
                            SolicitacaoID(aid, uid),
                            requester.name,
                            "Quer adotar ${animal.name}"
                        )
                        rs.add(s)
                    }
                }
                return@runTransaction rs
            }.await()

            requests.map { s ->
                SolicitacaoPreviewAsync(
                    s.id,
                    s.titulo,
                    s.descricao,
                    imageProvider.getUserImage(s.id!!.solicitadorID)
                )
            }
        }

    override suspend fun getTodasSolicitacoesAnimal(animalId: String): Deferred<List<SolicitacaoPreviewAsync>?> =
        CoroutineScope(Dispatchers.Main).async {
            val requests = animalProvider.runTransaction { t ->
                val rs = ArrayList<SolicitacaoPreviewData>()
                val animal = animalProvider.getAnimal(t, animalId) ?: return@runTransaction null
                for (uid in animal.requestersIds) {
                    val requester = usuarioProvider.getUser(t, uid) ?: continue

                    val s = SolicitacaoPreviewData(
                        SolicitacaoID(animalId, uid),
                        requester.name,
                        "Quer adotar ${animal.name}"
                    )
                    rs.add(s)
                }
                return@runTransaction rs
            }.await() ?: return@async null

            requests.map { s ->
                SolicitacaoPreviewAsync(
                    s.id,
                    s.titulo,
                    s.descricao,
                    imageProvider.getUserImage(s.id!!.solicitadorID)
                )
            }
        }

    override suspend fun getSolicitacao(solicitacaoId: SolicitacaoID): Deferred<SolicitacaoAsync> =
        CoroutineScope(Dispatchers.Main).async {
            val requestTask = animalProvider.runTransaction { t ->
                val animal = animalProvider.getAnimal(t, solicitacaoId.animalID)
                val requester = usuarioProvider.getUser(t, solicitacaoId.solicitadorID)
                return@runTransaction SolicitacaoData(
                    solicitacaoId,
                    animal,
                    requester
                )
            }

            val solicitadorTask = imageProvider.getUserImage(solicitacaoId.solicitadorID)
            val animalTask = imageProvider.getAnimalImage(solicitacaoId.animalID)
            val request = requestTask.await()

            val solicitacao = SolicitacaoAsync.fromData(request)
            solicitacao.solicitador!!.getImage = solicitadorTask
            solicitacao.animal!!.getImage = animalTask

            return@async solicitacao
        }

    override suspend fun getStatusSolicitacao(animalId: String): Deferred<StatusSolicitacao?> =
        CoroutineScope(Dispatchers.Main).async {
            val animal = animalProvider.runTransaction { t ->
                animalProvider.getAnimal(t, animalId)
            }.await() ?: return@async null
            return@async StatusSolicitacao(
                animal.requestersIds.isNotEmpty(),
                animal.beingAdopted,
                animal.requestDetails,
            )
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