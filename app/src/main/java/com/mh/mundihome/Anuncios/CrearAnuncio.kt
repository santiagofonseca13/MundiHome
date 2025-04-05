package com.mh.mundihome.Anuncios

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mh.mundihome.Adaptadores.AdaptadorImagenSeleccionada
import com.mh.mundihome.Constantes
import com.mh.mundihome.R
import com.mh.mundihome.databinding.ActivityCrearAnuncioBinding
import com.mh.mundihome.modelo.ModeloImagenSeleccionada

class CrearAnuncio : AppCompatActivity() {

    private lateinit var binding: ActivityCrearAnuncioBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var imagenUri:  Uri? = null

    private lateinit var  imagenesArrayList: ArrayList<ModeloImagenSeleccionada>
    private lateinit var  adaptadorImagenSeleccionada: AdaptadorImagenSeleccionada

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        val  adaptadorTipoInmueble = ArrayAdapter(this, R.layout.item_tipo_inmueble, Constantes.tipo_inmueble)
        binding.TipoInmueble.setAdapter(adaptadorTipoInmueble)

        val  adaptadorCiudad = ArrayAdapter(this, R.layout.item_ciudad, Constantes.ciudad)
        binding.Ciudad.setAdapter(adaptadorCiudad)

        val  adaptadorEstrato = ArrayAdapter(this, R.layout.item_estrato, Constantes.estrato)
        binding.Estrato.setAdapter(adaptadorEstrato)

        val  adaptadorDormitorios = ArrayAdapter(this, R.layout.item_dormitorios, Constantes.dormitorios)
        binding.Dormitorios.setAdapter(adaptadorDormitorios)

        val  adaptadorBaños = ArrayAdapter(this, R.layout.item_banos, Constantes.banos)
        binding.BaOs.setAdapter(adaptadorBaños)

        val  adaptadorEstacionamiento = ArrayAdapter(this, R.layout.item_estacionamiento, Constantes.estacionamiento)
        binding.Estacionamiento.setAdapter(adaptadorEstacionamiento)

        val  adaptadorMascotas = ArrayAdapter(this, R.layout.item_mascotas, Constantes.marcotas)
        binding.AceptaMascotas.setAdapter(adaptadorMascotas)

        val  adaptadorAdministracion = ArrayAdapter(this, R.layout.item_administracion, Constantes.administracion)
        binding.IncluyeAdministracion.setAdapter(adaptadorAdministracion)
    }
}