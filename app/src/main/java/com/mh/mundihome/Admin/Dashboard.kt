package com.mh.mundihome.Admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.mh.mundihome.R

class Dashboard : Fragment() {

    private lateinit var tvUsuarios: TextView
    private lateinit var tvAnuncios: TextView
    private lateinit var tvStatus: TextView
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Inicializar vistas
        tvUsuarios = view.findViewById(R.id.tv_count_usuarios)
        tvAnuncios = view.findViewById(R.id.tv_count_anuncios)
        tvStatus = view.findViewById(R.id.tv_status_reciente)
        database = FirebaseDatabase.getInstance()

        cargarEstadisticas()

        return view
    }

    private fun cargarEstadisticas() {
        // Contar Usuarios
        database.getReference("Usuarios").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    tvUsuarios.text = snapshot.childrenCount.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Contar Anuncios
        database.getReference("Anuncios").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    tvAnuncios.text = snapshot.childrenCount.toString()
                    tvStatus.text = "Plataforma operativa. ${snapshot.childrenCount} anuncios activos."
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}