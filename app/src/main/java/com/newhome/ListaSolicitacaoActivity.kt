package com.newhome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.newhome.dto.Solicitacao
import com.newhome.dto.SolicitacaoID

class ListaSolicitacaoActivity : AppCompatActivity() {
    private lateinit var semSolicitacoesText: TextView

    private lateinit var listaSolicitacoes: ListView
    private lateinit var adapter: SolicitacaoAdapter

    private lateinit var verSolicitacaoLauncher: ActivityResultLauncher<Intent>

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

        carregarDados()
        setVerSolicitacaoLauncher()
        listaSolicitacoes.setOnItemClickListener { _, _, position, _ -> onVerSolicitacao(position) }
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

    private fun setVerSolicitacaoLauncher() {
        // launcher usado para iniciar a activity de ver solicitacao

        verSolicitacaoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != RESULT_OK) return@registerForActivityResult

                val id = result.data!!.getSerializableExtra("id") as SolicitacaoID
                val solicitacao = Solicitacao() // TODO carregar do database
                val mensagem = result.data!!.getStringExtra("mensagem") ?: ""

                // TODO atualizar solicitacao no database
                // (se for aceita rejeitar todas outras pro mesmo animal)

                val text = when (mensagem) {
                    "aceita" -> "Solicitao aceita."
                    "rejeitada" -> "Solicitacao rejeitada."
                    "animalBuscado" -> "Animal buscado."
                    "cancelado" -> "Cancelado."
                    else -> ""
                }

                Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

                carregarDados() // atualiza lista de animais
            }
    }

    private fun onVerSolicitacao(position: Int) {
        // vai pra tela de ver solicitacao

        val intent = Intent(applicationContext, SolicitacaoActivity::class.java)
        intent.putExtra("id", adapter.getItem(position)!!.id)
        verSolicitacaoLauncher.launch(intent)
    }

    private fun carregarDados() {
        animalId = intent.getStringExtra("animalId") ?: ""

        // carrega solicitacoes do database

        // TODO carregar solicitacoes do database
        // se tiver id filtra por id

        val s1 = Solicitacao()
        s1.titulo = "Marcus"
        s1.descricao = "quer adotar Hamisterzinho"
        s1.imagemSolicitadorURL = ""
        var id = SolicitacaoID()
        id.animalId = "id1"
        id.adotadorId = "id2"
        s1.id = id

        val s2 = Solicitacao()
        s2.titulo = "Gabriel"
        s2.descricao = "quer adotar Cachorrinho"
        s2.imagemSolicitadorURL = ""
        id = SolicitacaoID()
        id.animalId = "id3"
        id.adotadorId = "id4"
        s2.id = id

        val s3 = Solicitacao()
        s3.titulo = "Kamila"
        s3.descricao = "quer adotar Gatinho"
        s3.imagemSolicitadorURL = ""
        id = SolicitacaoID()
        id.animalId = "id5"
        id.adotadorId = "id6"
        s3.id = id

        val list = arrayListOf(s1, s2, s3)
        adapter = SolicitacaoAdapter(this, list)
        listaSolicitacoes.adapter = adapter

        if (adapter.isEmpty) {
            semSolicitacoesText.visibility = View.VISIBLE
        } else {
            semSolicitacoesText.visibility = View.GONE
        }
    }
}
