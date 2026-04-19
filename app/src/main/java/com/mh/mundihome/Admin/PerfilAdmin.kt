package com.mh.mundihome.Admin

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mh.mundihome.Constantes
import com.mh.mundihome.OpcionesLogin
import com.mh.mundihome.R
import com.mh.mundihome.databinding.FragmentPerfilAdminBinding

class PerfilAdmin : Fragment() {

    private lateinit var binding: FragmentPerfilAdminBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mContext: Context
    private lateinit var progressDialog: ProgressDialog

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPerfilAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(mContext)
        progressDialog.setTitle("Cerrando sesión")
        progressDialog.setCanceledOnTouchOutside(false)

        cargarInfoAdmin()

        binding.BtnCerrarSesionAdmin.setOnClickListener {
            cerrarSesionSeguro()
        }
    }

    private fun cargarInfoAdmin() {
        val uid = firebaseAuth.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios").child(uid)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return

                val nombres = "${snapshot.child("nombres").value}"
                val email = "${snapshot.child("email").value}"
                val imagen = "${snapshot.child("urlImagenPerfil").value}"
                val telefono = "${snapshot.child("telefono").value}"
                val codTel = "${snapshot.child("codigoTelefono").value}"
                val tiempo = snapshot.child("tiempo").value as? Long ?: 0L

                binding.TvNombresAdmin.text = nombres
                binding.TvEmailAdmin.text = email
                binding.TvTelefonoAdmin.text = "$codTel $telefono"
                binding.TvMiembroAdmin.text = Constantes.obtenerFecha(tiempo)

                try {
                    Glide.with(mContext)
                        .load(imagen)
                        .placeholder(R.drawable.img_perfil)
                        .into(binding.IvPerfilAdmin)
                } catch (e: Exception) {}
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun cerrarSesionSeguro() {
        val uid = firebaseAuth.uid ?: return
        progressDialog.show()

        val ref = FirebaseDatabase.getInstance().reference.child("Usuarios").child(uid)
        val hashMap = HashMap<String, Any>()
        hashMap["estado"] = "Offline"

        ref.updateChildren(hashMap).addOnCompleteListener {
            firebaseAuth.signOut()
            if (progressDialog.isShowing) progressDialog.dismiss()

            val intent = Intent(activity, OpcionesLogin::class.java)
            startActivity(intent)
            activity?.finishAffinity()
        }
    }
}