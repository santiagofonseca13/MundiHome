package com.redsystemstudio.appcomprayventa

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.redsystemstudio.appcomprayventa.Opciones_login.Login_email
import com.redsystemstudio.appcomprayventa.databinding.ActivityCambiarPasswordBinding

class CambiarPassword : AppCompatActivity() {

    private lateinit var binding : ActivityCambiarPasswordBinding
    private lateinit var progressDialog : ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCambiarPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.BtnActualizarPass.setOnClickListener {
            validarInformacion()
        }
    }

    private var password_actual = ""
    private var password_nueva = ""
    private var password_nueva_r = ""
    private fun validarInformacion() {
        password_actual = binding.EtPasswordActual.text.toString().trim()
        password_nueva = binding.EtPasswordNueva.text.toString().trim()
        password_nueva_r = binding.EtPasswordNuevaR.text.toString().trim()

        if (password_actual.isEmpty()){
            binding.EtPasswordActual.error = "Ingrese password actual"
            binding.EtPasswordActual.requestFocus()
        }else if (password_nueva.isEmpty()){
            binding.EtPasswordNueva.error = "Ingrese nuevo password"
            binding.EtPasswordNueva.requestFocus()
        }else if (password_nueva_r.isEmpty()){
            binding.EtPasswordNuevaR.error = "Confirme nuevo password"
            binding.EtPasswordNuevaR.requestFocus()
        }else if (password_nueva!=password_nueva_r){
            binding.EtPasswordNuevaR.error = "Los password no coinciden"
            binding.EtPasswordNuevaR.requestFocus()
        }else{
            autenticarUsuarioCamPass()
        }

    }

    private fun autenticarUsuarioCamPass() {
        progressDialog.setMessage("Autenticando usuario")
        progressDialog.show()

        val autoCredencial = EmailAuthProvider.getCredential(firebaseUser.email.toString(), password_actual)
        firebaseUser.reauthenticate(autoCredencial)
            .addOnSuccessListener {
                progressDialog.dismiss()
                actualizarPassword()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }



    }

    private fun actualizarPassword() {
        progressDialog.setMessage("Cambiando password")
        progressDialog.show()

        firebaseUser.updatePassword(password_nueva)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Su contraseña ha sido actualizada",
                    Toast.LENGTH_SHORT
                ).show()

                firebaseAuth.signOut()
                startActivity(Intent(this, Login_email::class.java))
                finishAffinity()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }





















    }
}