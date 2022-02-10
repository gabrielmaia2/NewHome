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

class AnimalDonoAdotadoActivity : AppCompatActivity() {
    private lateinit var animal: Animal

    private lateinit var animalAdotadoDonoImage: ImageView

    private lateinit var nomeAnimalAdotadoDonoText: TextView
    private lateinit var descricaoAnimalAdotadoDonoText: TextView

    private lateinit var perfilAdotadorAdotadoDonoButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animal_dono_adotado)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Animal"

        animalAdotadoDonoImage = findViewById(R.id.animalAdotadoDonoImage)

        nomeAnimalAdotadoDonoText = findViewById(R.id.nomeAnimalAdotadoDonoText)
        descricaoAnimalAdotadoDonoText = findViewById(R.id.descricaoAnimalAdotadoDonoText)

        perfilAdotadorAdotadoDonoButton = findViewById(R.id.perfilAdotadorAdotadoDonoButton)

        carregarDados()
        perfilAdotadorAdotadoDonoButton.setOnClickListener { verAdotador() }
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
            nomeAnimalAdotadoDonoText.text = animal.nome
            descricaoAnimalAdotadoDonoText.text = animal.detalhes
            animalAdotadoDonoImage.setImageBitmap(animal.imagem)
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun verAdotador() {
        // ve o perfil do adotador

        NewHomeApplication.animalProvider.getAdotador(animal.id, { adotador ->
            val intent = Intent(applicationContext, PerfilActivity::class.java)
            intent.putExtra("id", adotador!!.id)
            startActivity(intent)
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }
}