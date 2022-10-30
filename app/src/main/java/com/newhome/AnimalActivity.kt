package com.newhome

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.newhome.dto.*
import com.newhome.utils.DialogDisplayer
import com.newhome.utils.LoadingDialog
import com.newhome.viewmodels.AnimalViewModel
import kotlinx.coroutines.launch

class AnimalActivity : AppCompatActivity() {
    private val viewModel: AnimalViewModel by viewModels()

    private lateinit var animalImage: ImageView

    private lateinit var nomeAnimalText: TextView

    private lateinit var descricaoAnimalText: TextView
    private lateinit var imagemDono: ImageView

    private lateinit var verMapaAnimalButton: Button

    private lateinit var verDetalhesBuscaButton: Button
    private lateinit var solicitacarAdocaoButton: Button

    private lateinit var animalBuscadoButton: Button
    private lateinit var cancelarAdocaoButton: Button

    private val loadingDialog = LoadingDialog(this)
    private val dialog = LoadingDialog(this)
    private lateinit var dialogDisplayer: DialogDisplayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animal)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Animal"

        dialogDisplayer = DialogDisplayer(applicationContext)

        animalImage = findViewById(R.id.animalImage)

        nomeAnimalText = findViewById(R.id.nomeAnimalText)
        descricaoAnimalText = findViewById(R.id.descricaoAnimalText)

        imagemDono = findViewById(R.id.imagemDono)

        verMapaAnimalButton = findViewById(R.id.verMapaAnimalButton)
        verDetalhesBuscaButton = findViewById(R.id.verDetalhesBuscaButton)

        animalBuscadoButton = findViewById(R.id.animalBuscadoButton)
        cancelarAdocaoButton = findViewById(R.id.cancelarAdocaoButton)
        solicitacarAdocaoButton = findViewById(R.id.solicitacarAdocaoButton)

        lifecycleScope.launch { loadData() }
        lifecycleScope.launch { watchForModelChanges() }
        imagemDono.setOnClickListener { onVerDono() }
        verMapaAnimalButton.setOnClickListener { onVerMapa() }
        verDetalhesBuscaButton.setOnClickListener { onVerDetalhes() }
        animalBuscadoButton.setOnClickListener { lifecycleScope.launch { onAnimalBuscado() } }
        cancelarAdocaoButton.setOnClickListener { lifecycleScope.launch { onCancelarAdocao() } }
        solicitacarAdocaoButton.setOnClickListener { lifecycleScope.launch { onSolicitarAdocao() } }

        animalBuscadoButton.visibility = View.GONE
        cancelarAdocaoButton.visibility = View.GONE
        solicitacarAdocaoButton.visibility = View.GONE
        verDetalhesBuscaButton.visibility = View.GONE
        verMapaAnimalButton.visibility = View.GONE
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
            val id = intent.getStringExtra("id")!!
            viewModel.loadDataAsync(id)
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao carregar animal", e)
            finish()
        }

        loadingDialog.stop()
    }

    private suspend fun watchForModelChanges() {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.animalState.collect { a -> loadAnimal(a) }
            }
            launch {
                viewModel.donoState.collect { d -> loadDono(d) }
            }
            launch {
                viewModel.statusState.collect { s -> loadStatus(s) }
            }
        }
    }

    private fun loadAnimal(animal: Animal) {
        nomeAnimalText.text = animal.nome
        descricaoAnimalText.text = animal.detalhes
        animalImage.setImageBitmap(animal.imagem)
    }

    private fun loadDono(dono: Usuario) {
        imagemDono.setImageBitmap(dono.imagem)
    }

    private fun loadStatus(status: StatusSolicitacao) {
        // verifica se o animal foi solicitado e se a solicitacao foi aceita

        if (!status.solicitado) {
            solicitacarAdocaoButton.visibility = View.VISIBLE
            return
        }

        cancelarAdocaoButton.visibility = View.VISIBLE

        if (status.solicitacaoAceita) {
            animalBuscadoButton.visibility = View.VISIBLE
            verDetalhesBuscaButton.visibility = View.VISIBLE
            verMapaAnimalButton.visibility = View.VISIBLE
        }
    }

    private fun onVerDono() {
        // vai pra tela de ver dono

        val intent = Intent(applicationContext, PerfilActivity::class.java)
        intent.putExtra("id", viewModel.donoState.value.id)
        startActivity(intent)
    }

    private fun onVerMapa() {
        // vai pra tela do mapa

        // TODO mostrar mapa
    }

    private fun onVerDetalhes() {
        // vai pra tela de detalhes

        // TODO
        val intent = Intent(applicationContext, SolicitacaoDetalhesActivity::class.java)
        intent.putExtra("detalhes", viewModel.statusState.value.detalhesAdocao)
        startActivity(intent)
    }

    private suspend fun onAnimalBuscado() {
        // busca animal e vai pra tela de animal adotado

        dialog.start()

        try {
            viewModel.animalBuscado()
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao marcar animal como buscado", e)
            dialog.stop()
            return
        }

        val intent = Intent(applicationContext, AnimalAdotadoActivity::class.java)
        intent.putExtra("id", viewModel.animalState.value.id)

        startActivity(intent)
        finish()
        dialogDisplayer.display("Animal buscado")
        dialog.stop()
    }

    private suspend fun onCancelarAdocao() {
        // cancela adocao

        dialog.start()

        try {
            viewModel.cancelarSolicitacao()
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao cancelar solicitação", e)
            dialog.stop()
            return
        }

        dialogDisplayer.display("Solicitação cancelada")
        finish()
        dialog.stop()
    }

    private suspend fun onSolicitarAdocao() {
        // solicita adocao

        dialog.start()

        try {
            viewModel.solicitarAnimal()
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao marcar animal como buscado", e)
            dialog.stop()
            return
        }

        dialogDisplayer.display("Animal buscado")
        finish()
        dialog.stop()
    }
}
