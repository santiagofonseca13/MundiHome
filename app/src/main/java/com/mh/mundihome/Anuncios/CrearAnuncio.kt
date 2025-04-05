package com.mh.mundihome.Anuncios

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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

        imagenesArrayList = ArrayList()
        cargarImagenes()

        binding.agregarImg.setOnClickListener{
            mostrarOpciones()
        }
    }
    private var tituloAnuncio = ""
    private var tipoInmueble = ""
    private var ciudad = ""
    private var estado =""
    private var estracto = ""
    private var areaConstruida =""
    private var areaTotal =""
    private var precio =""
    private var descripcion =""
    private var ubicacion =""
    private var coordenadas =""
    private var dormitorios =""
    private var banos = ""
    private var estacionamiento = ""
    private var piso =""
    private var mascotas = ""
    private var administracion = ""
    private var construccion =""
    private var servicios =""
    private var estadoLegal =""
    private var latitud = 0.0
    private var longitud = 0.0
    private fun validarDatos(){
        tituloAnuncio = binding.EtAnuncio.text.toString().trim()
        tipoInmueble = binding.TipoInmueble.text.toString().trim()
        ciudad = binding.Ciudad.text.toString().trim()
        estado = binding.Estado.text.toString().trim()
        estracto = binding.Estrato.text.toString().trim()
        areaConstruida = binding.AreaConstruida.text.toString().trim()
        areaTotal = binding.AreaTotal.text.toString().trim()
        precio = binding.EtPrecio.text.toString().trim()
        descripcion = binding.EtDescripcion.text.toString().trim()
        ubicacion = binding.Ubicacion.text.toString().trim()
        coordenadas = binding.Ubicacion.text.toString().trim()
        dormitorios = binding.Dormitorios.text.toString().trim()
        banos = binding.BaOs.text.toString().trim()
        estacionamiento = binding.Estacionamiento.text.toString().trim()
        piso = binding.Piso.text.toString().trim()
        mascotas = binding.AceptaMascotas.text.toString().trim()
        administracion = binding.IncluyeAdministracion.text.toString().trim()
        construccion = binding.AreaConstruida.text.toString().trim()
        servicios = binding.Servicios.text.toString().trim()
        estadoLegal = binding.EstadoLegal.text.toString().trim()

    }

    private fun mostrarOpciones() {
        val popupMenu = PopupMenu(this, binding.agregarImg)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Cámara")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Galeria")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item->
            val itemId = item.itemId
            if (itemId == 1){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    solicitarPermisoCamara.launch(arrayOf(android.Manifest.permission.CAMERA))
                }else{
                    solicitarPermisoCamara.launch(arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ))
                }
            }else if (itemId == 2){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    imagenGaleria()
                }else{
                    solicitarPermisoAlmacenamiento.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            true
        }
    }

    private val solicitarPermisoAlmacenamiento = registerForActivityResult(
        ActivityResultContracts.RequestPermission()){esConcedido->
        if (esConcedido){
            imagenGaleria()
        }else{
            Toast.makeText(
                this,
                "El permido de almacenamiento ha sido denegado",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun imagenGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultadoGaleria_ARL.launch(intent)
    }

    private val resultadoGaleria_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado->
            if (resultado.resultCode == Activity.RESULT_OK){
                val data = resultado.data
                imagenUri = data!!.data

                val tiempo = "${Constantes.obtenetTiempoDis()}"
                val modeloImgSel = ModeloImagenSeleccionada(
                    tiempo, imagenUri, null, false
                )
                cargarImagenes()

            }else{
                Toast.makeText(
                    this,
                    "Cancelado",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }

    private val solicitarPermisoCamara = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()){ resultado->
        var  todosConcedidos = true
        for (esConcedido in resultado.values){
            todosConcedidos = todosConcedidos && esConcedido
        }
        if (todosConcedidos){
            imageCamara()
        }else
        {
            Toast.makeText(
                this,
                "El permido de la cámara o almacenamiento ha sido denegada, o ambas fueron denegadas",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun imageCamara() {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE,"Titulo_imagen")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Descripcion_imagen")
        imagenUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imagenUri)
        resultadoCamara_ARL.launch(intent)

    }

    private val resultadoCamara_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado->
            if (resultado.resultCode == Activity.RESULT_OK){
              val tiempo = "${Constantes.obtenetTiempoDis()}"
                val modeloImgSel = ModeloImagenSeleccionada(
                    tiempo, imagenUri, null, false
                )
                cargarImagenes()
            }else{
                Toast.makeText(
                    this,
                    "Cancelado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun cargarImagenes() {
        adaptadorImagenSeleccionada = AdaptadorImagenSeleccionada(this, imagenesArrayList)
        binding.RVImagenes.adapter = adaptadorImagenSeleccionada
    }
}