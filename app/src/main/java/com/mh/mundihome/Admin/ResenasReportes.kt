package com.mh.mundihome.Admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.mh.mundihome.Modelo.ModeloComentario
import com.mh.mundihome.R
import com.mh.mundihome.Admin.AdaptadorComentarioAdmin

class ResenasReportes : Fragment() {

    private lateinit var rvResenas: RecyclerView
    private lateinit var listaComentarios: ArrayList<ModeloComentario>
    private lateinit var adaptador: AdaptadorComentarioAdmin

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_gestion_usuarios, container, false)

        rvResenas = view.findViewById(R.id.rv_usuarios) // Asumiendo que reutilizas el XML
        rvResenas.layoutManager = LinearLayoutManager(context)

        listaComentarios = ArrayList()
        adaptador = AdaptadorComentarioAdmin(listaComentarios)
        rvResenas.adapter = adaptador

        obtenerTodasLasResenas()

        return view
    }

    private fun obtenerTodasLasResenas() {
        val ref = FirebaseDatabase.getInstance().getReference("ComentariosVendedores")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaComentarios.clear()

                // Recorremos cada nodo de Vendedor (ej. Ws7lwIMaPMWybREVcjYM2BQUzGh1)
                for (vendedorSnapshot in snapshot.children) {
                    val comentariosSnapshot = vendedorSnapshot.child("Comentarios")

                    // Recorremos cada comentario dentro de ese vendedor (ej. 1764309123703)
                    for (comentarioDs in comentariosSnapshot.children) {
                        try {
                            val modelo = comentarioDs.getValue(ModeloComentario::class.java)
                            if (modelo != null) {
                                listaComentarios.add(modelo)
                            }
                        } catch (e: Exception) {
                            // En caso de que haya un dato corrupto
                        }
                    }
                }

                // Opcional: Invertir la lista para que las reseñas más recientes salgan arriba
                listaComentarios.reverse()

                adaptador.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}