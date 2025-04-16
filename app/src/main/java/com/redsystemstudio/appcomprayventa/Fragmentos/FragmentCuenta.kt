package com.redsystemstudio.appcomprayventa.Fragmentos

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
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
import com.redsystemstudio.appcomprayventa.CambiarPassword
import com.redsystemstudio.appcomprayventa.Constantes
import com.redsystemstudio.appcomprayventa.EditarPerfil
import com.redsystemstudio.appcomprayventa.Eliminar_cuenta
import com.redsystemstudio.appcomprayventa.OpcionesLogin
import com.redsystemstudio.appcomprayventa.R
import com.redsystemstudio.appcomprayventa.databinding.FragmentCuentaBinding

class FragmentCuenta : Fragment() {

    private lateinit var binding : FragmentCuentaBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mContext : Context
    private lateinit var progressDialog : ProgressDialog

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCuentaBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(mContext)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        leerInfo()

        binding.BtnEditarPerfil.setOnClickListener {
            startActivity(Intent(mContext, EditarPerfil::class.java))
        }

        binding.BtnCambiarPass.setOnClickListener {
            startActivity(Intent(mContext, CambiarPassword::class.java))
        }

        binding.BtnVerificarCuenta.setOnClickListener {
            verificarCuenta()
        }

        binding.BtnEliminarAnuncios.setOnClickListener {
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
             startActivity(Intent(mContext, Eliminar_cuenta::class.java))
        }

        binding.BtnCerrarSesion.setOnClickListener {
            actualizarEstado()
            cerrarSesion()
        }
    }

    private fun cerrarSesion(){
        object : CountDownTimer(3000, 1000){
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                //Pasado 3 segundos se va a ejecutar estas líneas de código
                firebaseAuth.signOut()
                startActivity(Intent(mContext, OpcionesLogin::class.java))
                activity?.finishAffinity()
            }

        }.start()
    }

    private fun actualizarEstado(){
        val ref = FirebaseDatabase.getInstance().reference.child("Usuarios").child(firebaseAuth.uid!!)

        val hashMap = HashMap<String, Any>()
        hashMap["estado"] = "Offline"
        ref!!.updateChildren(hashMap)
    }

    private fun eliminarTodosMiAnuncios() {
        val miUid = firebaseAuth.uid
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios").orderByChild("uid").equalTo(miUid)
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children){
                    ds.ref.removeValue()
                }

                Toast.makeText(mContext, "Se han eliminado todos sus anuncios",Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun leerInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = "${snapshot.child("nombres").value}"
                    val email = "${snapshot.child("email").value}"
                    val imagen = "${snapshot.child("urlImagenPerfil").value}"
                    val f_nac = "${snapshot.child("fecha_nac").value}"
                    var tiempo = "${snapshot.child("tiempo").value}"
                    val telefono = "${snapshot.child("telefono").value}"
                    val codTelefono = "${snapshot.child("codigoTelefono").value}"
                    val proveedor = "${snapshot.child("proveedor").value}"

                    val cod_tel = codTelefono+telefono

                    if (tiempo == "null"){
                        tiempo = "0"
                    }

                    val for_tiempo = Constantes.obtenerFecha(tiempo.toLong())

                    //Seteamos información
                    binding.TvEmail.text = email
                    binding.TvNombres.text = nombres
                    binding.TvNacimiento.text = f_nac
                    binding.TvTelefono.text = cod_tel
                    binding.TvMiembro.text = for_tiempo

                    //Seteo de la imagen
                    try {
                        Glide.with(mContext)
                            .load(imagen)
                            .placeholder(R.drawable.img_perfil)
                            .into(binding.IvPerfil)
                    }catch (e:Exception){
                        Toast.makeText(
                            mContext,
                            "${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    if (proveedor == "Email"){
                        val esVerificado = firebaseAuth.currentUser!!.isEmailVerified
                        if (esVerificado){
                            //Si el usuario está verificado
                            binding.BtnVerificarCuenta.visibility = View.GONE
                            binding.TvEstadoCuenta.text = "Verificado"
                        }else{
                            //Si el usuario NO está verificado
                            binding.BtnVerificarCuenta.visibility = View.VISIBLE
                            binding.TvEstadoCuenta.text = "No verificado"
                        }
                    }else{
                        //Usuario ingresó a la aplicación con una cuenta de Google
                        binding.BtnVerificarCuenta.visibility = View.GONE
                        binding.TvEstadoCuenta.text = "Verificado"
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun verificarCuenta(){
        progressDialog.setMessage("Enviando instrucciones de verificación a su correo electrónico")
        progressDialog.show()

        firebaseAuth.currentUser!!.sendEmailVerification()
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(
                    mContext,
                    "Las instrucciones fueron enviadas a su correo registrado",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(
                    mContext,
                    "${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

            }

    }

}