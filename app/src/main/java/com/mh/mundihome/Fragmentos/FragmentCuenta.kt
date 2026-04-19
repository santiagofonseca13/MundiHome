package com.mh.mundihome.Fragmentos

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mh.mundihome.CambiarPassword
import com.mh.mundihome.Constantes
import com.mh.mundihome.EditarPerfil
import com.mh.mundihome.Eliminar_cuenta
import com.mh.mundihome.OpcionesLogin
import com.mh.mundihome.R
import com.mh.mundihome.databinding.FragmentCuentaBinding

class FragmentCuenta : Fragment() {

    private lateinit var binding : FragmentCuentaBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mContext : Context
    private lateinit var progressDialog : ProgressDialog

    // Variable para control RBAC
    private var isCuentaEnabled = true

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCuentaBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(mContext)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        verificarModuloRBAC()

        binding.BtnEditarPerfil.setOnClickListener {
            if (isCuentaEnabled) startActivity(Intent(mContext, EditarPerfil::class.java))
        }

        binding.BtnCambiarPass.setOnClickListener {
            if (isCuentaEnabled) startActivity(Intent(mContext, CambiarPassword::class.java))
        }

        binding.BtnVerificarCuenta.setOnClickListener {
            if (isCuentaEnabled) verificarCuenta()
        }

        binding.BtnEliminarAnuncios.setOnClickListener {
            if (!isCuentaEnabled) return@setOnClickListener

            val alertDialog = MaterialAlertDialogBuilder(mContext)
            alertDialog.setTitle("Eliminar todos mis anuncios")
                .setMessage("¿Estás seguro(a) de eliminar todos tus anuncios?")
                .setPositiveButton("Eliminar"){dialog, which->
                    eliminarTodosMiAnuncios()
                }
                .setNegativeButton("Cancelar"){dialog, which->
                    dialog.dismiss()
                }
                .show()
        }

        binding.BtnEliminarCuenta.setOnClickListener {
            if (isCuentaEnabled) startActivity(Intent(mContext, Eliminar_cuenta::class.java))
        }

        // --- CORRECCIÓN: Botón de Cerrar Sesión Seguro ---
        binding.BtnCerrarSesion.setOnClickListener {
            cerrarSesionSeguro()
        }
    }

    /**
     * Función que unifica la actualización del estado "Offline" y el cierre de sesión,
     * asegurando que no haya bloqueos, esperas innecesarias ni NullPointerExceptions.
     */
    private fun cerrarSesionSeguro() {
        val uid = firebaseAuth.uid
        if (uid == null) {
            irALogin()
            return
        }
        
        progressDialog.setMessage("Cerrando sesión...")
        progressDialog.show()

        val ref = FirebaseDatabase.getInstance().reference.child("Usuarios").child(uid)
        val hashMap = HashMap<String, Any>()
        hashMap["estado"] = "Offline"

        // Actualizamos Firebase. Usamos addOnCompleteListener para que, sin importar
        // si hay internet o falla, el usuario siempre pueda salir de la app.
        ref.updateChildren(hashMap).addOnCompleteListener {
            firebaseAuth.signOut()
            if (progressDialog.isShowing) progressDialog.dismiss()
            irALogin()
        }
    }

    /**
     * Valida que el fragmento siga vivo (isAdded) antes de lanzar el Intent,
     * evitando crashes si el usuario minimizó la app mientras cerraba sesión.
     */
    private fun irALogin() {
        if (isAdded && activity != null) {
            val intent = Intent(activity, OpcionesLogin::class.java)
            startActivity(intent)
            activity?.finishAffinity()
        }
    }

    private fun verificarModuloRBAC() {
        val refModulos = FirebaseDatabase.getInstance().getReference("Configuracion/Modulos")
        refModulos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    isCuentaEnabled = snapshot.child("cuenta_enabled").getValue(Boolean::class.java) ?: true

                    if (isCuentaEnabled) {
                        binding.BtnEditarPerfil.visibility = View.VISIBLE
                        binding.BtnCambiarPass.visibility = View.VISIBLE
                        binding.BtnEliminarAnuncios.visibility = View.VISIBLE
                        binding.BtnEliminarCuenta.visibility = View.VISIBLE
                        leerInfo()
                    } else {
                        binding.BtnEditarPerfil.visibility = View.GONE
                        binding.BtnCambiarPass.visibility = View.GONE
                        binding.BtnEliminarAnuncios.visibility = View.GONE
                        binding.BtnEliminarCuenta.visibility = View.GONE
                        binding.BtnVerificarCuenta.visibility = View.GONE

                        binding.TvEmail.text = "Correo Oculto (Mantenimiento)"
                        binding.TvNombres.text = "Usuario"
                        binding.TvNacimiento.text = "***"
                        binding.TvTelefono.text = "***"
                        binding.TvMiembro.text = "---"
                        binding.TvEstadoCuenta.text = "No disponible"
                        binding.IvPerfil.setImageResource(R.drawable.img_perfil)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun eliminarTodosMiAnuncios() {
        val miUid = firebaseAuth.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios").orderByChild("uid").equalTo(miUid)
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children){
                    ds.ref.removeValue()
                }
                if (isAdded) Toast.makeText(mContext, "Se han eliminado todos sus anuncios",Toast.LENGTH_SHORT).show()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun leerInfo() {
        if (!isCuentaEnabled) return
        val uid = firebaseAuth.uid ?: return

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isCuentaEnabled || !isAdded) return

                    val nombres = "${snapshot.child("nombres").value}"
                    val email = "${snapshot.child("email").value}"
                    val imagen = "${snapshot.child("urlImagenPerfil").value}"
                    val f_nac = "${snapshot.child("fecha_nac").value}"
                    var tiempo = "${snapshot.child("tiempo").value}"
                    val telefono = "${snapshot.child("telefono").value}"
                    val codTelefono = "${snapshot.child("codigoTelefono").value}"
                    val proveedor = "${snapshot.child("proveedor").value}"

                    val cod_tel = codTelefono+telefono
                    if (tiempo == "null") tiempo = "0"
                    val for_tiempo = Constantes.obtenerFecha(tiempo.toLong())

                    binding.TvEmail.text = email
                    binding.TvNombres.text = nombres
                    binding.TvNacimiento.text = f_nac
                    binding.TvTelefono.text = cod_tel
                    binding.TvMiembro.text = for_tiempo

                    try {
                        Glide.with(mContext)
                            .load(imagen)
                            .placeholder(R.drawable.img_perfil)
                            .into(binding.IvPerfil)
                    }catch (e:Exception){ }

                    if (proveedor == "Email"){
                        val esVerificado = firebaseAuth.currentUser?.isEmailVerified == true
                        if (esVerificado){
                            binding.BtnVerificarCuenta.visibility = View.GONE
                            binding.TvEstadoCuenta.text = "Verificado"
                        }else{
                            binding.BtnVerificarCuenta.visibility = View.VISIBLE
                            binding.TvEstadoCuenta.text = "No verificado"
                        }
                    }else{
                        binding.BtnVerificarCuenta.visibility = View.GONE
                        binding.TvEstadoCuenta.text = "Verificado"
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun verificarCuenta(){
        progressDialog.setMessage("Enviando instrucciones de verificación...")
        progressDialog.show()

        firebaseAuth.currentUser?.sendEmailVerification()
            ?.addOnSuccessListener {
                if (progressDialog.isShowing) progressDialog.dismiss()
                Toast.makeText(mContext, "Instrucciones enviadas a su correo", Toast.LENGTH_SHORT).show()
            }
            ?.addOnFailureListener {e->
                if (progressDialog.isShowing) progressDialog.dismiss()
                Toast.makeText(mContext, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}