package com.newhome.app.services.concrete

import android.graphics.BitmapFactory
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.newhome.app.dao.IContaProvider
import com.newhome.app.dao.IUsuarioProvider
import com.newhome.app.dto.Credenciais
import com.newhome.app.dto.NovaConta
import com.newhome.app.dto.NovoUsuario
import com.newhome.app.services.IContaService
import kotlinx.coroutines.*
import java.net.URL

class ContaService(
    private val usuarioProvider: IUsuarioProvider,
    private val contaProvider: IContaProvider
) : IContaService {
    override fun getContaID(): String? {
        return contaProvider.getContaID()
    }

    override suspend fun enviarEmailConfirmacao(): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            contaProvider.enviarEmailConfirmacao().await()
        }

    override suspend fun cadastrar(novaConta: NovaConta): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
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
            usuarioProvider.createUser(usuario).await()

            try {
                contaProvider.enviarEmailConfirmacao().await()
            } catch (_: Exception) {
            }
        }

    override suspend fun logar(credenciais: Credenciais): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            contaProvider.logar(credenciais).await()

            if (!contaProvider.emailConfirmacaoVerificado()) {
                val enviarEmailTask = contaProvider.enviarEmailConfirmacao()
                val sairTask = contaProvider.sair()

                enviarEmailTask.await()
                sairTask.await() // TODO fix check email without logging in

                throw Exception("Email não foi verificado. Por favor, verifique o email enviado antes de logar.")
            }
        }

    override suspend fun entrarComGoogle(account: GoogleSignInAccount): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            contaProvider.entrarComGoogle(account).await()
            val uid = contaProvider.getContaID()!!
            try {
                usuarioProvider.getUser(uid).await()
            } catch (e: NoSuchElementException) {
                val usuario = NovoUsuario(uid, account.displayName!!, "", 0)
                usuarioProvider.createUser(usuario).await()
                val url = URL(account.photoUrl.toString())
                withContext(Dispatchers.IO) {
                    val image = BitmapFactory.decodeStream(url.openStream())
                    usuarioProvider.setUserImage(uid, image).await()
                }
            }
        }

    override fun tentarUsarContaLogada() {
        if (contaProvider.getContaID() == null) throw Exception("Usuário não está logado.")
    }

    override suspend fun sair(): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            contaProvider.sair().await()
        }

    override suspend fun excluirConta(): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            // TODO implementar
            usuarioProvider.deleteUser(contaProvider.getContaID()!!).await()
            contaProvider.excluirConta().await()
        }
}
