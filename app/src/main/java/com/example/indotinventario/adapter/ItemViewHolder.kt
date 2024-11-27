package com.example.indotinventario.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.indotinventario.Pruebas.InventarioItem
import com.example.indotinventario.databinding.ItemInventarioBinding

class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val binding = ItemInventarioBinding.bind(view)

    fun render(
        inventarioItemModel: InventarioItem,
        onClickListener: (InventarioItem) -> Unit,
        onClickDelete: (Int) -> Unit,
    ) {

        binding.tvArticulo2.text = inventarioItemModel.idArticulo
        binding.tvIdCombinacion2.text = inventarioItemModel.idCombinacion
        binding.tvDescripcion2.text = inventarioItemModel.descripcion
        binding.tvPartida2.text = inventarioItemModel.partida
        binding.tvFecha2.text = inventarioItemModel.fechaCaducidad
        binding.tvNumero2.text = inventarioItemModel.numeroSerie
        binding.tvUnidades2.text = inventarioItemModel.unidadesContadas

        //Lógica del botón de eliminar
        binding.buttonDelete.setOnClickListener { onClickListener(inventarioItemModel) }
        binding.buttonDelete.setOnClickListener { onClickDelete(adapterPosition) }
    }
}