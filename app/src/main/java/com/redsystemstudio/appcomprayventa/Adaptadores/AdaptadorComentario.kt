package com.redsystemstudio.appcomprayventa.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.redsystemstudio.appcomprayventa.Constantes
import com.redsystemstudio.appcomprayventa.Modelo.ModeloComentario
import com.redsystemstudio.appcomprayventa.R
import com.redsystemstudio.appcomprayventa.databinding.ItemComentarioBinding

class AdaptadorComentario : RecyclerView.Adapter<AdaptadorComentario.HolderComentario> {
    val context : Context
    val comentarioArrayList : ArrayList<ModeloComentario>

    private lateinit var binding : ItemComentarioBinding
    private lateinit var firebaseAuth: FirebaseAuth

    constructor(context: Context, comentarioArrayList: ArrayList<ModeloComentario>) {
        this.context = context
        this.comentarioArrayList = comentarioArrayList
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComentario {
        binding = ItemComentarioBinding.inflate(LayoutInflater.from(context),parent, false)
        return HolderComentario(binding.root)
    }

    override fun getItemCount(): Int {
        return comentarioArrayList.size
    }

    override fun onBindViewHolder(holder: HolderComentario, position: Int) {
        val modelo = comentarioArrayList[position]

        val id = modelo.id
        val comentario = modelo.comentario
        val uid = modelo.uid
        val tiempo = modelo.tiempo

        val f_fecha = Constantes.obtenerFecha(tiempo.toLong())

        holder.Tv_fecha.text = f_fecha
        holder.Tv_comentario.text = comentario

        cargarInformacion(modelo, holder)

        holder.itemView.setOnClickListener {
            dialogEliminarCom(modelo, holder)
        }

    }

    private fun dialogEliminarCom(modelo: ModeloComentario, holder: AdaptadorComentario.HolderComentario) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Eliminar comentario")
        builder.setMessage("¿Estás seguro(a) de eliminar este comentario?")
            .setPositiveButton("Eliminar"){d,e->
                val uidUsuario = modelo.uid
                if (firebaseAuth.uid.equals(uidUsuario)){
                    val uidVendedor = modelo.uid_vendedor
                    val idComentario = modelo.id
                    val ref = FirebaseDatabase.getInstance().getReference("ComentariosVendedores")
                    ref.child(uidVendedor).child("Comentarios").child(idComentario)
                        .removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(context,
                                "El comentario se ha eliminado",
                                Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e->
                            Toast.makeText(context,
                                "No se puede eliminar el comentario debido a ${e.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                }else{
                    //Si el comentario no nos pertenece
                    Toast.makeText(context,
                        "No puede eliminar este comentario, porque no le pertenece",
                        Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar"){d,e->
                    d.dismiss()
            }
            .show()
    }

    private fun cargarInformacion(modelo: ModeloComentario, holder: AdaptadorComentario.HolderComentario) {
        val uid = modelo.uid

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = "${snapshot.child("nombres").value}"
                    val imagen = "${snapshot.child("urlImagenPerfil").value}"

                    holder.Tv_nombres.text = nombres
                    try {
                        Glide.with(context)
                            .load(imagen)
                            .placeholder(R.drawable.img_perfil)
                            .into(holder.IvImagen)
                    }catch (e:Exception){
                        holder.IvImagen.setImageResource(R.drawable.img_perfil)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    inner class HolderComentario (itemView : View) : RecyclerView.ViewHolder(itemView){
        val IvImagen = binding.IvImagenPerfil
        val Tv_nombres = binding.TvNombresComentario
        val Tv_fecha = binding.TvFechaComentario
        val Tv_comentario = binding.TvComentario
    }


}