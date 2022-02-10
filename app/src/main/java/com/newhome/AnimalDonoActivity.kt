package com.newhome

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.newhome.dto.Animal

class AnimalDonoActivity : AppCompatActivity() {
    private lateinit var animal: Animal

    private lateinit var animalDonoImage: ImageView

    private lateinit var nomeAnimalDonoText: TextView
    private lateinit var descricaoAnimalDonoText: TextView

    private lateinit var verMapaAnimalDonoButton: Button
    private lateinit var solicitacoesDonoButton: Button
    private lateinit var removerAnimaDonolButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animal_dono)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Animal"

        animalDonoImage = findViewById(R.id.animalDonoImage)

        nomeAnimalDonoText = findViewById(R.id.nomeAnimalDonoText)
        descricaoAnimalDonoText = findViewById(R.id.descricaoAnimalDonoText)

        verMapaAnimalDonoButton = findViewById(R.id.verMapaAnimalDonoButton)
        solicitacoesDonoButton = findViewById(R.id.solicitacoesDonoButton)
        removerAnimaDonolButton = findViewById(R.id.removerAnimaDonolButton)

        // TODO setar mapa listener pra ver local no mapa

        solicitacoesDonoButton.setOnClickListener { onVerSolicitacoes() }
        removerAnimaDonolButton.setOnClickListener { onRemoverAnimal() }
    }

    override fun onResume() {
        super.onResume()
        carregarDados()
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

    private fun carregarDados() {
        // carrega os dados do database e preenche os campos

        val id = intent.getStringExtra("id")!!
        NewHomeApplication.animalProvider.getAnimal(id, { animal ->
            this.animal = animal
            nomeAnimalDonoText.text = animal.nome
            descricaoAnimalDonoText.text = animal.detalhes
            animalDonoImage.setImageBitmap(animal.imagem)
        }, {
            Toast.makeText(applicationContext, "Ocorreu um erro ao carregar animal.", Toast.LENGTH_SHORT).show()
            finish()
        })
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

    private fun onRemoverAnimal() {
        // remove animal e volta para lista de animais

        NewHomeApplication.animalProvider.removerAnimal(animal.id, {
            Toast.makeText(applicationContext, "Animal deletado com sucesso.", Toast.LENGTH_SHORT)
                .show()

            val intent = Intent(applicationContext, ListarAnimaisActivity::class.java)
            startActivity(intent)
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }
}
