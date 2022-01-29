package com.newhome

import androidx.fragment.app.Fragment
import android.os.Bundle

import android.view.ViewGroup

import android.view.LayoutInflater
import android.view.View


class AnimalPreviewFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_animal_preview, parent, false)
    }
}