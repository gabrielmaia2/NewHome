package com.newhome

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.newhome.dto.SolicitacaoPreview

class ListaSolicitacaoActivity : AppCompatActivity() {
    private lateinit var semSolicitacoesText: TextView

    private lateinit var listaSolicitacoes: ListView
    private lateinit var adapter: SolicitacaoAdapter

    // se tiver id, filtra solicitacoes feitas pro animal com esse id
    // senao, mostra todas solicitacoes recebidas pra todos os animais
    private var animalId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_solicitacao)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Solicitações"

        semSolicitacoesText = findViewById(R.id.semSolicitacoesText)
        semSolicitacoesText.visibility = View.GONE

        listaSolicitacoes = findViewById(R.id.listaSolicitacoes)

        listaSolicitacoes.setOnItemClickListener { _, _, position, _ -> onVerSolicitacao(position) }
    }

    override fun onResume() {
        super.onResume()
        carregarDados()
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

    private fun onVerSolicitacao(position: Int) {
        // vai pra tela de ver solicitacao

        val intent = Intent(applicationContext, SolicitacaoActivity::class.java)
        intent.putExtra("id", adapter.getItem(position)!!.id)
        startActivity(intent)
    }

    private fun carregarDados() {
        animalId = intent.getStringExtra("animalId") ?: ""

        // carrega solicitacoes do database

        if (animalId == "") {
            NewHomeApplication.solicitacaoProvider.getTodasSolicitacoes({ solicitacoes ->
                onSolicitacoesCarregadas(solicitacoes)
            }, { e ->
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            })
        } else {
            NewHomeApplication.solicitacaoProvider.getTodasSolicitacoesAnimal(
                animalId,
                { solicitacoes ->
                    onSolicitacoesCarregadas(solicitacoes)
                },
                { e ->
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                })
        }
    }

    private fun onSolicitacoesCarregadas(solicitacoes: List<SolicitacaoPreview>) {
        adapter = SolicitacaoAdapter(this, solicitacoes)
        listaSolicitacoes.adapter = adapter

        if (adapter.isEmpty) {
            semSolicitacoesText.visibility = View.VISIBLE
        } else {
            semSolicitacoesText.visibility = View.GONE
        }
    }
}
