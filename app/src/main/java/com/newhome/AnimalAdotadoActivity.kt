package com.newhome

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.newhome.dto.Animal

class AnimalAdotadoActivity : AppCompatActivity() {
    private lateinit var animal: Animal

    private lateinit var animalAdotadoImage: ImageView

    private lateinit var nomeAnimalAdotadoText: TextView
    private lateinit var descricaoAnimalAdotadoText: TextView

    private lateinit var perfilDonoAntigoButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animal_adotado)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Animal"

        animalAdotadoImage = findViewById(R.id.animalAdotadoImage)

        nomeAnimalAdotadoText = findViewById(R.id.nomeAnimalAdotadoText)
        descricaoAnimalAdotadoText = findViewById(R.id.descricaoAnimalAdotadoText)

        perfilDonoAntigoButton = findViewById(R.id.perfilDonoAntigoButton)

        carregarDados()
        perfilDonoAntigoButton.setOnClickListener { onVerPerfilDonoAntigo() }
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

    private fun carregarDados() {
        // carrega animal do database

        val id = intent.getStringExtra("id")!!
        NewHomeApplication.animalProvider.getAnimal(id, { animal ->
            this.animal = animal
            nomeAnimalAdotadoText.text = animal.nome
            descricaoAnimalAdotadoText.text = animal.detalhes
            animalAdotadoImage.setImageBitmap(animal.imagem)
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun onVerPerfilDonoAntigo() {
        // ve perfil do dono

        NewHomeApplication.animalProvider.getDonoInicial(animal.id, { dono ->
            val intent = Intent(applicationContext, PerfilActivity::class.java)
            intent.putExtra("id", dono.id)
            startActivity(intent)
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }
}