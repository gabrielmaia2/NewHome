package com.newhome.app

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.newhome.app.R
import com.newhome.app.adapters.SolicitacaoAdapter
import com.newhome.app.dto.SolicitacaoPreview
import com.newhome.app.dto.SolicitacaoPreviewAsync
import com.newhome.app.utils.DialogDisplayer
import com.newhome.app.utils.LoadingDialog
import kotlinx.coroutines.launch

class ListaSolicitacaoActivity : AppCompatActivity() {
    private lateinit var semSolicitacoesText: TextView

    private lateinit var listaSolicitacoes: ListView
    private lateinit var adapter: SolicitacaoAdapter

    // se tiver id, filtra solicitacoes feitas pro animal com esse id
    // senao, mostra todas solicitacoes recebidas pra todos os animais
    private var animalId: String = ""

    private val dialog = LoadingDialog(this)
    private lateinit var dialogDisplayer: DialogDisplayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_solicitacao)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Solicitações"

        dialogDisplayer = DialogDisplayer(applicationContext)

        semSolicitacoesText = findViewById(R.id.semSolicitacoesText)
        semSolicitacoesText.visibility = View.GONE

        listaSolicitacoes = findViewById(R.id.listaSolicitacoes)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { carregarDados() }
    }

    override fun onPause() {
        super.onPause()
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

    private suspend fun carregarDados() {
        dialog.start()

        animalId = intent.getStringExtra("animalId") ?: ""

        // carrega solicitacoes do database

        val solicitacoes = try {
            if (animalId == "") {
                NewHomeApplication.solicitacaoService.getTodasSolicitacoes().await()
            } else {
                NewHomeApplication.solicitacaoService.getTodasSolicitacoesAnimal(animalId).await()
            }
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao buscar solicitações", e)
            dialog.stop()
            return
        }

        onSolicitacoesCarregadas(solicitacoes)
        carregaImagensSolicitacoes(solicitacoes)
        dialog.stop()
    }

    private fun onSolicitacoesCarregadas(solicitacoes: List<SolicitacaoPreviewAsync>) {
        val sols = solicitacoes.map { s -> SolicitacaoPreview(s.id, s.titulo, s.descricao) }
        adapter = SolicitacaoAdapter(this, sols)
        listaSolicitacoes.adapter = adapter

        if (adapter.isEmpty) {
            semSolicitacoesText.visibility = View.VISIBLE
        } else {
            semSolicitacoesText.visibility = View.GONE
        }
    }

    private fun carregaImagensSolicitacoes(solicitacoes: List<SolicitacaoPreviewAsync>) {
        for (solicitacao in solicitacoes) {
            // precisa ser um launch pra cada solicitacao
            lifecycleScope.launch {
                val imagem = solicitacao.getImagemSolicitador!!.await()
                val index = adapter.solicitacoes.indexOfFirst { a -> a.id == solicitacao.id }
                if (index == -1) return@launch
                adapter.solicitacoes[index].imagemSolicitador = imagem
                listaSolicitacoes.invalidateViews()
            }
        }
    }
}
