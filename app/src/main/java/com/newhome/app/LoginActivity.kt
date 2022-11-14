package com.newhome.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.newhome.app.R
import com.newhome.app.dto.Credenciais
import com.newhome.app.utils.DialogDisplayer
import com.newhome.app.utils.LoadingDialog
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {
    private lateinit var loginLoginText: EditText
    private lateinit var senhaLoginText: EditText

    private lateinit var cadastrarLoginButton: Button
    private lateinit var fazerLoginLoginButton: Button
    private lateinit var googleSignInButton: Button

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    private val dialog = LoadingDialog(this)
    private lateinit var dialogDisplayer: DialogDisplayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        dialogDisplayer = DialogDisplayer(applicationContext)

        loginLoginText = findViewById(R.id.loginLoginText)
        senhaLoginText = findViewById(R.id.senhaLoginText)

        cadastrarLoginButton = findViewById(R.id.cadastrarLoginButton)
        fazerLoginLoginButton = findViewById(R.id.fazerLoginLoginButton)
        googleSignInButton = findViewById(R.id.googleSignInButton)

        fazerLoginLoginButton.setOnClickListener { lifecycleScope.launch { onFazerLogin() } }
        cadastrarLoginButton.setOnClickListener { onCadastrar() }
        googleSignInButton.setOnClickListener { onGoogleSignIn() }

        configureGoogleSignIn()
    }

    override fun onStart() {
        super.onStart()

        checkGoogleSignedIn()
    }

    override fun onPause() {
        super.onPause()
        dialog.stop()
    }

    private suspend fun onFazerLogin() {
        // loga e vai pra lista de animais

        dialog.start()

        val credenciais = Credenciais()
        credenciais.email = (loginLoginText.text?.toString() ?: "").trim()
        credenciais.senha = senhaLoginText.text?.toString() ?: ""

        try {
            NewHomeApplication.contaService.logar(credenciais).await()
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao fazer login", e)
            dialog.stop()
            return
        }

        // clear task ta dizendo que esse intent é pra limpar as outras task
        // new task usa a atual como a raiz

        try {
            NewHomeApplication.usuarioService.carregarUsuarioAtual().await()
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao carregar usuário", e)

            val intent = Intent(applicationContext, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            dialog.stop()
            return
        }

        dialogDisplayer.display("Logado com sucesso")

        val intent = Intent(applicationContext, ListarAnimaisActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        dialog.stop()
    }

    private fun onCadastrar() {
        // vai pra tela de cadastrar

        val intent = Intent(applicationContext, CriarContaActivity::class.java)
        startActivity(intent)
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                //if (result.resultCode != RESULT_OK) return@registerForActivityResult

                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            }
    }

    private fun checkGoogleSignedIn() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) onSignedIn(account)
    }

    private fun onGoogleSignIn() {
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            onSignedIn(account)
        } catch (e: ApiException) {
            e.printStackTrace()
            dialogDisplayer.display("Falha ao fazer login", e)
        }
    }

    private fun onSignedIn(account: GoogleSignInAccount) {
        // TODO ja ta logado
        dialogDisplayer.display("Logado com sucesso ${account.email}")
    }
}
