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
import com.mh.mundihome.databinding.FragmentMisAnunciosPublicadosBinding

class Mis_Anuncios_Publicados_Fragment : Fragment() {

    private lateinit var binding : FragmentMisAnunciosPublicadosBinding
    private lateinit var mContext : Context
    private lateinit var firebaseAuth: FirebaseAuth

    // Inicializamos la lista de inmediato para evitar NullPointerException
    private var anunciosArrayList = ArrayList<ModeloAnuncio>()
    private lateinit var anunciosAdaptador : AdaptadorAnuncio

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMisAnunciosPublicadosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

        // 1. INICIALIZAR EL ADAPTADOR AQUÍ (Esto evita que la app se cierre al buscar)
        anunciosAdaptador = AdaptadorAnuncio(mContext, anunciosArrayList)
        binding.misAnunciosRv.adapter = anunciosAdaptador

        cargarMisAnuncios()

        // Lógica del buscador
        binding.EtBuscar.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(filtro: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    // Ahora es seguro filtrar porque el adaptador ya existe
                    anunciosAdaptador.filter.filter(filtro.toString())
                } catch (e: Exception){
                    e.printStackTrace()
                }
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

    private fun cargarMisAnuncios() {
        val uid = firebaseAuth.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")

        // Filtramos por UID. Usamos addValueEventListener si quieres cambios en tiempo real,
        // o addListenerForSingleValueEvent para más velocidad.
        ref.orderByChild("uid").equalTo(uid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    anunciosArrayList.clear()
                    for (ds in snapshot.children){
                        try {
                            val modeloAnuncio = ds.getValue(ModeloAnuncio::class.java)
                            if (modeloAnuncio != null) {
                                anunciosArrayList.add(modeloAnuncio)
                            }
                        } catch (e:Exception){
                            e.printStackTrace()
                        }
                    }
                    // 2. NOTIFICAR CAMBIOS (No recrear el adaptador, solo avisar que hay datos nuevos)
                    anunciosAdaptador.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Evitar el error "Not yet implemented" que cierra la app
                }
            })
    }
}