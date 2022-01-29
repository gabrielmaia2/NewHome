package com.newhome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.newhome.dto.Animal

class AnimalDonoActivity : AppCompatActivity() {
    private lateinit var animal: Animal

    private lateinit var adicionarAnimaDonolImage: ImageView

    private lateinit var nomeAnimalDonoText: TextView
    private lateinit var descricaoAnimalDonoText: TextView

    private lateinit var verMapaAnimalDonoButton: Button
    private lateinit var solicitacoesDonoButton: Button
    private lateinit var removerAnimaDonolButton: Button

    private lateinit var editAnimalActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animal_dono)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Animal"

        adicionarAnimaDonolImage = findViewById(R.id.adicionarAnimaDonolImage)

        nomeAnimalDonoText = findViewById(R.id.nomeAnimalDonoText)
        descricaoAnimalDonoText = findViewById(R.id.descricaoAnimalDonoText)

        verMapaAnimalDonoButton = findViewById(R.id.verMapaAnimalDonoButton)
        solicitacoesDonoButton = findViewById(R.id.solicitacoesDonoButton)
        removerAnimaDonolButton = findViewById(R.id.removerAnimaDonolButton)

        // TODO setar mapa listener pra ver local no mapa

        carregarDados()
        setEditAnimalActivityLauncher()
        solicitacoesDonoButton.setOnClickListener { onVerSolicitacoes() }
        removerAnimaDonolButton.setOnClickListener { onRemoverAnimal() }
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

    private fun setEditAnimalActivityLauncher() {
        editAnimalActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != RESULT_OK) return@registerForActivityResult

                carregarDados() // atualiza animal
                Toast.makeText(this, "Animal editado com sucesso.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onEditarAnimalClick() {
        // vai para a tela de editar animal

        val intent = Intent(applicationContext, EditarAnimalActivity::class.java)
        intent.putExtra("animal", animal)
        editAnimalActivityLauncher.launch(intent)
    }

    private fun onVerSolicitacoes() {
        // vai pra tela de solicitacoes do animal

        val intent = Intent(applicationContext, ListaSolicitacaoActivity::class.java)
        intent.putExtra("animalId", animal.id)
        startActivity(intent)
    }

    private fun onRemoverAnimal() {
        // remove animal e volta para lista de animais

        // TODO remover do database

        val intent = Intent(applicationContext, ListarAnimaisActivity::class.java)
        intent.putExtra("mensagem", "animalDeletado")
        startActivity(intent)
    }

    private fun carregarDados() {
        // TODO carregar do database

        animal = Animal()
        animal.id = intent.getStringExtra("id")!!
        animal.nome = "Cachorrinho"
        animal.detalhes = "Ele é muito fofinho e gosta de mijar a casa toda."
        animal.imagemURL = ""

        nomeAnimalDonoText.text = animal.nome
        descricaoAnimalDonoText.text = animal.detalhes
        Util.tryLoadDrawableAsync(applicationContext, animal.imagemURL) { drawable ->
            adicionarAnimaDonolImage.setImageDrawable(drawable)
        }
    }
}