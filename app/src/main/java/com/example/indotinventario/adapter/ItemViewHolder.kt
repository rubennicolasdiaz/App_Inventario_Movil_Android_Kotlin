package com.example.indotinventario.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.indotinventario.Pruebas.InventarioItem
import com.example.indotinventario.databinding.ItemInventarioBinding

class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val binding = ItemInventarioBinding.bind(view)

    fun render(
        inventarioItemModel: InventarioItem,
        onClickListener: (InventarioItem) -> Unit
    ) {

        binding.tvCodigoBarras.text = inventarioItemModel.codigoBarras
        binding.tvDescripcion.text = inventarioItemModel.descripcion
        binding.tvIdArticulo.text = inventarioItemModel.idArticulo
        binding.tvIdCombinacion.text = inventarioItemModel.idCombinacion
        binding.tvPartida.text = inventarioItemModel.partida
        binding.tvFechaCaducidad.text = inventarioItemModel.fechaCaducidad
        binding.tvNumeroSerie.text = inventarioItemModel.numeroSerie
        binding.tvUnidades.text = inventarioItemModel.unidadesContadas

        //binding.btnSelect.setOnClickListener { onClickListener(inventarioItemModel) }
    }
}