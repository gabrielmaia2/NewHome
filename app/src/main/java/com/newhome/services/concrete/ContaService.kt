package com.newhome.services.concrete

import com.newhome.dao.IContaProvider
import com.newhome.dao.IUsuarioProvider
import com.newhome.dto.Credenciais
import com.newhome.dto.NovaConta
import com.newhome.dto.NovoUsuario
import com.newhome.services.IContaService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class ContaService(
    private val usuarioProvider: IUsuarioProvider,
    private val contaProvider: IContaProvider
) : IContaService {
    override fun getContaID(): String? {
        return contaProvider.getContaID()
    }

    override suspend fun enviarEmailConfirmacao(): Deferred<Unit> =
        CoroutineScope(Dispatchers.IO).async {
            contaProvider.enviarEmailConfirmacao().await()
        }

    override suspend fun cadastrar(novaConta: NovaConta): Deferred<Unit> =
        CoroutineScope(Dispatchers.IO).async {
            if (novaConta.nome.length < 4 || novaConta.nome.length > 128) {
                throw Exception("Nome deve ter entre 4 e 128 caracteres.")
            }
            if (novaConta.idade < 18 || novaConta.idade > 80) {
                throw Exception("Idade deve estar entre 18 e 80.")
            }
            if (novaConta.senha.length < 8 || novaConta.senha.length > 64) {
                throw Exception("Senha deve ter entre 8 e 64 caracteres.")
            }

            val credenciais = Credenciais(novaConta.email, novaConta.senha)
            contaProvider.criarConta(credenciais).await()

            val uid = contaProvider.getContaID()!!

            val usuario = NovoUsuario(uid, novaConta.nome, "", novaConta.idade)
            usuarioProvider.criarUsuario(usuario).await()

            try {
                contaProvider.enviarEmailConfirmacao().await()
            } catch (_: Exception) {
            }
        }

    override suspend fun logar(credenciais: Credenciais): Deferred<Unit> =
        CoroutineScope(Dispatchers.IO).async {
            contaProvider.logar(credenciais).await()

            if (!contaProvider.emailConfirmacaoVerificado()) {
                val enviarEmailTask = contaProvider.enviarEmailConfirmacao()
                val sairTask = contaProvider.sair()

                enviarEmailTask.await()
                sairTask.await() // TODO fix check email without logging in

                throw Exception("Email não foi verificado. Por favor, verifique o email enviado antes de logar.")
            }
        }

    override fun tentarUsarContaLogada() {
        if (contaProvider.getContaID() == null) throw Exception("Usuário não está logado.")
    }

    override suspend fun sair(): Deferred<Unit> =
        CoroutineScope(Dispatchers.IO).async {
            contaProvider.sair().await()
        }

    override suspend fun excluirConta(): Deferred<Unit> =
        CoroutineScope(Dispatchers.IO).async {
            // TODO implementar
            usuarioProvider.deleteUsuario(contaProvider.getContaID()!!).await()
            contaProvider.excluirConta().await()
        }
}
