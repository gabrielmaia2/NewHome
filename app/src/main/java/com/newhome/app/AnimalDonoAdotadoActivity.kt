package com.newhome.app

import android.content.Intent
import android.os.Bundle
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

class AnimalDonoAdotadoActivity : AppCompatActivity() {
    private lateinit var animal: AnimalAsync

    private lateinit var animalAdotadoDonoImage: ImageView

    private lateinit var nomeAnimalAdotadoDonoText: TextView
    private lateinit var descricaoAnimalAdotadoDonoText: TextView

    private lateinit var perfilAdotadorButton: Button

    private val loadingDialog = LoadingDialog(this)
    private val dialog = LoadingDialog(this)
    private lateinit var dialogDisplayer: DialogDisplayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animal_dono_adotado)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Animal"

        dialogDisplayer = DialogDisplayer(applicationContext)

        animalAdotadoDonoImage = findViewById(R.id.animalAdotadoDonoImage)

        nomeAnimalAdotadoDonoText = findViewById(R.id.nomeAnimalAdotadoDonoText)
        descricaoAnimalAdotadoDonoText = findViewById(R.id.descricaoAnimalAdotadoDonoText)

        perfilAdotadorButton = findViewById(R.id.perfilAdotadorAdotadoDonoButton)

        lifecycleScope.launch { carregarDados() }
        perfilAdotadorButton.setOnClickListener { lifecycleScope.launch { verAdotador() } }
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

        nomeAnimalAdotadoDonoText.text = animal.nome
        descricaoAnimalAdotadoDonoText.text = animal.detalhes

        val imagem = try {
            animal.getImagem!!.await()
        } catch (e: Exception) {
            errorLoading(e)
            return
        }

        animalAdotadoDonoImage.setImageBitmap(imagem)
        loadingDialog.stop()
    }

    private fun errorLoading(e: Exception) {
        dialogDisplayer.display("Falha ao carregar animal", e)
        finish()
        loadingDialog.stop()
    }

    private suspend fun verAdotador() {
        // ve o perfil do adotador

        dialog.start()

        val adotador = try {
            NewHomeApplication.animalService.getAdotador(animal.id).await()
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao carregar adotador", e)
            dialog.stop()
            return
        }

        val intent = Intent(applicationContext, PerfilActivity::class.java)
        intent.putExtra("id", adotador!!.id)
        startActivity(intent)
        dialog.stop()
    }
}