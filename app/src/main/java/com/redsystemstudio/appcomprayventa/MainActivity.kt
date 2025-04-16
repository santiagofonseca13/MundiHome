package com.redsystemstudio.appcomprayventa

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.redsystemstudio.appcomprayventa.Anuncios.CrearAnuncio
import com.redsystemstudio.appcomprayventa.Fragmentos.FragmentChats
import com.redsystemstudio.appcomprayventa.Fragmentos.FragmentCuenta
import com.redsystemstudio.appcomprayventa.Fragmentos.FragmentInicio
import com.redsystemstudio.appcomprayventa.Fragmentos.FragmentMisAnuncios
import com.redsystemstudio.appcomprayventa.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        comprobarSesion()

        verFragmentInicio()

        binding.BottomNV.setOnItemSelectedListener {item->
            when(item.itemId){
                R.id.Item_Inicio->{
                    verFragmentInicio()
                    true
                }

                R.id.Item_Chats->{
                    verFragmentChats()
                    true
                }

                R.id.Item_Mis_Anuncios->{
                    verFragmentMisAnuncios()
                    true
                }

                R.id.Item_Cuenta->{
                    verFragmentCuenta()
                    true
                }
                else->{
                    false
                }
            }

        }

        binding.FAB.setOnClickListener {
            val intent = Intent(this, CrearAnuncio::class.java)
            intent.putExtra("Edicion", false)
            startActivity(intent)
        }

    }

    private fun comprobarSesion(){
        if (firebaseAuth.currentUser == null){
            startActivity(Intent(this, OpcionesLogin::class.java))
            finishAffinity()
        }else{
            agregarFcmToken()
            solicitarPermisoNotificacion()
        }
    }

    private fun verFragmentInicio(){
        binding.TituloRl.text = "Inicio"
        val fragment = FragmentInicio()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "FragmentInicio")
        fragmentTransition.commit()
    }

    private fun verFragmentChats(){
        binding.TituloRl.text = "Chats"
        val fragment = FragmentChats()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "FragmentChats")
        fragmentTransition.commit()
    }

    private fun verFragmentMisAnuncios(){
        binding.TituloRl.text = "Anuncios"
        val fragment = FragmentMisAnuncios()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "FragmentMisAnuncios")
        fragmentTransition.commit()
    }

    private fun verFragmentCuenta(){
        binding.TituloRl.text = "Cuenta"
        val fragment = FragmentCuenta()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "FragmentCuenta")
        fragmentTransition.commit()
    }

    private fun agregarFcmToken(){
        val miUid = "${firebaseAuth.uid}"

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener {fcmToken->
                val hashMap = HashMap<String, Any>()
                hashMap["fcmToken"] = "$fcmToken"
                val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
                ref.child(miUid)
                    .updateChildren(hashMap)
                    .addOnSuccessListener {
                        //El token se agregó con éxito
                    }
                    .addOnFailureListener {e->
                        Toast.makeText(
                            this,
                            "${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener {e->
                Toast.makeText(
                    this,
                    "${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun solicitarPermisoNotificacion(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_DENIED){
                permisoNotificacion.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val permisoNotificacion =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){esConcedido->
            //Aqui se concede el permiso
        }

    private fun actualizarEstado(estado : String){
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser!=null){
            val ref = FirebaseDatabase.getInstance().reference.child("Usuarios").child(firebaseAuth.uid!!)
            val hashMap = HashMap<String, Any>()
            hashMap["estado"] = estado
            ref!!.updateChildren(hashMap)
        }

    }

    override fun onResume() {
        super.onResume()
        actualizarEstado("online")
    }

    override fun onPause() {
        super.onPause()
        actualizarEstado("offline")
    }
}