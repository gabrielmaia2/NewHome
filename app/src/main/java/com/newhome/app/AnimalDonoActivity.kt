package com.newhome.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.newhome.app.R
import com.newhome.app.dto.AnimalAsync
import com.newhome.app.utils.DialogDisplayer
import com.newhome.app.utils.LoadingDialog
import kotlinx.coroutines.launch

class AnimalDonoActivity : AppCompatActivity() {
    private lateinit var animal: AnimalAsync

    private lateinit var animalDonoImage: ImageView

    private lateinit var nomeAnimalDonoText: TextView
    private lateinit var descricaoAnimalDonoText: TextView

    private lateinit var verMapaAnimalDonoButton: Button
    private lateinit var solicitacoesDonoButton: Button
    private lateinit var removerAnimaDonolButton: Button

    private val loadingDialog = LoadingDialog(this)
    private val dialog = LoadingDialog(this)
    private lateinit var dialogDisplayer: DialogDisplayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animal_dono)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Animal"

        dialogDisplayer = DialogDisplayer(applicationContext)

        animalDonoImage = findViewById(R.id.animalDonoImage)

        nomeAnimalDonoText = findViewById(R.id.nomeAnimalDonoText)
        descricaoAnimalDonoText = findViewById(R.id.descricaoAnimalDonoText)

        verMapaAnimalDonoButton = findViewById(R.id.verMapaAnimalDonoButton)
        solicitacoesDonoButton = findViewById(R.id.solicitacoesDonoButton)
        removerAnimaDonolButton = findViewById(R.id.removerAnimaDonolButton)

        // TODO setar mapa listener pra ver local no mapa

        solicitacoesDonoButton.setOnClickListener { onVerSolicitacoes() }
        removerAnimaDonolButton.setOnClickListener { lifecycleScope.launch { onRemoverAnimal() } }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { carregarDados() }
    }

    override fun onPause() {
        super.onPause()
        loadingDialog.stop()
        dialog.stop()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.editar_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editarMenuItem -> {
                onEditarAnimalClick()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private suspend fun carregarDados() {
        // carrega os dados do database e preenche os campos

        loadingDialog.start()

        val id = intent.getStringExtra("id")!!

        animal = try {
            NewHomeApplication.animalService.getAnimal(id).await()
        } catch (e: Exception) {
            errorLoading(e)
            return
        }

        nomeAnimalDonoText.text = animal.nome
        descricaoAnimalDonoText.text = animal.detalhes

        val imagem = try {
            animal.getImagem!!.await()
        } catch (e: Exception) {
            errorLoading(e)
            return
        }

        animalDonoImage.setImageBitmap(imagem)
        loadingDialog.stop()
    }

    private fun errorLoading(e: Exception) {
        dialogDisplayer.display("Falha ao carregar animal", e)
        finish()
        loadingDialog.stop()
    }

    private fun onEditarAnimalClick() {
        // vai para a tela de editar animal

        val intent = Intent(applicationContext, EditarAnimalActivity::class.java)
        intent.putExtra("id", animal.id)
        startActivity(intent)
    }

    private fun onVerSolicitacoes() {
        // vai pra tela de solicitacoes do animal

        val intent = Intent(applicationContext, ListaSolicitacaoActivity::class.java)
        intent.putExtra("animalId", animal.id)
        startActivity(intent)
    }

    private suspend fun onRemoverAnimal() {
        // remove animal e volta para lista de animais

        dialog.start()

        try {
            NewHomeApplication.animalService.removerAnimal(animal.id).await()
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao remover animal", e)
            dialog.stop()
            return
        }

        dialogDisplayer.display("Animal removido com sucesso")

        val intent = Intent(applicationContext, ListarAnimaisActivity::class.java)
        startActivity(intent)
        dialog.stop()
    }
}
