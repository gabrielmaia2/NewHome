package com.newhome.app

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.newhome.app.fragments.AnimalPreviewFragment
import com.newhome.app.utils.LoadingDialog
import com.newhome.app.viewmodels.SolicitacaoViewModel
import kotlinx.coroutines.launch
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.newhome.app.R
import com.newhome.app.dto.Solicitacao
import com.newhome.app.dto.SolicitacaoID
import com.newhome.app.dto.StatusSolicitacao
import com.newhome.app.utils.DialogDisplayer

class SolicitacaoActivity : AppCompatActivity() {
    private val viewModel: SolicitacaoViewModel by viewModels()

    private lateinit var solicitacaoPerfilImage: ImageView

    private lateinit var solicitacaoNomeText: TextView
    private lateinit var solicitacaoPerfilDescricaoText: TextView

    private lateinit var animalDetalhesSolicitacaoFragment: AnimalPreviewFragment

    private lateinit var aceitarSolicitacaoButton: Button
    private lateinit var rejeitarSolicitacaoButton: Button
    private lateinit var voltarButton: Button
    private lateinit var animalBuscadoButton: Button
    private lateinit var cancelarAdocaoButton: Button

    private lateinit var adicionarDetalhesLauncher: ActivityResultLauncher<Intent>

    private val loadingDialog = LoadingDialog(this)
    private val dialog = LoadingDialog(this)
    private lateinit var dialogDisplayer: DialogDisplayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitacao)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Solicitação de adoção"

        dialogDisplayer = DialogDisplayer(applicationContext)

        solicitacaoPerfilImage = findViewById(R.id.solicitacaoPerfilImage)

        solicitacaoNomeText = findViewById(R.id.solicitacaoNomeText)
        solicitacaoPerfilDescricaoText = findViewById(R.id.solicitacaoPerfilDescricaoText)

        animalDetalhesSolicitacaoFragment =
            supportFragmentManager.findFragmentById(R.id.animalDetalhesSolicitacaoFragment) as AnimalPreviewFragment

        aceitarSolicitacaoButton = findViewById(R.id.aceitarSolicitacaoButton)
        rejeitarSolicitacaoButton = findViewById(R.id.rejeitarSolicitacaoButton)
        voltarButton = findViewById(R.id.voltarSolicitacaoButton)
        animalBuscadoButton = findViewById(R.id.animalBuscadoSolicitacaoButton)
        cancelarAdocaoButton = findViewById(R.id.cancelarAdocaoSolicitacaoButton)

        lifecycleScope.launch { loadData() }
        lifecycleScope.launch { watchForModelChanges() }
        setAdicionarDetalhesLauncher()
        solicitacaoPerfilImage.setOnClickListener { onVerSolicitador() }
        aceitarSolicitacaoButton.setOnClickListener { onAceitar() }
        rejeitarSolicitacaoButton.setOnClickListener { lifecycleScope.launch { onRejeitar() } }
        voltarButton.setOnClickListener { onVoltar() }
        animalBuscadoButton.setOnClickListener { lifecycleScope.launch { onAnimalBuscado() } }
        cancelarAdocaoButton.setOnClickListener { lifecycleScope.launch { onCancelar() } }

        animalBuscadoButton.visibility = View.GONE
        cancelarAdocaoButton.visibility = View.GONE
        aceitarSolicitacaoButton.visibility = View.GONE
        rejeitarSolicitacaoButton.visibility = View.GONE
        voltarButton.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        loadingDialog.stop()
        dialog.stop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private suspend fun loadData() {
        loadingDialog.start()

        try {
            val id = intent.getSerializableExtra("id") as SolicitacaoID
            viewModel.loadDataAsync(id)
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao buscar solicitação", e)
            finish()
        }

        loadingDialog.stop()
    }

    private suspend fun watchForModelChanges() {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.solicitacaoState.collect { s -> loadSolicitacao(s) }
            }
            launch {
                viewModel.statusState.collect { s -> loadStatus(s) }
            }
        }
    }

    private fun loadSolicitacao(solicitacao: Solicitacao) {
        solicitacaoNomeText.text = solicitacao.solicitador!!.nome
        solicitacaoPerfilDescricaoText.text = solicitacao.solicitador!!.detalhes
        solicitacaoPerfilImage.setImageBitmap(solicitacao.solicitador!!.imagem)

        animalDetalhesSolicitacaoFragment.carregarAnimal(solicitacao.animal!!)
    }

    private fun loadStatus(status: StatusSolicitacao) {
        if (status.solicitacaoAceita) {
            animalBuscadoButton.visibility = View.VISIBLE
            cancelarAdocaoButton.visibility = View.VISIBLE
        } else {
            aceitarSolicitacaoButton.visibility = View.VISIBLE
            rejeitarSolicitacaoButton.visibility = View.VISIBLE
            voltarButton.visibility = View.VISIBLE
        }
    }

    private fun setAdicionarDetalhesLauncher() {
        adicionarDetalhesLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != RESULT_OK) return@registerForActivityResult

                val detalhes = result.data!!.getStringExtra("detalhes")!!
                lifecycleScope.launch { aceitarSolicitacao(detalhes) }
            }
    }

    private suspend fun aceitarSolicitacao(detalhes: String) {
        dialog.start()

        try {
            viewModel.aceitarSolicitacao(detalhes)
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao aceitar solicitação", e)
            dialog.stop()
            return
        }

        dialogDisplayer.display("Solicitação aceita com sucesso")
        finish()
        dialog.stop()
    }

    private fun onVerSolicitador() {
        // vai pra tela do solicitador

        val intent = Intent(applicationContext, PerfilActivity::class.java)
        intent.putExtra("id", viewModel.solicitacaoState.value.id!!)
        startActivity(intent)
    }

    private fun onAceitar() {
        // aceita solicitacao

        val intent = Intent(applicationContext, SolicitacaoAdicionarDetalhesActivity::class.java)
        adicionarDetalhesLauncher.launch(intent)
    }

    private suspend fun onRejeitar() {
        // rejeita solicitacao

        dialog.start()

        try {
            viewModel.rejeitarSolicitacao()
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao rejeitar solicitação", e)
            dialog.stop()
            return
        }

        dialogDisplayer.display("Solicitação rejeitada com sucesso")
        finish()
        dialog.stop()
    }

    private fun onVoltar() {
        // volta sem fazer nada
        finish()
    }

    private suspend fun onAnimalBuscado() {
        // vai pra tela de detalhes do animal

        dialog.start()

        try {
            viewModel.animalBuscado()
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao marcar animal como enviado", e)
            dialog.stop()
            return
        }

        dialogDisplayer.display("Animal doado com sucesso")
        finish()
        dialog.stop()
    }

    private suspend fun onCancelar() {
        // cancela adocao

        dialog.start()

        try {
            viewModel.cancelarSolicitacao()
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao cancelar solicitação", e)
            dialog.stop()
            return
        }

        dialogDisplayer.display("Solicitação cancelada com sucesso")
        finish()
        dialog.stop()
    }
}
