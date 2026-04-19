package com.mh.mundihome.Admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.mh.mundihome.Modelo.ModeloAnuncio
import com.mh.mundihome.R

class ModeracionAnuncios : Fragment() {

    private lateinit var rvAnuncios: RecyclerView
    private lateinit var listaAnuncios: ArrayList<ModeloAnuncio>
    private lateinit var adaptador: AdaptadorAnuncioAdmin

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Asegúrate de que el XML del fragmento exista (puede ser idéntico al de Gestión de Usuarios)
        val view = inflater.inflate(R.layout.fragment_gestion_usuarios, container, false)

        // Nota: Estoy reutilizando el ID 'rv_usuarios' asumiendo que usaste el mismo layout.
        // Si creaste 'fragment_moderacion_anuncios.xml', cambia los IDs según corresponda.
        rvAnuncios = view.findViewById(R.id.rv_usuarios)
        rvAnuncios.layoutManager = LinearLayoutManager(context)

        listaAnuncios = ArrayList()
        adaptador = AdaptadorAnuncioAdmin(listaAnuncios)
        rvAnuncios.adapter = adaptador

        obtenerAnuncios()

        return view
    }

    private fun obtenerAnuncios() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaAnuncios.clear()
                for (ds in snapshot.children) {
                    try {
                        val modelo = ds.getValue(ModeloAnuncio::class.java)
                        if (modelo != null) {
                            listaAnuncios.add(modelo)
                        }
                    } catch (e: Exception) {
                        // El try-catch evita que la app explote si un anuncio viejo tiene datos incompatibles
                    }
                }
                adaptador.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}