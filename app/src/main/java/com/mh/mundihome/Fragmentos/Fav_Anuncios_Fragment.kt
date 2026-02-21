package com.mh.mundihome.Fragmentos

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mh.mundihome.Adaptadores.AdaptadorAnuncio
import com.mh.mundihome.Modelo.ModeloAnuncio
import com.mh.mundihome.databinding.FragmentFavAnunciosBinding

class Fav_Anuncios_Fragment : Fragment() {

    private lateinit var binding: FragmentFavAnunciosBinding
    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth

    private var anunciosArrayList = ArrayList<ModeloAnuncio>()
    private lateinit var anunciosAdaptador: AdaptadorAnuncio

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

        // 1. Inicializar adaptador una sola vez
        anunciosAdaptador = AdaptadorAnuncio(mContext, anunciosArrayList)

        // USA EL ID QUE TENGAS EN EL XML (anunciosRv o favAnunciosRv)
        binding.anunciosRv.adapter = anunciosAdaptador

        cargarAnunciosFav()

        // Buscador
        binding.EtBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(filtro: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    anunciosAdaptador.filter.filter(filtro.toString())
                } catch (e: Exception) { e.printStackTrace() }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.IbLimpiar.setOnClickListener {
            binding.EtBuscar.setText("")
            anunciosAdaptador.filter.filter("")
        }
    }

    private fun cargarAnunciosFav() {
        val uid = firebaseAuth.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios").child(uid).child("Favoritos")

        // Usamos addListenerForSingleValueEvent para mayor rapidez
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                anunciosArrayList.clear()

                // Si no hay favoritos, refrescar para mostrar lista vacía
                if (!snapshot.exists()) {
                    anunciosAdaptador.notifyDataSetChanged()
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
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios").child(idAnuncio)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val modeloAnuncio = snapshot.getValue(ModeloAnuncio::class.java)
                    if (modeloAnuncio != null) {
                        // Evitar duplicados
                        if (!anunciosArrayList.any { it.id == modeloAnuncio.id }) {
                            anunciosArrayList.add(modeloAnuncio)
                            // Notificar al adaptador cada vez que entra un anuncio para que aparezcan de inmediato
                            anunciosAdaptador.notifyDataSetChanged()
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}