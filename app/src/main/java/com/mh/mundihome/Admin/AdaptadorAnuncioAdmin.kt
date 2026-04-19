package com.mh.mundihome.Admin

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.mh.mundihome.Modelo.ModeloAnuncio
import com.mh.mundihome.R

class AdaptadorAnuncioAdmin(private val listaAnuncios: ArrayList<ModeloAnuncio>) :
    RecyclerView.Adapter<AdaptadorAnuncioAdmin.HolderAnuncio>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAnuncio {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_anuncio_admin, parent, false)
        return HolderAnuncio(view)
    }

    override fun onBindViewHolder(holder: HolderAnuncio, position: Int) {
        val modelo = listaAnuncios[position]
        val context = holder.itemView.context

        holder.titulo.text = modelo.titulo
        holder.precio.text = "$ ${modelo.precio}"
        holder.tipo.text = modelo.tipoInmueble

        // Lógica para ELIMINAR el anuncio como Moderador
        holder.btnEliminar.setOnClickListener {
            confirmarEliminacion(modelo, context)
        }
    }

    override fun getItemCount(): Int = listaAnuncios.size

    inner class HolderAnuncio(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.tv_titulo_anuncio)
        val precio: TextView = itemView.findViewById(R.id.tv_precio_anuncio)
        val tipo: TextView = itemView.findViewById(R.id.tv_tipo_anuncio)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.btn_eliminar_anuncio)
    }

    private fun confirmarEliminacion(modelo: ModeloAnuncio, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Eliminar Anuncio")
        builder.setMessage("¿Estás seguro de que deseas eliminar permanentemente el anuncio '${modelo.titulo}' por incumplir las normas?")

        builder.setPositiveButton("Eliminar") { _, _ ->
            val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
            ref.child(modelo.id).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, "Anuncio eliminado correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}