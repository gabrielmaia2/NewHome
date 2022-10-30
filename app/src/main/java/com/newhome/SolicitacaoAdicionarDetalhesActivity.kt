package com.newhome

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SolicitacaoAdicionarDetalhesActivity : AppCompatActivity() {
    private lateinit var detalhesPopupDonoEditText: TextView

    private lateinit var concluirPopupDonoButton: Button
    private lateinit var cancelarPopupDonoButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitacao_adicionar_detalhes)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Adicionar detalhes"

        detalhesPopupDonoEditText = findViewById(R.id.detalhesPopupDonoEditText)

        concluirPopupDonoButton = findViewById(R.id.concluirPopupDonoButton)
        cancelarPopupDonoButton = findViewById(R.id.cancelarPopupDonoButton)

        concluirPopupDonoButton.setOnClickListener { onConcluir() }
        cancelarPopupDonoButton.setOnClickListener { onCancelar() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onCancelar()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onConcluir() {
        // envia detalhes de volta

        val detalhes = detalhesPopupDonoEditText.text?.toString() ?: ""

        val intent = Intent(applicationContext, SolicitacaoActivity::class.java)
        intent.putExtra("detalhes", detalhes)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun onCancelar() {
        // volta pra tela de lista sem adicionar animal

        val intent = Intent(applicationContext, SolicitacaoActivity::class.java)
        setResult(RESULT_CANCELED, intent)
        finish()
    }
}
