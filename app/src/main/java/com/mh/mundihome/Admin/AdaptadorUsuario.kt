package com.mh.mundihome.Admin

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.FirebaseDatabase
import com.mh.mundihome.Modelo.ModeloUsuario
import com.mh.mundihome.R

class AdaptadorUsuario(private val listaUsuarios: ArrayList<ModeloUsuario>) :
    RecyclerView.Adapter<AdaptadorUsuario.HolderUsuario>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderUsuario {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false)
        return HolderUsuario(view)
    }

    override fun onBindViewHolder(holder: HolderUsuario, position: Int) {
        val modelo = listaUsuarios[position]
        val context = holder.itemView.context

        holder.nombre.text = modelo.nombres
        holder.email.text = modelo.email

        // Si no tiene rol definido, asumimos que es "usuario"
        val rolActual = if (modelo.rol.isNotEmpty()) modelo.rol.uppercase() else "USUARIO"
        holder.rol.text = rolActual

        try {
            Glide.with(context)
                .load(modelo.urlImagenPerfil)
                .placeholder(R.drawable.ic_persona)
                .into(holder.imagen)
        } catch (e: Exception) {}

        // ----------------------------------------------------
        // AQUÍ AGREGAMOS LA LÓGICA AL TOCAR EL USUARIO
        // ----------------------------------------------------
        holder.itemView.setOnClickListener {
            mostrarOpciones(modelo, context)
        }
    }

    override fun getItemCount(): Int = listaUsuarios.size

    inner class HolderUsuario(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagen: ShapeableImageView = itemView.findViewById(R.id.iv_perfil_item)
        val nombre: TextView = itemView.findViewById(R.id.tv_nombre_item)
        val email: TextView = itemView.findViewById(R.id.tv_email_item)
        val rol: TextView = itemView.findViewById(R.id.tv_rol_item)
    }

    // --- FUNCIONES DEL ADMINISTRADOR ---

    private fun mostrarOpciones(modelo: ModeloUsuario, context: Context) {
        val opciones = arrayOf("Cambiar Rol", "Eliminar Usuario")

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Opciones: ${modelo.nombres}")
        builder.setItems(opciones) { _, which ->
            when (which) {
                0 -> cambiarRol(modelo, context)
                1 -> confirmarEliminacion(modelo, context)
            }
        }
        builder.show()
    }

    private fun cambiarRol(modelo: ModeloUsuario, context: Context) {
        // Alternamos el rol. Si es admin, lo bajamos a usuario. Si es usuario, lo subimos a admin.
        val nuevoRol = if (modelo.rol == "admin") "usuario" else "admin"

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(modelo.uid).child("rol").setValue(nuevoRol)
            .addOnSuccessListener {
                Toast.makeText(context, "Rol actualizado a $nuevoRol", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmarEliminacion(modelo: ModeloUsuario, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Eliminar Usuario")
        builder.setMessage("¿Estás seguro de que deseas eliminar permanentemente a ${modelo.nombres} de la base de datos?")

        builder.setPositiveButton("Eliminar") { _, _ ->
            val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
            ref.child(modelo.uid).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        // Un detalle visual para que el botón de eliminar sea rojo (opcional)
        val dialog = builder.create()
        dialog.show()
    }
}