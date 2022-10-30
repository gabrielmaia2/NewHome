package com.newhome

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.newhome.dto.AnimalAsync
import com.newhome.utils.DialogDisplayer
import com.newhome.utils.LoadingDialog
import kotlinx.coroutines.launch

class AnimalAdotadoActivity : AppCompatActivity() {
    private lateinit var animal: AnimalAsync

    private lateinit var animalAdotadoImage: ImageView

    private lateinit var nomeAnimalAdotadoText: TextView
    private lateinit var descricaoAnimalAdotadoText: TextView

    private lateinit var donoAntigoButton: Button

    private val loadingDialog = LoadingDialog(this)
    private val dialog = LoadingDialog(this)
    private lateinit var dialogDisplayer: DialogDisplayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animal_adotado)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Animal"

        dialogDisplayer = DialogDisplayer(applicationContext)

        animalAdotadoImage = findViewById(R.id.animalAdotadoImage)

        nomeAnimalAdotadoText = findViewById(R.id.nomeAnimalAdotadoText)
        descricaoAnimalAdotadoText = findViewById(R.id.descricaoAnimalAdotadoText)

        donoAntigoButton = findViewById(R.id.perfilDonoAntigoButton)

        lifecycleScope.launch { carregarDados() }
        donoAntigoButton.setOnClickListener { lifecycleScope.launch { onVerDonoAntigo() } }
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

    private suspend fun carregarDados() {
        // carrega animal do database

        loadingDialog.start()

        val id = intent.getStringExtra("id")!!

        animal = try {
            NewHomeApplication.animalService.getAnimal(id).await()
        } catch (e: Exception) {
            errorLoading(e)
            return
        }

        nomeAnimalAdotadoText.text = animal.nome
        descricaoAnimalAdotadoText.text = animal.detalhes

        val imagem = try {
            animal.getImagem!!.await()
        } catch (e: Exception) {
            errorLoading(e)
            return
        }

        animalAdotadoImage.setImageBitmap(imagem)
        loadingDialog.stop()
    }

    private fun errorLoading(e: Exception) {
        dialogDisplayer.display("Falha ao carregar animal", e)
        finish()
        loadingDialog.stop()
    }

    private suspend fun onVerDonoAntigo() {
        // ve perfil do dono

        dialog.start()

        val dono = try {
            NewHomeApplication.animalService.getDonoInicial(animal.id).await()
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao buscar dono", e)
            dialog.stop()
            return
        }

        val intent = Intent(applicationContext, PerfilActivity::class.java)
        intent.putExtra("id", dono.id)
        startActivity(intent)
        dialog.stop()
    }
}