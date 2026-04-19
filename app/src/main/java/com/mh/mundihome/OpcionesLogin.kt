package com.mh.mundihome

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mh.mundihome.Admin.AdminMainActivity
import com.mh.mundihome.databinding.ActivityOpcionesLoginBinding

class OpcionesLogin : AppCompatActivity() {

    private lateinit var binding : ActivityOpcionesLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient : GoogleSignInClient
    private lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpcionesLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        // Configuramos Google Sign-In ANTES de comprobar la sesión
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        comprobarSesion()

        binding.IngresarGoogle.setOnClickListener {
            googleLogin()
        }
    }

    private fun googleLogin() {
        // Al hacer signOut() borramos el caché del dispositivo.
        // Esto obliga a Google a mostrar la ventana de "Selecciona una cuenta"
        mGoogleSignInClient.signOut().addOnCompleteListener {
            val googleSignInIntent = mGoogleSignInClient.signInIntent
            googleSignInARL.launch(googleSignInIntent)
        }
    }

    private val googleSignInARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){ resultado->
        if (resultado.resultCode == RESULT_OK){
            val data = resultado.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val cuenta = task.getResult(ApiException::class.java)
                val idToken = cuenta.idToken

                // CORRECCIÓN 2: Evitar Crash si el token de Google viene nulo
                if (idToken != null) {
                    autenticacionGoogle(idToken)
                } else {
                    Toast.makeText(this, "Error de Google: Token nulo", Toast.LENGTH_SHORT).show()
                }
            }catch (e: ApiException){
                // EXPLICACIÓN: Si el error es ApiException 10 o 12500, tu SHA-1 no coincide en Firebase
                Toast.makeText(this, "Fallo en Google Sign-In: ${e.statusCode}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun autenticacionGoogle(idToken: String) {
        progressDialog.setMessage("Iniciando sesión...")
        progressDialog.show()

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { resultadoAuth->

                // CORRECCIÓN 3: Uso seguro de additionalUserInfo sin "!!"
                val isNewUser = resultadoAuth.additionalUserInfo?.isNewUser ?: false

                if (isNewUser){
                    llenarInfoBD()
                }else{
                    val uid = firebaseAuth.uid
                    if (uid != null) comprobarRol(uid) else cerrarDialogo()
                }
            }
            .addOnFailureListener { e->
                cerrarDialogo()
                Toast.makeText(this, "${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun llenarInfoBD() {
        progressDialog.setMessage("Guardando información")

        val uidUsuario = firebaseAuth.uid
        if (uidUsuario == null) {
            cerrarDialogo()
            return
        }

        val tiempo = Constantes.obtenerTiempoDis()
        val emailUsuario = firebaseAuth.currentUser?.email ?: ""
        val nombreUsuario = firebaseAuth.currentUser?.displayName ?: ""

        val hashMap = HashMap<String, Any>()
        hashMap["nombres"] = nombreUsuario
        hashMap["codigoTelefono"] = ""
        hashMap["telefono"] = ""
        hashMap["urlImagenPerfil"] = ""
        hashMap["proveedor"] = "Google"
        hashMap["escribiendo"] = ""
        hashMap["tiempo"] = tiempo
        hashMap["estado"] = "online"
        hashMap["email"] = emailUsuario
        hashMap["uid"] = uidUsuario
        hashMap["fecha_nac"] = ""
        hashMap["rol"] = "usuario"

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uidUsuario)
            .setValue(hashMap)
            .addOnSuccessListener {
                cerrarDialogo()
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e->
                cerrarDialogo()
                Toast.makeText(this, "No se registró debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun comprobarSesion(){
        val uid = firebaseAuth.uid
        if (uid != null){
            progressDialog.setMessage("Verificando credenciales...")
            progressDialog.show()
            comprobarRol(uid)
        }
    }

    private fun comprobarRol(uid: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cerrarDialogo()

                val rol = snapshot.child("rol").value?.toString() ?: "usuario"

                if (rol == "admin") {
                    startActivity(Intent(this@OpcionesLogin, AdminMainActivity::class.java))
                    Toast.makeText(this@OpcionesLogin, "Bienvenido Administrador", Toast.LENGTH_SHORT).show()
                } else {
                    startActivity(Intent(this@OpcionesLogin, MainActivity::class.java))
                }
                finishAffinity()
            }

            override fun onCancelled(error: DatabaseError) {
                cerrarDialogo()
                Toast.makeText(this@OpcionesLogin, "Error al leer rol: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // CORRECCIÓN 4: Prevención de WindowLeaked (Crash de interfaz)
    private fun cerrarDialogo() {
        if (!isFinishing && !isDestroyed && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }
}