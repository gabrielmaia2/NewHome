package com.newhome.app

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.newhome.app.adapters.AnimalAdapter
import com.newhome.app.dto.Animal
import com.newhome.app.dto.AnimalAsync
import com.newhome.app.dto.Usuario
import com.newhome.app.utils.DialogDisplayer
import com.newhome.app.utils.LoadingDialog
import kotlinx.coroutines.launch

class ListarAnimaisActivity : AppCompatActivity() {
    private lateinit var semAnimaisText: TextView

    private lateinit var porAdocaoButton: Button
    private lateinit var solicitacoesFeitasListarAnimaisButton: Button
    private lateinit var solicitacoesRecebidasListarAnimaisButton: Button

    private lateinit var listView: ListView
    private lateinit var listViewAdapter: AnimalAdapter

    // todosAnimais, postosAdocao, adotados, solicitados
    private var tipo: String = ""

    // quando o tipo e postosAdocao, se esse id nao for vazio, vai mostrar os animais
    // postos em adocao pelo usuario com esse id
    private var usuarioId: String = ""
    private var eProprioPerfil = false
    private lateinit var usuarioAtual: Usuario

    private val loadingDialog = LoadingDialog(this)
    private val dialog = LoadingDialog(this)
    private lateinit var dialogDisplayer: DialogDisplayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_animais)
        supportActionBar?.title = "Animais"

        dialogDisplayer = DialogDisplayer(applicationContext)

        semAnimaisText = findViewById(R.id.semAnimaisText)
        semAnimaisText.visibility = View.GONE

        porAdocaoButton = findViewById(R.id.porAdocaoListarAnimaisButton)
        solicitacoesFeitasListarAnimaisButton =
            findViewById(R.id.solicitacoesFeitasListarAnimaisButton)
        solicitacoesRecebidasListarAnimaisButton =
            findViewById(R.id.solicitacoesRecebidasListarAnimaisButton)

        listView = findViewById(R.id.listarAnimaisListView)

        listView.setOnItemClickListener { _, _, i, _ -> lifecycleScope.launch { onVerAnimal(i) } }
        porAdocaoButton.setOnClickListener { onAddAnimal() }
        solicitacoesFeitasListarAnimaisButton.setOnClickListener { onVerSolicitacoesFeitas() }
        solicitacoesRecebidasListarAnimaisButton.setOnClickListener { onVerSolicitacoesRecebidas() }

        porAdocaoButton.visibility = View.GONE
        solicitacoesFeitasListarAnimaisButton.visibility = View.GONE
        solicitacoesRecebidasListarAnimaisButton.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { carregarDados() }

        invalidateOptionsMenu()
    }

    override fun onPause() {
        super.onPause()
        loadingDialog.stop()
        dialog.stop()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchItem: MenuItem = menu!!.findItem(R.id.pesquisaMenuItem)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Pesquisar..."
        searchView.maxWidth = Int.MAX_VALUE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarAnimais(newText)
                return true
            }
        })

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val perfilItem: MenuItem = menu!!.findItem(R.id.verPerfilMenuItem)
        perfilItem.icon =
            BitmapDrawable(resources, NewHomeApplication.usuarioService.getUsuarioAtual().imagem)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.verPerfilMenuItem -> {
                val intent = Intent(applicationContext, PerfilActivity::class.java)
                startActivity(intent)
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
        // pega qual o tipo da lista (todosAnimais por padrao)
        tipo = intent.getStringExtra("tipo") ?: "todosAnimais"

        // pega o id do usuario (se for postosAdocao)
        usuarioId =
            intent.getStringExtra("usuarioId")
                ?: NewHomeApplication.usuarioService.getUsuarioAtual().id

        eProprioPerfil = usuarioId == NewHomeApplication.usuarioService.getUsuarioAtual().id

        if (!eProprioPerfil) {
            tipo = "postosAdocao"
        }

        usuarioAtual = NewHomeApplication.usuarioService.getUsuarioAtual()

        definirVisibilidadeBotoes()
        carregarAnimais()
    }

    private fun definirVisibilidadeBotoes() {
        // muda quais botoes estao visiveis dependendo do tipo da tela
        // se o tipo for todosAnimals ou postosAdocao, o botao de por animal em adocao fica visivel
        // se o tipo for adotados ou postosAdocao, o botao de solicitacoes feitas fica visivel

        // verifica se e a lista de todos os animais ou
        // a de animais postos em adocao do proprio perfil

        if (tipo == "todosAnimais" || (tipo == "postosAdocao" && eProprioPerfil)) {
            // permite adicionar animal pra adocao
            porAdocaoButton.visibility = View.VISIBLE
            solicitacoesRecebidasListarAnimaisButton.visibility = View.VISIBLE
        }

        // verifica se e a lista de animais adotados ou solicitados
        if (tipo == "adotados" || (tipo == "postosAdocao" && eProprioPerfil)) {
            // permite ver solicitacoes feitas por este no adotados ou por outros no postosAdocao
            solicitacoesFeitasListarAnimaisButton.visibility = View.VISIBLE
        }
    }

    private suspend fun carregarAnimais() {
        // carrega animais do database

        loadingDialog.start()

        val animais = try {
            when (tipo) {
                "todosAnimais" -> {
                    supportActionBar?.title = "Animais"
                    NewHomeApplication.animalService.getTodosAnimais().await()
                }
                "postosAdocao" -> {
                    supportActionBar?.title = "Animais postos em adoção"
                    NewHomeApplication.animalService.getAnimaisPostosAdocao(usuarioId).await()
                }
                "adotados" -> {
                    supportActionBar?.title = "Animais adotados"
                    NewHomeApplication.animalService.getAnimaisAdotados(usuarioAtual.id).await()
                }
                "solicitados" -> {
                    supportActionBar?.title = "Animais solicitados"
                    NewHomeApplication.animalService.getAnimaisSolicitados(usuarioAtual.id).await()
                }
                else -> {
                    loadingDialog.stop()
                    val message =
                        "Tipo da lista deve ser um de (todosAnimais, postosAdocao, adotados, solicitados)."
                    throw Exception(message)
                }
            }
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao carregar animais", e)
            loadingDialog.stop()
            return
        }

        exibeAnimaisCarregados(animais)
        carregaImagensAnimais(animais)

        loadingDialog.stop()
    }

    private suspend fun onVerAnimal(position: Int) {
        // vai pra tela de ver animal

        val animal = listViewAdapter.getItem(position)!!

        dialog.start()

        try {
            val taskDono = NewHomeApplication.animalService.getDonoInicial(animal.id)
            val taskAdotador = NewHomeApplication.animalService.getAdotador(animal.id)

            val dono = taskDono.await()
            val adotador = taskAdotador.await()

            val eDono = usuarioAtual.id == dono.id
            val adotado = adotador != null

            val intent = if (eDono) {
                if (adotado) {
                    Intent(applicationContext, AnimalDonoAdotadoActivity::class.java)
                } else {
                    // nao precisa so id
                    Intent(applicationContext, AnimalDonoActivity::class.java)
                }
            } else {
                if (adotado) {
                    Intent(applicationContext, AnimalAdotadoActivity::class.java)
                } else {
                    Intent(applicationContext, AnimalActivity::class.java)
                }
            }

            intent.putExtra("id", animal.id)
            startActivity(intent)
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao tentar ver animal", e)
        }

        dialog.stop()
    }

    private fun onAddAnimal() {
        // vai para a tela de adicionar um novo animal

        val intent = Intent(applicationContext, NovoAnimalActivity::class.java)
        startActivity(intent)
    }

    private fun onVerSolicitacoesFeitas() {
        // vai para tela de solicitacoes feitas pela pessoa

        val intent = Intent(applicationContext, ListarAnimaisActivity::class.java)
        intent.putExtra("tipo", "solicitados")
        startActivity(intent)
    }

    private fun onVerSolicitacoesRecebidas() {
        // vai para tela de todas solicitacoes de adocao recebidas

        val intent = Intent(applicationContext, ListaSolicitacaoActivity::class.java)
        startActivity(intent)
    }

    private fun exibeAnimaisCarregados(animais: List<AnimalAsync>) {
        val ans = animais.map { animal -> Animal(animal.id, animal.nome, animal.detalhes) }
        listViewAdapter = AnimalAdapter(this, ans)
        listView.adapter = listViewAdapter

        if (listViewAdapter.isEmpty) {
            semAnimaisText.visibility = View.VISIBLE
        } else {
            semAnimaisText.visibility = View.GONE
        }
    }

    private fun carregaImagensAnimais(animais: List<AnimalAsync>) {
        for (animal in animais) {
            // precisa ser um launch pra cada animal
            lifecycleScope.launch {
                val imagem = animal.getImagem!!.await()
                val index = listViewAdapter.animais.indexOfFirst { a -> a.id == animal.id }
                if (index == -1) return@launch
                listViewAdapter.animais[index].imagem = imagem
                listView.invalidateViews()
            }
        }
    }

    private fun filtrarAnimais(pesquisa: String?) {
        listViewAdapter.filter.filter(pesquisa?.lowercase()) { count ->
            if (count == 0) {
                semAnimaisText.visibility = View.VISIBLE
            } else {
                semAnimaisText.visibility = View.GONE
            }
        }
    }
}
