package com.example.indotinventario.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.indotinventario.Pruebas.Articulo
import com.example.indotinventario.Pruebas.InventarioItem
import com.example.indotinventario.R

class ItemAdapter(

    private var inventarioItemList: List<InventarioItem>,
    private val onClickListener: (InventarioItem) -> Unit) : RecyclerView.Adapter<ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(layoutInflater.inflate(R.layout.item_inventario, parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = inventarioItemList[position]
        holder.render(item, onClickListener)
    }

    override fun getItemCount(): Int = inventarioItemList.size

    fun updateArticulos(inventarioItemList: List<InventarioItem>){

        this.inventarioItemList = inventarioItemList
        notifyDataSetChanged()
    }
}