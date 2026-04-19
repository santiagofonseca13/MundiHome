package com.mh.mundihome.Admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.mh.mundihome.Modelo.ModeloUsuario
import com.mh.mundihome.R

class GestionUsuarios : Fragment() {

    private lateinit var rvUsuarios: RecyclerView
    private lateinit var listaUsuarios: ArrayList<ModeloUsuario>
    private lateinit var adaptador: AdaptadorUsuario

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_gestion_usuarios, container, false)

        rvUsuarios = view.findViewById(R.id.rv_usuarios)
        rvUsuarios.layoutManager = LinearLayoutManager(context)

        listaUsuarios = ArrayList()
        adaptador = AdaptadorUsuario(listaUsuarios)
        rvUsuarios.adapter = adaptador

        obtenerUsuarios()

        return view
    }

    private fun obtenerUsuarios() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaUsuarios.clear()
                for (ds in snapshot.children) {
                    val modelo = ds.getValue(ModeloUsuario::class.java)
                    if (modelo != null) {
                        listaUsuarios.add(modelo)
                    }
                }
                adaptador.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}