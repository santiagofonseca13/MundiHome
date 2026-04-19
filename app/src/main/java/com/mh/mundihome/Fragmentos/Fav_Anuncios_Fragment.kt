package com.mh.mundihome.Fragmentos

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mh.mundihome.Adaptadores.AdaptadorAnuncio
import com.mh.mundihome.Modelo.ModeloAnuncio
import com.mh.mundihome.databinding.FragmentFavAnunciosBinding

class Fav_Anuncios_Fragment : Fragment() {

    private lateinit var binding: FragmentFavAnunciosBinding
    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    private var anunciosArrayList = ArrayList<ModeloAnuncio>()
    private lateinit var anunciosAdaptador: AdaptadorAnuncio

    // Variable para control RBAC
    private var isAnunciosEnabled = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFavAnunciosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        // 1. Inicializar adaptador una sola vez
        anunciosAdaptador = AdaptadorAnuncio(mContext, anunciosArrayList)
        binding.anunciosRv.adapter = anunciosAdaptador

        // 2. Iniciar el control de acceso y ahorro de datos (RBAC)
        verificarModuloRBAC()

        // Buscador
        binding.EtBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(filtro: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    if (::anunciosAdaptador.isInitialized) {
                        anunciosAdaptador.filter.filter(filtro.toString())
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.IbLimpiar.setOnClickListener {
            binding.EtBuscar.setText("")
            if (::anunciosAdaptador.isInitialized) {
                anunciosAdaptador.filter.filter("")
            }
        }
    }

    /**
     * Verifica si el módulo está activo. Si se apaga, limpia la memoria y evita consultas.
     */
    private fun verificarModuloRBAC() {
        val refModulos = mDatabase.child("Configuracion").child("Modulos")
        refModulos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    isAnunciosEnabled = snapshot.child("anuncios_enabled").getValue(Boolean::class.java) ?: true

                    if (isAnunciosEnabled) {
                        cargarAnunciosFav()
                    } else {
                        // Si se desactiva, vaciamos la lista para liberar RAM
                        // y asegurarnos de que no queden datos expuestos.
                        anunciosArrayList.clear()
                        if (::anunciosAdaptador.isInitialized) {
                            anunciosAdaptador.notifyDataSetChanged()
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun cargarAnunciosFav() {
        // Primera barrera: Evitar peticiones si el módulo está desactivado
        if (!isAnunciosEnabled) return

        val uid = firebaseAuth.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios").child(uid).child("Favoritos")

        // Usamos addListenerForSingleValueEvent para mayor rapidez
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Segunda barrera por asincronía
                if (!isAnunciosEnabled) return

                anunciosArrayList.clear()

                // Si no hay favoritos, refrescar para mostrar lista vacía
                if (!snapshot.exists()) {
                    if (::anunciosAdaptador.isInitialized) {
                        anunciosAdaptador.notifyDataSetChanged()
                    }
                    return
                }

                for (ds in snapshot.children) {
                    val idAnuncio = "${ds.child("idAnuncio").value}"
                    obtenerDetalleAnuncio(idAnuncio)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun obtenerDetalleAnuncio(idAnuncio: String) {
        // Tercera barrera: Si el módulo se apagó mientras buscaba los IDs
        if (!isAnunciosEnabled) return

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios").child(idAnuncio)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAnunciosEnabled) return

                try {
                    val modeloAnuncio = snapshot.getValue(ModeloAnuncio::class.java)
                    if (modeloAnuncio != null) {
                        // Evitar duplicados en la lista visual
                        if (!anunciosArrayList.any { it.id == modeloAnuncio.id }) {
                            anunciosArrayList.add(modeloAnuncio)
                            // Notificar al adaptador cada vez que entra un anuncio para que aparezcan de inmediato
                            if (::anunciosAdaptador.isInitialized) {
                                anunciosAdaptador.notifyDataSetChanged()
                            }
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}