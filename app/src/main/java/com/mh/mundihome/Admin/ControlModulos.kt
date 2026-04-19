package com.mh.mundihome.Admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.database.*
import com.mh.mundihome.R

class ControlModulos : Fragment() {

    private lateinit var dbRef: DatabaseReference

    // Declaración de switches
    private lateinit var swInicio: SwitchMaterial
    private lateinit var swChats: SwitchMaterial
    private lateinit var swPublicar: SwitchMaterial
    private lateinit var swAnuncios: SwitchMaterial
    private lateinit var swCuenta: SwitchMaterial
    private lateinit var swUbicacion: SwitchMaterial

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_control_modulos, container, false)

        dbRef = FirebaseDatabase.getInstance().getReference("Configuracion/Modulos")

        // Inicializar vistas
        swInicio = view.findViewById(R.id.sw_inicio)
        swChats = view.findViewById(R.id.sw_chats)
        swPublicar = view.findViewById(R.id.sw_publicar)
        swAnuncios = view.findViewById(R.id.sw_anuncios)
        swCuenta = view.findViewById(R.id.sw_cuenta)
        swUbicacion = view.findViewById(R.id.sw_ubicacion)

        cargarEstadoModulos()
        configurarListeners()

        return view
    }

    private fun cargarEstadoModulos() {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    swInicio.isChecked = snapshot.child("inicio_enabled").getValue(Boolean::class.java) ?: true
                    swChats.isChecked = snapshot.child("chats_enabled").getValue(Boolean::class.java) ?: true
                    swPublicar.isChecked = snapshot.child("publicar_enabled").getValue(Boolean::class.java) ?: true
                    swAnuncios.isChecked = snapshot.child("anuncios_enabled").getValue(Boolean::class.java) ?: true
                    swCuenta.isChecked = snapshot.child("cuenta_enabled").getValue(Boolean::class.java) ?: true
                    swUbicacion.isChecked = snapshot.child("ubicacion_enabled").getValue(Boolean::class.java) ?: true
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun configurarListeners() {
        swInicio.setOnCheckedChangeListener { _, isChecked -> actualizarModulo("inicio_enabled", isChecked) }
        swChats.setOnCheckedChangeListener { _, isChecked -> actualizarModulo("chats_enabled", isChecked) }
        swPublicar.setOnCheckedChangeListener { _, isChecked -> actualizarModulo("publicar_enabled", isChecked) }
        swAnuncios.setOnCheckedChangeListener { _, isChecked -> actualizarModulo("anuncios_enabled", isChecked) }
        swCuenta.setOnCheckedChangeListener { _, isChecked -> actualizarModulo("cuenta_enabled", isChecked) }
        swUbicacion.setOnCheckedChangeListener { _, isChecked -> actualizarModulo("ubicacion_enabled", isChecked) }
    }

    private fun actualizarModulo(key: String, valor: Boolean) {
        dbRef.child(key).setValue(valor).addOnSuccessListener {
            Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show()
        }
    }
}