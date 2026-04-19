package com.mh.mundihome.Fragmentos

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private lateinit var mDatabase: DatabaseReference

    // Inicializamos la lista de inmediato para evitar NullPointerException
    private var anunciosArrayList = ArrayList<ModeloAnuncio>()
    private lateinit var anunciosAdaptador : AdaptadorAnuncio

    // Variable para control RBAC
    private var isAnunciosEnabled = true

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
        mDatabase = FirebaseDatabase.getInstance().reference

        // 1. INICIALIZAR EL ADAPTADOR AQUÍ (Esto evita que la app se cierre al buscar)
        anunciosAdaptador = AdaptadorAnuncio(mContext, anunciosArrayList)
        binding.misAnunciosRv.adapter = anunciosAdaptador

        // 2. Iniciar el control de acceso y ahorro de datos (RBAC)
        verificarModuloRBAC()

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
                        cargarMisAnuncios()
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

    private fun cargarMisAnuncios() {
        // Primera barrera: Evitar peticiones si el módulo está desactivado
        if (!isAnunciosEnabled) return

        val uid = firebaseAuth.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")

        ref.orderByChild("uid").equalTo(uid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Segunda barrera por asincronía (por si se apaga mientras descarga los datos)
                    if (!isAnunciosEnabled) return

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
                    // NOTIFICAR CAMBIOS (No recrear el adaptador, solo avisar que hay datos nuevos)
                    if (::anunciosAdaptador.isInitialized) {
                        anunciosAdaptador.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}