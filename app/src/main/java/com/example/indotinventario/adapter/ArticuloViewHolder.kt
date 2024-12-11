package com.example.indotinventario.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.indotinventario.logica.Articulo
import com.example.indotinventario.databinding.ItemArticuloBinding

class ArticuloViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val binding = ItemArticuloBinding.bind(view)

    fun render(
        articuloModel: Articulo,
        onClickListener: (Articulo) -> Unit
    ) {
        binding.tvArticulo.text = articuloModel.IdArticulo
        binding.tvIdCombinacion.text = articuloModel.IdCombinacion
        binding.tvDescripcion.text = articuloModel.Descripcion
        binding.btnSelect.setOnClickListener { onClickListener(articuloModel) }
    }
}