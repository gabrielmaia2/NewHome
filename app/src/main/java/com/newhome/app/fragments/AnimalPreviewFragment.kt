package com.newhome.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.newhome.app.AnimalDonoActivity
import com.newhome.app.R
import com.newhome.app.dto.Animal


class AnimalPreviewFragment : Fragment() {
    var id: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_animal_preview, parent, false)

        view.setOnClickListener { onVerAnimal() }

        return view
    }

    private fun onVerAnimal() {
        // vai pra tela do animal dono

        val intent = Intent(requireView().context.applicationContext, AnimalDonoActivity::class.java)
        intent.putExtra("id", id!!)
        startActivity(intent)
    }

    fun carregarAnimal(animal: Animal) {
        val view = requireView()
        val nomeAnimalPreviewText: TextView = view.findViewById(R.id.nomeAnimalPreviewText)
        val detalhesAnimalPreviewText: TextView =
            view.findViewById(R.id.detalhesAnimalPreviewText)
        val imageView: ImageView = view.findViewById(R.id.animalImagem)

        id = animal.id
        nomeAnimalPreviewText.text = animal.name
        detalhesAnimalPreviewText.text = animal.details
        imageView.setImageBitmap(animal.image)
    }
}
