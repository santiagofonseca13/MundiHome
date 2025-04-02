package com.mh.mundihome

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import com.google.firebase.auth.FirebaseAuth
import com.mh.mundihome.databinding.ActivityMainBinding
import com.mh.mundihome.fragmentos.fragmentCuenta
import com.mh.mundihome.fragmentos.fragmentInicio
import com.mh.mundihome.fragmentos.fragmentMisAnuncios
import com.mh.mundihome.fragmentos.frangmentChats

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        comprobarSesion()

        verfragmentInicio()

        binding.BottomNV.setOnItemSelectedListener { item->
            when(item.itemId){
                R.id.Item_Inicio->{
                    verfragmentInicio()
                    true
                }

                R.id.Item_Chats->{
                    verfragmentChats()
                    true
                }

                R.id.Item_mis_anuncios->{
                    verfragmentMisAnuncios()
                    true
                }

                R.id.Item_Cuenta->{
                    verfragmentCuenta()
                    true
                }
                else->{
                    false
                }
            }
        }
    }

    private fun comprobarSesion(){
        if (firebaseAuth.currentUser == null){
            startActivity(Intent(this, OpcionesLogin::class.java))
            finishAffinity()
        }
    }

    private fun verfragmentInicio(){
        binding.TTituloR1.text = "Inicio"
        val fragment = fragmentInicio()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "fragmentInicio")
        fragmentTransition.commit()
    }

    private fun verfragmentChats(){
        binding.TTituloR1.text = "Chats"
        val fragment = frangmentChats()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "frangmentChats")
        fragmentTransition.commit()
    }

    private fun verfragmentMisAnuncios(){
        binding.TTituloR1.text = "Mis Anuncios"
        val fragment = fragmentMisAnuncios()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "fragmentMisAnuncios")
        fragmentTransition.commit()
    }

    private fun verfragmentCuenta(){
        binding.TTituloR1.text = "Cuenta"
        val fragment = fragmentCuenta()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "fragmentCuenta")
        fragmentTransition.commit()
    }
}