package com.mh.mundihome.Anuncios

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.mh.mundihome.Adaptadores.AdaptadorImagenSeleccionada
import com.mh.mundihome.Constantes
import com.mh.mundihome.MainActivity
import com.mh.mundihome.Modelo.ModeloImageSeleccionada
import com.mh.mundihome.R
import com.mh.mundihome.SeleccionarUbicacion
import com.mh.mundihome.databinding.ActivityCrearAnuncioBinding

class CrearAnuncio : AppCompatActivity() {

    private lateinit var binding : ActivityCrearAnuncioBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog : ProgressDialog

    private var imagenUri : Uri?=null

    private lateinit var imagenSelecArrayList : ArrayList<ModeloImageSeleccionada>
    private lateinit var adaptadorImagenSel : AdaptadorImagenSeleccionada

    private var Edicion = false
    private var idAnuncioEditar = ""

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

        val  adaptadorEstrato = ArrayAdapter(this, R.layout.item_estracto, Constantes.estracto)
        binding.Estracto.setAdapter(adaptadorEstrato)

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

        Edicion = intent.getBooleanExtra("Edicion", false)

        /*Identificamos de que activity estamos llegando*/
        if (Edicion){
            //True
            //LLegamos de la actividad detalle anuncio
            idAnuncioEditar = intent.getStringExtra("idAnuncio") ?: ""
            cargarDetalles()
            binding.BtnCrearAnuncio.text = "Actualizar anuncio"
        }else{
            //False
            //LLegando de la actividad Main activity
            binding.BtnCrearAnuncio.text = "Crear anuncio"
        }

        imagenSelecArrayList = ArrayList()
        cargarImagenes()

        binding.agregarImg.setOnClickListener {
            mostrarOpciones()
        }

        binding.Locacion.setOnDismissListener {
            val intent = Intent(this, SeleccionarUbicacion::class.java)
            seleccionarUbicacion_ARL.launch(intent)
        }

        binding.BtnCrearAnuncio.setOnClickListener {
            validarDatos()
        }
    }

    private fun cargarDetalles() {
        var ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncioEditar)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    /*Obtener de la BD la información del anuncio*/
                    val tipoInmueble = "${snapshot.child("tipoInmueble").value}"
                    val dormitorios = "${snapshot.child("dormitorios").value}"
                    val estado = "${snapshot.child("estado").value}"
                    val estracto = "${snapshot.child("estracto").value}"
                    val areaConstruida = "${snapshot.child("areaConstruida").value}"
                    val areaTotal = "${snapshot.child("areaTotal").value}"
                    val banos = "${snapshot.child("banos").value}"
                    val estacionamiento = "${snapshot.child("estacionamiento").value}"
                    val piso = "${snapshot.child("piso").value}"
                    val mascotas = "${snapshot.child("mascotas").value}"
                    val administracion = "${snapshot.child("administracion").value}"
                    val contruccion = "${snapshot.child("contruccion").value}"
                    val servicios = "${snapshot.child("servicios").value}"
                    val estadoLegal = "${snapshot.child("estadoLegal").value}"
                    val locacion = "${snapshot.child("direccion").value}"
                    val precio = "${snapshot.child("precio").value}"
                    val titulo ="${snapshot.child("titulo").value}"
                    val descripcion = "${snapshot.child("descripcion").value}"
                    latitud = (snapshot.child("latitud").value) as Double
                    longitud = (snapshot.child("longitud").value) as Double

                    /*Setear la información en las vistas*/
                    binding.Locacion.setText("")
                    binding.EtPrecio.setText("")
                    binding.EtTitulo.setText("")
                    binding.EtDescripcion.setText("")
                    binding.TipoInmueble.setText("")
                    binding.Estado.setText("")
                    binding.Estracto.setText("")
                    binding.AreaConstruida.setText("")
                    binding.AreaTotal.setText("")
                    binding.Dormitorios.setText("")
                    binding.BaOs.setText("")
                    binding.Estacionamiento.setText("")
                    binding.Piso.setText("")
                    binding.AceptaMascotas.setText("")
                    binding.IncluyeAdministracion.setText("")
                    binding.DetallesContruccion.setText("")
                    binding.Servicios.setText("")
                    binding.EstadoLegal.setText("")
                    binding.Locacion.setText(locacion)

                    val refImagenes = snapshot.child("Imagenes").ref
                    refImagenes.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (ds in snapshot.children){
                                val id = "${ds.child("id").value}"
                                val imagenUrl = "${ds.child("imagenUrl").value}"

                                val modeloImgSeleccionada = ModeloImageSeleccionada(id, null, imagenUrl,true)
                                imagenSelecArrayList.add(modeloImgSeleccionada)

                            }

                            cargarImagenes()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }
    private var tipoInmueble = ""
    private var ciudad = ""
    private var estado =""
    private var estracto = ""
    private var areaConstruida =""
    private var areaTotal =""
    private var dormitorios =""
    private var banos = ""
    private var estacionamiento = ""
    private var piso =""
    private var mascotas = ""
    private var administracion = ""
    private var construccion =""
    private var servicios =""
    private var estadoLegal =""
    private var direccion = ""
    private var precio = ""
    private var titulo = ""
    private var descripcion = ""
    private var latitud = 0.0
    private var longitud = 0.0
    private fun validarDatos(){
        tipoInmueble = binding.TipoInmueble.text.toString().trim()
        estado = binding.Estado.text.toString().trim()
        estracto = binding.Estracto.text.toString().trim()
        areaConstruida = binding.AreaConstruida.text.toString().trim()
        areaTotal = binding.AreaTotal.text.toString().trim()
        dormitorios = binding.Dormitorios.text.toString().trim()
        banos = binding.BaOs.text.toString().trim()
        estacionamiento = binding.Estacionamiento.text.toString().trim()
        piso = binding.Piso.text.toString().trim()
        mascotas = binding.AceptaMascotas.text.toString().trim()
        administracion = binding.IncluyeAdministracion.text.toString().trim()
        construccion = binding.AreaConstruida.text.toString().trim()
        servicios = binding.Servicios.text.toString().trim()
        estadoLegal = binding.EstadoLegal.text.toString().trim()
        direccion = binding.Locacion.text.toString().trim()
        precio = binding.EtPrecio.text.toString().trim()
        titulo = binding.EtTitulo.text.toString().trim()
        descripcion = binding.EtDescripcion.text.toString().trim()

        if (tipoInmueble.isEmpty()){
            binding.TipoInmueble.error = "Seleccione el tipo de inmueble"
            binding.TipoInmueble.requestFocus()
        }
        else if (estado.isEmpty()) {
            binding.Estado.error = "Seleccione el estado"
            binding.Estado.requestFocus()
        }
        else if (estracto.isEmpty()) {
            binding.Estracto.error = "Seleccione el estrato"
            binding.Estracto.requestFocus()
        }
        else if (areaConstruida.isEmpty()) {
            binding.AreaConstruida.error = "Ingrese el área construida"
            binding.AreaConstruida.requestFocus()
        }
        else if (areaTotal.isEmpty()) {
            binding.AreaTotal.error = "Ingrese el área total"
            binding.AreaTotal.requestFocus()
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
        else if (direccion.isEmpty()){
            binding.Locacion.error = "Ingrese una locación"
            binding.Locacion.requestFocus()
        }
        else if (precio.isEmpty()){
            binding.EtPrecio.error = "Ingrese un precio"
            binding.EtPrecio.requestFocus()
        }
        else if (titulo.isEmpty()){
            binding.EtTitulo.error = "Ingrese un títuli"
            binding.EtTitulo.requestFocus()
        }
        else if (descripcion.isEmpty()){
            binding.EtDescripcion.error = "Ingrese una descripción"
            binding.EtDescripcion.requestFocus()
        }
       else{
            if (Edicion){
                //True
                actualizarAnuncio()
            }else{
                //False
                if (imagenUri == null){
                    Toast.makeText(this,"Agregue al menos una imagen",Toast.LENGTH_SHORT).show()
                }else{
                    agregarAnuncio()
                }

            }
        }
    }

    private fun actualizarAnuncio() {
        progressDialog.setMessage("Actualizando anuncio")
        progressDialog.show()

        val hashMap = HashMap<String, Any>()

        hashMap["tipoInmueble"] = "${tipoInmueble}"
        hashMap["ciudad"] = "${ciudad}"
        hashMap["direccion"] = "${direccion}"
        hashMap["estado"] = "${Constantes.anuncio_disponible}"
        hashMap["estrato"] = "${estracto}"
        hashMap["areaContruida"] = "${areaConstruida}"
        hashMap["areaTotal"] = "${areaTotal}"
        hashMap["precio"] = "${precio}"
        hashMap["descripción"] = "${descripcion}"
        hashMap["dormitorios"] = "${dormitorios}"
        hashMap["baños"] = "${banos}"
        hashMap["estacionamiento"] = "${estacionamiento}"
        hashMap["piso"] = "${piso}"
        hashMap["mascotas"] = "${mascotas}"
        hashMap["administración"] = "${administracion}"
        hashMap["construcción"] = "${construccion}"
        hashMap["servicios"] = "${servicios}"
        hashMap["estadoLegal"] = "${estadoLegal}"
        hashMap["titulo"] = "${titulo}"
        hashMap["latitud"] = latitud
        hashMap["longitud"] = longitud

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncioEditar)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                cargarImagenesStorage(idAnuncioEditar)
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Falló la actualización debido a ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private val seleccionarUbicacion_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado->
            if (resultado.resultCode == Activity.RESULT_OK){
                val data = resultado.data
                if (data != null){
                    latitud = data.getDoubleExtra("latitud", 0.0)
                    longitud = data.getDoubleExtra("longitud", 0.0)
                    direccion = data.getStringExtra("direccion") ?: ""

                    binding.Locacion.setText(direccion)
                }
            }else{
                Toast.makeText(this, "Cancelado",Toast.LENGTH_SHORT).show()
            }
        }

    private fun agregarAnuncio() {
        progressDialog.setMessage("Agregando anuncio")
        progressDialog.show()

        val tiempo = Constantes.obtenerTiempoDis()

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        val keyId = ref.push().key

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "${keyId}"
        hashMap["uid"] = "${firebaseAuth.uid}"
        hashMap["tipoInmueble"] = "${tipoInmueble}"
        hashMap["direccion"] = "${direccion}"
        hashMap["estado"] = "${Constantes.anuncio_disponible}"
        hashMap["estracto"] = "${estracto}"
        hashMap["areaContruida"] = "${areaConstruida}"
        hashMap["areaTotal"] = "${areaTotal}"
        hashMap["precio"] = "${precio}"
        hashMap["descripción"] = "${descripcion}"
        hashMap["dormitorios"] = "${dormitorios}"
        hashMap["baños"] = "${banos}"
        hashMap["estacionamiento"] = "${estacionamiento}"
        hashMap["piso"] = "${piso}"
        hashMap["mascotas"] = "${mascotas}"
        hashMap["administración"] = "${administracion}"
        hashMap["construcción"] = "${construccion}"
        hashMap["servicios"] = "${servicios}"
        hashMap["estadoLegal"] = "${estadoLegal}"
        hashMap["titulo"] = "${titulo}"
        hashMap["estado"] = "${Constantes.anuncio_disponible}"
        hashMap["tiempo"] = tiempo
        hashMap["latitud"] = latitud
        hashMap["longitud"] = longitud
        hashMap["contadorVistas"] = 0

        ref.child(keyId!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                cargarImagenesStorage(keyId)
            }
            .addOnFailureListener {e->
                Toast.makeText(
                    this, "${e.message}",Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun cargarImagenesStorage(keyId : String) {
        for (i in imagenSelecArrayList.indices){
            val modeloImagenSel = imagenSelecArrayList[i]

            if (!modeloImagenSel.deInternet){
                val nombreImagen = modeloImagenSel.id
                val rutaNombreImagen = "Anuncios/$nombreImagen"

                val storageReference = FirebaseStorage.getInstance().getReference(rutaNombreImagen)
                storageReference.putFile(modeloImagenSel.imagenUri!!)
                    .addOnSuccessListener {taskSnaphot->
                        val uriTask = taskSnaphot.storage.downloadUrl
                        while (!uriTask.isSuccessful);
                        val urlImgCargada = uriTask.result

                        if (uriTask.isSuccessful){
                            val hashMap = HashMap<String, Any>()
                            hashMap["id"] = "${modeloImagenSel.id}"
                            hashMap["imagenUrl"] = "$urlImgCargada"

                            val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
                            ref.child(keyId).child("Imagenes")
                                .child(nombreImagen)
                                .updateChildren(hashMap)
                        }

                        if (Edicion){
                            progressDialog.dismiss()
                            val intent = Intent(this@CrearAnuncio, MainActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(this, "Se actualizó la información del anuncio", Toast.LENGTH_SHORT).show()
                            finishAffinity()
                        }else{
                            progressDialog.dismiss()
                            Toast.makeText(this, "Se publicó su anuncio", Toast.LENGTH_SHORT).show()
                            limpiarCampos()
                        }



                    }
                    .addOnFailureListener {e->
                        Toast.makeText(
                            this, "${e.message}",Toast.LENGTH_SHORT
                        ).show()
                    }
            }


        }
    }

    private fun limpiarCampos(){
        imagenSelecArrayList.clear()
        adaptadorImagenSel.notifyDataSetChanged()
        binding.Locacion.setText("")
        binding.EtPrecio.setText("")
        binding.EtTitulo.setText("")
        binding.EtDescripcion.setText("")
        binding.TipoInmueble.setText("")
        binding.Estado.setText("")
        binding.Estracto.setText("")
        binding.AreaConstruida.setText("")
        binding.AreaTotal.setText("")
        binding.Dormitorios.setText("")
        binding.BaOs.setText("")
        binding.Estacionamiento.setText("")
        binding.Piso.setText("")
        binding.AceptaMascotas.setText("")
        binding.IncluyeAdministracion.setText("")
        binding.DetallesContruccion.setText("")
        binding.Servicios.setText("")
        binding.EstadoLegal.setText("")
    }

    private fun mostrarOpciones() {
        val popupMenu = PopupMenu(this, binding.agregarImg)

        popupMenu.menu.add(Menu.NONE, 1 ,1, "Cámara")
        popupMenu.menu.add(Menu.NONE,2,2,"Galería")

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
            }else if (itemId ==2){
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
                "El permiso de almacenamiento ha sido denegado",
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

                val tiempo = "${Constantes.obtenerTiempoDis()}"
                val modeloImgSel = ModeloImageSeleccionada(
                    tiempo, imagenUri, null, false
                )
                imagenSelecArrayList.add(modeloImgSel)
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
        var todosConcedidos = true
        for (esConcedido in resultado.values){
            todosConcedidos = todosConcedidos && esConcedido
        }
        if (todosConcedidos){
            imageCamara()
        }else
        {
            Toast.makeText(
                this,
                "El permiso de la cámara o almacenamiento ha sido denegada, o ambas fueron denegadas",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun imageCamara() {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Titulo_imagen")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Descripcion_imagen")
        imagenUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUri)
        resultadoCamara_ARL.launch(intent)
    }

    private val resultadoCamara_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado->
            if (resultado.resultCode == Activity.RESULT_OK){
                val tiempo = "${Constantes.obtenerTiempoDis()}"
                val modeloImgSel = ModeloImageSeleccionada(
                    tiempo, imagenUri, null, false
                )
                imagenSelecArrayList.add(modeloImgSel)
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
        adaptadorImagenSel = AdaptadorImagenSeleccionada(this, imagenSelecArrayList, idAnuncioEditar)
        binding.RVImagenes.adapter = adaptadorImagenSel
    }
}