package com.newhome.app.services.concrete

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.newhome.app.dao.IContaProvider
import com.newhome.app.dao.IImageProvider
import com.newhome.app.dao.IUsuarioProvider
import com.newhome.app.dto.Credentials
import com.newhome.app.dto.NewAccount
import com.newhome.app.dto.NewUser
import com.newhome.app.services.IContaService
import com.newhome.app.utils.Utils
import kotlinx.coroutines.*

class ContaService(
    private val usuarioProvider: IUsuarioProvider,
    private val contaProvider: IContaProvider,
    private val imageProvider: IImageProvider
) : IContaService {
    override fun getContaID(): String? {
        return contaProvider.getContaID()
    }

    override suspend fun enviarEmailConfirmacao(): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            contaProvider.enviarEmailConfirmacao().await()
        }

    override suspend fun cadastrar(newAccount: NewAccount): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            if (newAccount.name.length < 4 || newAccount.name.length > 128) {
                throw Exception("Nome deve ter entre 4 e 128 caracteres.")
            }
            if (newAccount.age < 18 || newAccount.age > 80) {
                throw Exception("Idade deve estar entre 18 e 80.")
            }
            if (newAccount.password.length < 8 || newAccount.password.length > 64) {
                throw Exception("Senha deve ter entre 8 e 64 caracteres.")
            }

            val credentials = Credentials(newAccount.email, newAccount.password)
            contaProvider.criarConta(credentials).await()

            val uid = contaProvider.getContaID()!!

            val usuario = NewUser(uid, newAccount.name, "", newAccount.age)
            usuarioProvider.createUser(usuario).await()

            try {
                contaProvider.enviarEmailConfirmacao().await()
            } catch (_: Exception) {
            }
        }

    override suspend fun logar(credentials: Credentials): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            contaProvider.logar(credentials).await()

            if (!contaProvider.emailConfirmacaoVerificado()) {
                val enviarEmailTask = contaProvider.enviarEmailConfirmacao()
                val sairTask = contaProvider.sair()

                enviarEmailTask.await()
                sairTask.await() // TODO fix check email without logging in

                throw Exception("Email not verified. Please, verify your email address before signing in.")
            }
        }

    override suspend fun entrarComGoogle(account: GoogleSignInAccount): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            contaProvider.entrarComGoogle(account).await()
            val uid = contaProvider.getContaID()!!
            try {
                usuarioProvider.getUser(uid).await()
            } catch (e: NoSuchElementException) {
                val usuario = NewUser(uid, account.displayName!!, "", 0)
                usuarioProvider.createUser(usuario).await()

                val photoUrl = account.photoUrl ?: return@async
                val image = Utils.decodeBitmap(photoUrl.toString())
                imageProvider.saveUserImage(uid, image).await()
            }
        }

    override fun tentarUsarContaLogada() {
        if (contaProvider.getContaID() == null) throw Exception("User not signed in.")
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
