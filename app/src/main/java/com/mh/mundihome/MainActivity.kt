package com.mh.mundihome

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.mh.mundihome.Anuncios.CrearAnuncio
import com.mh.mundihome.Fragmentos.FragmentChats
import com.mh.mundihome.Fragmentos.FragmentCuenta
import com.mh.mundihome.Fragmentos.FragmentInicio
import com.mh.mundihome.Fragmentos.FragmentMisAnuncios
import com.mh.mundihome.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    // Control de UX
    private var isFirstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        comprobarSesion()
        verFragmentInicio()

        // --- INICIAR SISTEMA RBAC Y SIMETRÍA ---
        obtenerEstadoModulos()

        // Listener del Menú Inferior
        binding.BottomNV.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Item_Inicio -> {
                    verFragmentInicio()
                    true
                }
                R.id.Item_Chats -> {
                    verFragmentChats()
                    true
                }
                R.id.Item_Mis_Anuncios -> {
                    verFragmentMisAnuncios()
                    true
                }
                R.id.Item_Cuenta -> {
                    verFragmentCuenta()
                    true
                }
                R.id.Item_vender -> {
                    val intent = Intent(this, CrearAnuncio::class.java)
                    intent.putExtra("Edicion", false)
                    startActivity(intent)
                    false
                }
                else -> false
            }
        }

        // Botón Central (Vender/Publicar)
        binding.FAB.setOnClickListener {
            val intent = Intent(this, CrearAnuncio::class.java)
            intent.putExtra("Edicion", false)
            startActivity(intent)
        }
    }

    /**
     * Lógica Maestra: Controla qué módulos se ven y aplica la paridad
     * para mantener el botón de Vender siempre centrado.
     */
    private fun obtenerEstadoModulos() {
        val refModulos = mDatabase.child("Configuracion").child("Modulos")

        refModulos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val inicio = snapshot.child("inicio_enabled").getValue(Boolean::class.java) ?: true
                    val chats = snapshot.child("chats_enabled").getValue(Boolean::class.java) ?: true
                    val anuncios = snapshot.child("anuncios_enabled").getValue(Boolean::class.java) ?: true
                    val cuenta = snapshot.child("cuenta_enabled").getValue(Boolean::class.java) ?: true
                    val publicarMaestro = snapshot.child("publicar_enabled").getValue(Boolean::class.java) ?: true

                    var contadorActivos = 0
                    if (inicio) contadorActivos++
                    if (chats) contadorActivos++
                    if (anuncios) contadorActivos++
                    if (cuenta) contadorActivos++

                    // --- LÓGICA CORREGIDA ---

                    // El ÍTEM del menú se muestra siempre que el admin lo permita
                    binding.BottomNV.menu.findItem(R.id.Item_vender).isVisible = publicarMaestro

                    // El FAB (círculo verde) solo aparece si hay paridad (para estar en el centro)
                    val debeMostrarFAB = (contadorActivos % 2 == 0) && publicarMaestro
                    binding.FAB.visibility = if (debeMostrarFAB) View.VISIBLE else View.GONE

                    // Actualizar resto de módulos
                    binding.BottomNV.menu.findItem(R.id.Item_Inicio).isVisible = inicio
                    binding.BottomNV.menu.findItem(R.id.Item_Chats).isVisible = chats
                    binding.BottomNV.menu.findItem(R.id.Item_Mis_Anuncios).isVisible = anuncios
                    binding.BottomNV.menu.findItem(R.id.Item_Cuenta).isVisible = cuenta

                    if (!isFirstLoad) { verificarExpulsion(chats, anuncios, cuenta) }
                    isFirstLoad = false
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun verificarExpulsion(chats: Boolean, anuncios: Boolean, cuenta: Boolean) {
        val titulo = binding.TituloRl.text.toString()
        if ((!chats && titulo == "Chats") || (!anuncios && titulo == "Anuncios") || (!cuenta && titulo == "Cuenta")) {
            verFragmentInicio()
            binding.BottomNV.selectedItemId = R.id.Item_Inicio
            Toast.makeText(this, "Esta sección ha sido deshabilitada por el administrador.", Toast.LENGTH_LONG).show()
        }
    }

    // --- GESTIÓN DE FRAGMENTOS ---

    private fun verFragmentInicio() {
        binding.TituloRl.text = "Inicio"
        supportFragmentManager.beginTransaction().replace(binding.FragmentL1.id, FragmentInicio(), "FragmentInicio").commit()
    }

    private fun verFragmentChats() {
        binding.TituloRl.text = "Chats"
        supportFragmentManager.beginTransaction().replace(binding.FragmentL1.id, FragmentChats(), "FragmentChats").commit()
    }

    private fun verFragmentMisAnuncios() {
        binding.TituloRl.text = "Anuncios"
        supportFragmentManager.beginTransaction().replace(binding.FragmentL1.id, FragmentMisAnuncios(), "FragmentMisAnuncios").commit()
    }

    private fun verFragmentCuenta() {
        binding.TituloRl.text = "Cuenta"
        supportFragmentManager.beginTransaction().replace(binding.FragmentL1.id, FragmentCuenta(), "FragmentCuenta").commit()
    }

    // --- FUNCIONES DE SESIÓN Y ESTADO ---

    private fun comprobarSesion() {
        if (firebaseAuth.currentUser == null) {
            startActivity(Intent(this, OpcionesLogin::class.java))
            finishAffinity()
        } else {
            actualizarFcmToken()
            solicitarPermisoNotificacion()
        }
    }

    private fun actualizarFcmToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            mDatabase.child("Usuarios").child(firebaseAuth.uid!!).child("fcmToken").setValue(token)
        }
    }

    private fun solicitarPermisoNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun actualizarEstadoPresencia(estado: String) {
        if (firebaseAuth.uid != null) {
            mDatabase.child("Usuarios").child(firebaseAuth.uid!!).child("estado").setValue(estado)
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarEstadoPresencia("online")
    }

    override fun onPause() {
        super.onPause()
        actualizarEstadoPresencia("offline")
    }
}