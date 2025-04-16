package com.redsystemstudio.appcomprayventa

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.redsystemstudio.appcomprayventa.databinding.ActivityRecuperarPasswordBinding

class RecuperarPassword : AppCompatActivity() {

    private lateinit var binding : ActivityRecuperarPasswordBinding
    private lateinit var progressDialog : ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecuperarPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.BtnEnviarInstrucciones.setOnClickListener {
            validarEmail()
        }
    }

    private var email = ""
    private fun validarEmail() {
        email = binding.EtEmail.text.toString().trim()
        if (email.isEmpty()){
            Toast.makeText(this, "Ingrese su correo",Toast.LENGTH_SHORT).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.EtEmail.error = "Email inválido"
            binding.EtEmail.requestFocus()
        }else{
            enviarInstrucciones()
        }

    }

    private fun enviarInstrucciones() {
        progressDialog.setMessage("Enviando instrucciones a email ${email}")
        progressDialog.dismiss()

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Las instrucciones fueron enviadas a su correo registrado",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "No se enviaron las instrucciones debido a ${e.message}",Toast.LENGTH_SHORT).show()
            }


















    }
}