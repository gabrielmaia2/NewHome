package com.newhome

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SolicitacaoDetalhesActivity : AppCompatActivity() {
    private lateinit var solicitacaoDetalhesDetalhesText: TextView

    private lateinit var solicitacaoDetalhesConcluirButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitacao_detalhes)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalhes"

        solicitacaoDetalhesDetalhesText = findViewById(R.id.solicitacaoDetalhesDetalhesText)
        solicitacaoDetalhesConcluirButton = findViewById(R.id.solicitacaoDetalhesConcluirButton)

        solicitacaoDetalhesDetalhesText.text = intent.getStringExtra("detalhes")

        solicitacaoDetalhesConcluirButton.setOnClickListener { finish() }
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
}