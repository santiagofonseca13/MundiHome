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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mh.mundihome.Modelo.ModeloComentario
import com.mh.mundihome.R

class AdaptadorComentarioAdmin(private val listaComentarios: ArrayList<ModeloComentario>) :
    RecyclerView.Adapter<AdaptadorComentarioAdmin.HolderComentario>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComentario {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comentario_admin, parent, false)
        return HolderComentario(view)
    }

    override fun onBindViewHolder(holder: HolderComentario, position: Int) {
        val modelo = listaComentarios[position]
        val context = holder.itemView.context

        holder.tvComentario.text = "\"${modelo.comentario}\""

        // Cargamos los nombres reales en lugar de los UIDs
        cargarInformacionUsuario(modelo.uid, holder.tvAutor, "De: ")
        cargarInformacionUsuario(modelo.uid_vendedor, holder.tvVendedor, "Para: ")

        holder.btnEliminar.setOnClickListener {
            confirmarEliminacion(modelo, context)
        }
    }

    override fun getItemCount(): Int = listaComentarios.size

    inner class HolderComentario(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvComentario: TextView = itemView.findViewById(R.id.tv_texto_comentario)
        // Separamos los TextViews en el layout para mejor control
        val tvAutor: TextView = itemView.findViewById(R.id.tv_autor_nombre)
        val tvVendedor: TextView = itemView.findViewById(R.id.tv_vendedor_nombre)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.btn_eliminar_comentario)
    }

    private fun cargarInformacionUsuario(uid: String, tvNombre: TextView, prefijo: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val nombre = snapshot.child("nombres").value.toString()
                    tvNombre.text = "$prefijo$nombre"
                } else {
                    tvNombre.text = "$prefijo(Usuario eliminado)"
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun confirmarEliminacion(modelo: ModeloComentario, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Eliminar Reseña")
        builder.setMessage("¿Estás seguro de eliminar este comentario?")
        builder.setPositiveButton("Eliminar") { _, _ ->
            val ref = FirebaseDatabase.getInstance().getReference("ComentariosVendedores")
            ref.child(modelo.uid_vendedor).child("Comentarios").child(modelo.id).removeValue()
                .addOnSuccessListener { Toast.makeText(context, "Reseña eliminada", Toast.LENGTH_SHORT).show() }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
}