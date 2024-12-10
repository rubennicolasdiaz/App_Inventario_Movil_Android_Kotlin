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
        binding.tvArticulo.text = articuloModel.idArticulo
        binding.tvIdCombinacion.text = articuloModel.idCombinacion
        binding.tvDescripcion.text = articuloModel.descripcion
        binding.btnSelect.setOnClickListener { onClickListener(articuloModel) }
    }
}