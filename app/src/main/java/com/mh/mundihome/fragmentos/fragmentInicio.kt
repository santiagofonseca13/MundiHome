package com.mh.mundihome.fragmentos

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.mh.mundihome.Constantes
import com.mh.mundihome.R
import com.mh.mundihome.SeleccionarUbicacion
import com.mh.mundihome.databinding.ActivityCrearAnuncioBinding
import com.mh.mundihome.databinding.FragmentInicioBinding

class fragmentInicio : Fragment() {
    private lateinit var binding: FragmentInicioBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var direccion =""
    private var latitud = 0.0
    private var longitud = 0.0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.ubicacion.setOnClickListener {
            val intent = Intent(requireContext(), SeleccionarUbicacion::class.java) // Usa requireContext()
            seleccionarUbicacion_ARL.launch(intent)
        }
    }

    private val seleccionarUbicacion_ARL =
        registerForActivityResult (ActivityResultContracts.StartActivityForResult()){resultado->
            if (resultado.resultCode == Activity.RESULT_OK){
                val data = resultado.data
                if (data != null){
                    latitud = data.getDoubleExtra("latitud", 0.0)
                    longitud = data.getDoubleExtra("longitud", 0.0)
                    direccion = data.getStringExtra("direccion") ?: ""
                    binding.ubicacion.setText(direccion)
                }else{
                    Toast.makeText(requireContext(), "Cancelado", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun buscarAnuncios(){
        progressDialog.setMessage("Agregando anuncio")
        progressDialog.show()

        val tiempo = Constantes.obtenetTiempoDis()

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        val keyId = ref.push().key

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "${keyId}"
        hashMap["uid"] = "${firebaseAuth.uid}"
        hashMap["latitud"] = latitud
        hashMap["longitud"] = longitud
    }

    private fun limpiarCampos(){
        binding.ubicacion.text.clear()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

}