package com.newhome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import com.newhome.dto.Animal
import java.util.*
import kotlin.collections.ArrayList

class ListarAnimaisActivity : AppCompatActivity() {
    private lateinit var semAnimaisText: TextView

    private lateinit var porAdocaoButton: Button
    private lateinit var solicitacoesFeitasListarAnimaisButton: Button
    private lateinit var solicitacoesRecebidasListarAnimaisButton: Button

    private lateinit var listView: ListView
    private lateinit var listViewAdapter: AnimalAdapter

    private lateinit var addAnimalActivityLanucher: ActivityResultLauncher<Intent>

    // todosAnimais, postosAdocao, adotados, solicitados
    private var tipo: String = ""

    // quando o tipo e postosAdocao, se esse id nao for vazio, vai mostrar os animais
    // postos em adocao pelo usuario com esse id
    private var usuarioId: String = ""
    private var eProprioPerfil = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_animais)

        semAnimaisText = findViewById(R.id.semAnimaisText)
        semAnimaisText.visibility = View.GONE

        porAdocaoButton = findViewById(R.id.porAdocaoListarAnimaisButton)
        solicitacoesFeitasListarAnimaisButton =
            findViewById(R.id.solicitacoesFeitasListarAnimaisButton)
        solicitacoesRecebidasListarAnimaisButton =
            findViewById(R.id.solicitacoesRecebidasListarAnimaisButton)

        listView = findViewById(R.id.listarAnimaisListView)

        carregaDados()
        verificarMensagem()
        definirVisibilidadeBotoes()
        carregarAnimais()
        setAddAnimalActivityLauncher()
        listView.setOnItemClickListener { _, _, position, _ -> onVerAnimal(position) }
        porAdocaoButton.setOnClickListener { onAddAnimal() }
        solicitacoesFeitasListarAnimaisButton.setOnClickListener { onVerSolicitacoesFeitas() }
        solicitacoesRecebidasListarAnimaisButton.setOnClickListener { onVerSolicitacoesRecebidas() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchItem: MenuItem = menu!!.findItem(R.id.pesquisaMenuItem)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Pesquise por um animal"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                carregarAnimais(newText)
                return true
            }
        })

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

    private fun carregaDados() {
        // pega qual o tipo da lista (todosAnimais por padrao)
        tipo = intent.getStringExtra("tipo") ?: "todosAnimais"

        // pega o id do usuario (se for postosAdocao)
        usuarioId = intent.getStringExtra("usuarioId") ?: ""
        eProprioPerfil = usuarioId == ""
        if (!eProprioPerfil) {
            tipo = "postosAdocao"
        }
    }

    private fun verificarMensagem() {
        // verifica se existe alguma mensagem pra exibir e se tiver exibe ela num toast

        val mensagem = intent.getStringExtra("mensagem") ?: return

        when (mensagem) {
            "animalDeletado" -> Toast.makeText(
                this,
                "Animal deletado com sucesso.",
                Toast.LENGTH_SHORT
            ).show()
            "animalBuscado" -> Toast.makeText(
                this,
                "Você buscou o animal.",
                Toast.LENGTH_SHORT
            ).show()
            "adocaoCancelada" -> Toast.makeText(
                this,
                "Você cancelou a adoção do animal.",
                Toast.LENGTH_SHORT
            ).show()
            else -> return
        }
    }

    private fun definirVisibilidadeBotoes() {
        // muda quais botoes estao visiveis dependendo do tipo da tela
        // se o tipo for todosAnimals ou postosAdocao, o botao de por animal em adocao fica visivel
        // se o tipo for adotados ou postosAdocao, o botao de solicitacoes feitas fica visivel

        porAdocaoButton.visibility = View.GONE
        solicitacoesFeitasListarAnimaisButton.visibility = View.GONE
        solicitacoesRecebidasListarAnimaisButton.visibility = View.GONE

        // verifica se e a lista de todos os animais ou
        // a de animais postos em adocao do proprio perfil
        // TODO verificar se e animais postos em adocao do proprio perfil

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

    private fun setAddAnimalActivityLauncher() {
        // launchar usado para iniciar a activity de adicionar um novo animal

        addAnimalActivityLanucher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != RESULT_OK) return@registerForActivityResult

                carregarAnimais() // atualiza lista de animais
                Toast.makeText(this, "Animal adicionado com sucesso.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onVerAnimal(position: Int) {
        // vai pra tela do animal dono

        // TODO mudar pra verificar se e o dono de cada animal no database

        // TODO verificar se e dono no database
        val eDono = eProprioPerfil

        // TODO verificar do database se foi adotado
        val adotado = false

        val intent = if (eDono) {
            if (adotado) {
                Intent(applicationContext, AnimalDonoAdotadoActivity::class.java)
            } else {
                Intent(applicationContext, AnimalDonoActivity::class.java)
            }
        }
        else {
            if (adotado) {
                Intent(applicationContext, AnimalAdotadoActivity::class.java)
            } else {
                Intent(applicationContext, AnimalActivity::class.java)
            }
        }

        intent.putExtra("id", listViewAdapter.getItem(position)!!.id)
        startActivity(intent)
    }

    private fun onAddAnimal() {
        // vai para a tela de adicionar um novo animal

        val intent = Intent(applicationContext, NovoAnimalActivity::class.java)
        addAnimalActivityLanucher.launch(intent)
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

    private fun carregarAnimais(pesquisa: String? = null) {
        // carrega animais do database

        // TODO pegar animais dos providers
        // TODO setar click listeners pra botoes de remover ou cancelar de cada item
        // TODO verificar se pesquisa e nula e so pesquisar se nao for

        // TODO pegar dados baseados no tipo da tela e
        //  filtrar por id usuario caso seja postosAdocao e tenha id

        val a1 = Animal()
        a1.nome = "Cachorrinho"
        a1.detalhes = "Ele é muito fofinho e gosta de mijar a casa toda."
        a1.imagemURL = ""
        val a2 = Animal()
        a2.nome = "Gatinho"
        a2.detalhes = "Ele é muito fofinho e acha que é dono da casa."
        a2.imagemURL = ""
        val a3 = Animal()
        a3.nome = "Hamsterzinho"
        a3.detalhes = "Ele existe, eu acho. Eu sei que ele dorme e come (às vezes)."
        a3.imagemURL = ""

        val animais = ArrayList<Animal>()
        animais.add(a1)
        animais.add(a2)
        animais.add(a3)

        listViewAdapter = AnimalAdapter(this, animais)
        if (pesquisa != null && pesquisa != "")
            listViewAdapter.filter.filter(pesquisa)
        listView.adapter = listViewAdapter

        if (listViewAdapter.isEmpty) {
            semAnimaisText.visibility = View.VISIBLE
        } else {
            semAnimaisText.visibility = View.GONE
        }
    }
}
