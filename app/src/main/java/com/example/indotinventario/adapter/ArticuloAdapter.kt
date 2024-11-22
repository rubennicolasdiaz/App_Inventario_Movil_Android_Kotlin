package com.example.indotinventario.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.indotinventario.Pruebas.Articulo
import com.example.indotinventario.R

class ArticuloAdapter(

    private var articuloList: List<Articulo>,
    private val onClickListener: (Articulo) -> Unit) : RecyclerView.Adapter<ArticuloViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticuloViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ArticuloViewHolder(layoutInflater.inflate(R.layout.item_articulo, parent, false))
    }

    override fun onBindViewHolder(holder: ArticuloViewHolder, position: Int) {
        val item = articuloList[position]
        holder.render(item, onClickListener)
    }

    override fun getItemCount(): Int = articuloList.size

    fun updateArticulos(articuloList: List<Articulo>){

        this.articuloList = articuloList
        notifyDataSetChanged()
    }
}