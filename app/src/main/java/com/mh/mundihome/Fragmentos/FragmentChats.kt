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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mh.mundihome.Adaptadores.AdaptadorChats
import com.mh.mundihome.Modelo.ModeloChats
import com.mh.mundihome.databinding.FragmentChatsBinding

class FragmentChats : Fragment() {

    private lateinit var binding : FragmentChatsBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private var miUid = ""

    private lateinit var chatsArrayList : ArrayList<ModeloChats>
    private lateinit var adaptadorChats : AdaptadorChats
    private lateinit var mContext : Context

    // Variable para controlar si el módulo está activo (RBAC)
    private var isChatsEnabled = true

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentChatsBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        miUid = "${firebaseAuth.uid}"

        // 1. Inicializamos la lista y el adaptador vacíos por precaución
        chatsArrayList = ArrayList()
        adaptadorChats = AdaptadorChats(mContext, chatsArrayList)
        binding.chatsRv.adapter = adaptadorChats

        // 2. Iniciamos el control de acceso RBAC
        verificarModuloRBAC()

        // 3. Configuración de Búsqueda
        binding.EtBuscar.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(filtro: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    val consulta = filtro.toString()
                    adaptadorChats.filter.filter(consulta)
                }catch (e:Exception){ }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    /**
     * Verifica en tiempo real si el administrador ha habilitado o deshabilitado los chats.
     */
    private fun verificarModuloRBAC() {
        val refModulos = mDatabase.child("Configuracion").child("Modulos")
        refModulos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    isChatsEnabled = snapshot.child("chats_enabled").getValue(Boolean::class.java) ?: true

                    if (isChatsEnabled) {
                        // El módulo está activo: Mostramos la vista y cargamos los datos
                        binding.chatsRv.visibility = View.VISIBLE
                        binding.EtBuscar.isEnabled = true
                        cargarChats()
                    } else {
                        // El módulo fue desactivado: Ocultamos y limpiamos los datos por seguridad
                        binding.chatsRv.visibility = View.GONE
                        binding.EtBuscar.isEnabled = false
                        binding.EtBuscar.setText("") // Limpiamos la búsqueda si había algo

                        chatsArrayList.clear()
                        adaptadorChats.notifyDataSetChanged()

                        Toast.makeText(mContext, "El módulo de chats está en mantenimiento.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun cargarChats() {
        // Doble validación: Si está deshabilitado, no solicitamos nada a Firebase
        if (!isChatsEnabled) return

        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // Validación por si se desactiva justo mientras llegan los datos
                if (!isChatsEnabled) return

                chatsArrayList.clear()
                for (ds in snapshot.children){
                    val chatKey = "${ds.key}" //uidemisor_uidreceptor
                    if (chatKey.contains(miUid)){
                        val modeloChats = ModeloChats()
                        modeloChats.keyChat = chatKey
                        chatsArrayList.add(modeloChats)
                    }
                }
                // Como el adaptador ya está asignado al RecyclerView, solo notificamos los cambios
                adaptadorChats.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Puedes mostrar un error aquí si falla la conexión
            }
        })
    }
}