package com.redsystemstudio.appcomprayventa

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.redsystemstudio.appcomprayventa.databinding.ActivityEliminarCuentaBinding

class Eliminar_cuenta : AppCompatActivity() {

    private lateinit var binding : ActivityEliminarCuentaBinding

    private lateinit var progressDialog : ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private var firebaseUser : FirebaseUser?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEliminarCuentaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.BtnEliminarMiCuenta.setOnClickListener {
            eliminarCuenta()
        }
    }

    private fun eliminarCuenta() {
        progressDialog.setMessage("Eliminando su cuenta")
        progressDialog.show()

        val miUid = firebaseAuth.uid

        firebaseUser!!.delete()
            .addOnSuccessListener {
                val anuncios = FirebaseDatabase.getInstance().getReference("Anuncios")
                anuncios.orderByChild("uid").equalTo(miUid)
                    .addValueEventListener(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (ds in snapshot.children){
                                ds.ref.removeValue()
                            }

                            val usuarioBD = FirebaseDatabase.getInstance().getReference("Usuarios")
                            usuarioBD.child(miUid!!).removeValue()
                                .addOnSuccessListener {
                                    progressDialog.dismiss()
                                    irMainActivity()
                                }
                                .addOnFailureListener {e->
                                progressDialog.dismiss()
                                    Toast.makeText(this@Eliminar_cuenta, "${e.message}",Toast.LENGTH_SHORT).show()
                                    irMainActivity()
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}",Toast.LENGTH_SHORT).show()
            }



    }

    private fun irMainActivity(){
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    override fun onBackPressed() {
        irMainActivity()
    }
}