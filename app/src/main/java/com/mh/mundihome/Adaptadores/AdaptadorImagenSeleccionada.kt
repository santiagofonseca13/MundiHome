package com.mh.mundihome.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.mh.mundihome.R
import com.mh.mundihome.databinding.ItemImagenesSeleccionadasBinding
import com.mh.mundihome.modelo.ModeloImagenSeleccionada

class AdaptadorImagenSeleccionada (
    private val context: Context,
    private val imagenesSelectArrayList: ArrayList<ModeloImagenSeleccionada>
): Adapter<AdaptadorImagenSeleccionada.HolderImagenSelecciona>(){

    private lateinit var binding: ItemImagenesSeleccionadasBinding
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HolderImagenSelecciona {
        binding = ItemImagenesSeleccionadasBinding.inflate(LayoutInflater.from(context),parent, false)
        return HolderImagenSelecciona(binding.root)
    }

    override fun onBindViewHolder(
        holder: HolderImagenSelecciona,
        position: Int
    ) {
        val  modelo = imagenesSelectArrayList[position]

        val imagenUri = modelo.imagenUri

        try {
            Glide.with(context)
                .load(imagenUri)
                .placeholder(R.drawable.item_imagen)
                .into(holder.item_imagen)
        }
        catch (e: Exception){
        }

        holder.btn_cerrar.setOnClickListener {
            imagenesSelectArrayList.remove(modelo)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return imagenesSelectArrayList.size
    }

    inner class HolderImagenSelecciona(itemView: View) : ViewHolder(itemView){
        var item_imagen = binding.itemImagen
        var btn_cerrar = binding.cerrarItem

    }
}