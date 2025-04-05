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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
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

        binding.BtnCrearAnuncio.setOnClickListener {
            validarDatos()
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

        if (tituloAnuncio.isEmpty()){
            binding.EtAnuncio.error = "Ingrese un titulo para el anuncio"
            binding.EtAnuncio.requestFocus()
        }
        else if (tipoInmueble.isEmpty()){
            binding.TipoInmueble.error = "Seleccione el tipo de inmueble"
            binding.TipoInmueble.requestFocus()
        }
        else if (ciudad.isEmpty()) {
            binding.Ciudad.error = "Seleccione la ciudad"
            binding.Ciudad.requestFocus()
        }
        else if (estado.isEmpty()) {
            binding.Estado.error = "Seleccione el estado"
            binding.Estado.requestFocus()
        }
        else if (estracto.isEmpty()) {
            binding.Estrato.error = "Seleccione el estrato"
            binding.Estrato.requestFocus()
        }
        else if (areaConstruida.isEmpty()) {
            binding.AreaConstruida.error = "Ingrese el área construida"
            binding.AreaConstruida.requestFocus()
        }
        else if (areaTotal.isEmpty()) {
            binding.AreaTotal.error = "Ingrese el área total"
            binding.AreaTotal.requestFocus()
        }
        else if (precio.isEmpty()) {
            binding.EtPrecio.error = "Ingrese un precio"
            binding.EtPrecio.requestFocus()
        }
        else if (descripcion.isEmpty()) {
            binding.EtDescripcion.error = "Ingrese la descripción"
            binding.EtDescripcion.requestFocus()
        }
        else if (ubicacion.isEmpty()) {
            binding.Ubicacion.error = "Ingrese la ubicación"
            binding.Ubicacion.requestFocus()
        }
        else if (coordenadas.isEmpty()) {
            binding.Ubicacion.error = "Ingrese las coordenadas"
            binding.Ubicacion.requestFocus()
        }
        else if (dormitorios.isEmpty()) {
            binding.Dormitorios.error = "Seleccione la cantidad de dormitorios"
            binding.Dormitorios.requestFocus()
        }
        else if (banos.isEmpty()) {
            binding.BaOs.error = "Seleccione la cantidad de baños"
            binding.BaOs.requestFocus()
        }
        else if (estacionamiento.isEmpty()) {
            binding.Estacionamiento.error = "Seleccione una opción"
            binding.Estacionamiento.requestFocus()
        }
        else if (piso.isEmpty()) {
            binding.Piso.error = "Ingrese la cantidad de pisos"
            binding.Piso.requestFocus()
        }
        else if (mascotas.isEmpty()) {
            binding.AceptaMascotas.error = "Selecione una opción"
            binding.AceptaMascotas.requestFocus()
        }
        else if (administracion.isEmpty()) {
            binding.IncluyeAdministracion.error = "Seleccione una opción"
            binding.IncluyeAdministracion.requestFocus()
        }
        else if (construccion.isEmpty()) {
            binding.AreaConstruida.error = "Ingrese el área construida"
            binding.AreaConstruida.requestFocus()
        }
        else if (servicios.isEmpty()) {
            binding.Servicios.error = "Ingrese los servicios"
            binding.Servicios.requestFocus()
        }
        else if (estadoLegal.isEmpty()) {
            binding.EstadoLegal.error = "Seleccione una opción"
            binding.EstadoLegal.requestFocus()
        }
        else if (imagenUri == null){
            Toast.makeText(this,"Agregue al menos una imagen", Toast.LENGTH_SHORT).show()
        }else{
            agregarAnuncio()
        }

    }

    private fun agregarAnuncio() {
        progressDialog.setMessage("Agregando anuncio")
        progressDialog.show()

        val tiempo = Constantes.obtenetTiempoDis()

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        val keyId = ref.push().key

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "${keyId}"
        hashMap["uid"] = "${firebaseAuth.uid}"
        hashMap["tituloAnuncio"] = "${tituloAnuncio}"
        hashMap["tipoInmueble"] = "${tipoInmueble}"
        hashMap["ciudad"] = "${ciudad}"
        hashMap["estado"] = "${Constantes.anuncio_disponible}"
        hashMap["estrato"] = "${estracto}"
        hashMap["areaContruida"] = "${areaConstruida}"
        hashMap["areaTotal"] = "${areaTotal}"
        hashMap["precio"] = "${precio}"
        hashMap["descripción"] = "${descripcion}"
        hashMap["ubicación"] = "${ubicacion}"
        hashMap["coordenadas"] = "${coordenadas}"
        hashMap["dormitorios"] = "${dormitorios}"
        hashMap["baños"] = "${banos}"
        hashMap["estacionamiento"] = "${estacionamiento}"
        hashMap["piso"] = "${piso}"
        hashMap["mascotas"] = "${mascotas}"
        hashMap["administración"] = "${administracion}"
        hashMap["construcción"] = "${construccion}"
        hashMap["servicios"] = "${servicios}"
        hashMap["estadoLegal"] = "${estadoLegal}"
        hashMap["tiempo"] = tiempo
        hashMap["latitud"] = latitud
        hashMap["longitud"] = longitud

        ref.child(keyId!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                cargarImagenesStorage(keyId)
            }
            .addOnFailureListener {e->
                Toast.makeText(
                    this, "${e.message}", Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun cargarImagenesStorage(keyId : String) {
        for (i in imagenesArrayList.indices){
            val modeloImagenSeleccionada = imagenesArrayList[i]
            val nombreImagen = modeloImagenSeleccionada.id
            val rutaNombreImagen = "Anuncios/$nombreImagen"

            val storageReference = FirebaseStorage.getInstance().getReference(rutaNombreImagen)
            storageReference.putFile(modeloImagenSeleccionada.imagenUri!!)
                .addOnSuccessListener { taskSnaphot ->
                    val uriTask = taskSnaphot.storage.downloadUrl
                    while (!uriTask.isSuccessful);
                    val urlImgCargada = uriTask.result

                    if (uriTask.isSuccessful){
                        val hashMap = HashMap<String, Any>()
                        hashMap["id"] = "${modeloImagenSeleccionada.imagenUri}"
                        hashMap["imagenUrl"] = "$urlImgCargada"

                        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
                        ref.child(keyId).child("Imagenes")
                            .child(nombreImagen)
                            .updateChildren(hashMap)
                    }
                    progressDialog.dismiss()
                    onBackPressedDispatcher.onBackPressed()
                    Toast.makeText(this,
                        "Se publicó satisfactoriamente su anuncio",
                        Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e->
                    Toast.makeText(
                        this, "$e,message", Toast.LENGTH_SHORT)
                }
        }
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
                imagenesArrayList.add(modeloImgSel)
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
                imagenesArrayList.add(modeloImgSel)
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