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
import com.google.firebase.database.DatabaseReference
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
    private lateinit var mDatabase: DatabaseReference
    private lateinit var progressDialog : ProgressDialog

    private var imagenUri : Uri?=null

    private lateinit var imagenSelecArrayList : ArrayList<ModeloImageSeleccionada>
    private lateinit var adaptadorImagenSel : AdaptadorImagenSeleccionada

    private var Edicion = false
    private var idAnuncioEditar = ""

    private var isPublicarEnabled = true

    private lateinit var adaptadorTipoInmueble: ArrayAdapter<String>
    private lateinit var adaptadorEstracto: ArrayAdapter<String>
    private lateinit var adaptadorDormitorios: ArrayAdapter<String>
    private lateinit var adaptadorBanos: ArrayAdapter<String>
    private lateinit var adaptadorEstacionamiento: ArrayAdapter<String>
    private lateinit var adaptadorMascotas: ArrayAdapter<String>
    private lateinit var adaptadorAdministracion: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        verificarModuloRBAC()

        adaptadorTipoInmueble = ArrayAdapter(this, R.layout.item_tipo_inmueble, Constantes.tipo_inmueble)
        adaptadorEstracto = ArrayAdapter(this, R.layout.item_estracto, Constantes.estracto)
        adaptadorDormitorios = ArrayAdapter(this, R.layout.item_dormitorios, Constantes.dormitorios)
        adaptadorBanos = ArrayAdapter(this, R.layout.item_banos, Constantes.banos)
        adaptadorEstacionamiento = ArrayAdapter(this, R.layout.item_estacionamiento, Constantes.estacionamiento)
        adaptadorMascotas = ArrayAdapter(this, R.layout.item_mascotas, Constantes.marcotas)
        adaptadorAdministracion = ArrayAdapter(this, R.layout.item_administracion, Constantes.administracion)

        binding.TipoInmueble.setAdapter(adaptadorTipoInmueble)
        binding.Estracto.setAdapter(adaptadorEstracto)
        binding.Dormitorios.setAdapter(adaptadorDormitorios)
        binding.BaOs.setAdapter(adaptadorBanos)
        binding.Estacionamiento.setAdapter(adaptadorEstacionamiento)
        binding.AceptaMascotas.setAdapter(adaptadorMascotas)
        binding.IncluyeAdministracion.setAdapter(adaptadorAdministracion)

        Edicion = intent.getBooleanExtra("Edicion", false)

        if (Edicion){
            idAnuncioEditar = intent.getStringExtra("idAnuncio") ?: ""
            cargarDetalles()
            binding.BtnCrearAnuncio.text = "Actualizar anuncio"
        }else{
            binding.BtnCrearAnuncio.text = "Crear anuncio"
        }

        imagenSelecArrayList = ArrayList()
        cargarImagenes()

        binding.agregarImg.setOnClickListener {
            if (isPublicarEnabled) mostrarOpciones()
        }

        binding.Locacion.setOnDismissListener {
            if (isPublicarEnabled) {
                val intent = Intent(this, SeleccionarUbicacion::class.java)
                seleccionarUbicacion_ARL.launch(intent)
            }
        }

        binding.BtnCrearAnuncio.setOnClickListener {
            validarDatos()
        }
    }

    private fun verificarModuloRBAC() {
        val refModulos = mDatabase.child("Configuracion").child("Modulos")
        refModulos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    isPublicarEnabled = snapshot.child("publicar_enabled").getValue(Boolean::class.java) ?: true
                    if (!isPublicarEnabled) {
                        if (progressDialog.isShowing) progressDialog.dismiss()
                        Toast.makeText(this@CrearAnuncio, "Módulo deshabilitado.", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun cargarDetalles() {
        if (!isPublicarEnabled) return
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncioEditar).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isPublicarEnabled) return

                val locacion = "${snapshot.child("direccion").value}"
                val precio = "${snapshot.child("precio").value}"
                val titulo ="${snapshot.child("titulo").value}"
                val descripcion = "${snapshot.child("descripción").value}"
                val tipoInmueble = "${snapshot.child("tipoInmueble").value}"
                val estracto = "${snapshot.child("estracto").value}"
                val areaConstruida = "${snapshot.child("areaConstruida").value}"
                val areaTotal = "${snapshot.child("areaTotal").value}"
                val dormitorios = "${snapshot.child("dormitorios").value}"
                val banos = "${snapshot.child("baños").value}"
                val estacionamiento = if (snapshot.child("estacionamiento").value == true) "Sí" else "No"
                val piso = "${snapshot.child("piso").value}"
                val mascotas = if (snapshot.child("mascotas").value == true) "Sí" else "No"
                val administracion = if (snapshot.child("administración").value == true) "Sí" else "No"
                val construccion = "${snapshot.child("construcción").value}"
                val servicios = "${snapshot.child("servicios").value}"
                val estadoLegal = "${snapshot.child("estadoLegal").value}"

                latitud = snapshot.child("latitud").getValue(Double::class.java) ?: 0.0
                longitud = snapshot.child("longitud").getValue(Double::class.java) ?: 0.0

                binding.Locacion.setText(locacion)
                binding.EtPrecio.setText(precio)
                binding.EtTitulo.setText(titulo)
                binding.EtDescripcion.setText(descripcion)
                binding.TipoInmueble.setText(tipoInmueble, false)
                binding.Estracto.setText(estracto, false)
                binding.AreaConstruida.setText(areaConstruida)
                binding.AreaTotal.setText(areaTotal)
                binding.Dormitorios.setText(dormitorios, false)
                binding.BaOs.setText(banos, false)
                binding.Estacionamiento.setText(estacionamiento, false)
                binding.Piso.setText(piso)
                binding.AceptaMascotas.setText(mascotas, false)
                binding.IncluyeAdministracion.setText(administracion, false)
                binding.DetallesContruccion.setText(construccion)
                binding.Servicios.setText(servicios)
                binding.EstadoLegal.setText(estadoLegal)

                val refImagenes = snapshot.child("Imagenes").ref
                refImagenes.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!isPublicarEnabled) return
                        for (ds in snapshot.children){
                            val id = "${ds.child("id").value}"
                            val imagenUrl = "${ds.child("imagenUrl").value}"
                            val modeloImgSeleccionada = ModeloImageSeleccionada(id, null, imagenUrl,true)
                            imagenSelecArrayList.add(modeloImgSeleccionada)
                        }
                        cargarImagenes()
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private var tipoInmueble = ""
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
        if (!isPublicarEnabled) return

        tipoInmueble = binding.TipoInmueble.text.toString().trim()
        estracto = binding.Estracto.text.toString().trim()
        areaConstruida = binding.AreaConstruida.text.toString().trim()
        areaTotal = binding.AreaTotal.text.toString().trim()
        dormitorios = binding.Dormitorios.text.toString().trim()
        banos = binding.BaOs.text.toString().trim()
        estacionamiento = binding.Estacionamiento.text.toString().trim()
        piso = binding.Piso.text.toString().trim()
        mascotas = binding.AceptaMascotas.text.toString().trim()
        administracion = binding.IncluyeAdministracion.text.toString().trim()
        construccion = binding.DetallesContruccion.text.toString().trim()
        servicios = binding.Servicios.text.toString().trim()
        estadoLegal = binding.EstadoLegal.text.toString().trim()
        direccion = binding.Locacion.text.toString().trim()
        precio = binding.EtPrecio.text.toString().trim()
        titulo = binding.EtTitulo.text.toString().trim()
        descripcion = binding.EtDescripcion.text.toString().trim()

        if (tipoInmueble.isEmpty()){
            binding.TipoInmueble.error = "Seleccione el tipo"
            binding.TipoInmueble.requestFocus()
        } else if (precio.isEmpty()){
            binding.EtPrecio.error = "Ingrese precio"
            binding.EtPrecio.requestFocus()
        } else {
            if (Edicion) actualizarAnuncio()
            else {
                if (imagenSelecArrayList.isEmpty()){
                    Toast.makeText(this,"Agregue al menos una imagen para su anuncio", Toast.LENGTH_SHORT).show()
                } else {
                    agregarAnuncio()
                }
            }
        }
    }

    private fun actualizarAnuncio() {
        if (!isPublicarEnabled) return
        progressDialog.setMessage("Actualizando...")
        progressDialog.show()

        // --- CONVERSIONES ---
        val precioNum = precio.toLongOrNull() ?: 0L
        val areaConstruidaNum = areaConstruida.toDoubleOrNull() ?: 0.0
        val areaTotalNum = areaTotal.toDoubleOrNull() ?: 0.0
        val estractoNum = estracto.toIntOrNull() ?: 0
        val dormitoriosNum = dormitorios.toIntOrNull() ?: 0
        val banosNum = banos.toIntOrNull() ?: 0
        val pisoNum = piso.toIntOrNull() ?: 0
        val aceptaMascotasBool = mascotas.equals("Sí", ignoreCase = true)
        val incluyeAdminBool = administracion.equals("Sí", ignoreCase = true)
        val estacionamientoBool = estacionamiento.equals("Sí", ignoreCase = true)

        val hashMap = HashMap<String, Any>()
        hashMap["tipoInmueble"] = tipoInmueble
        hashMap["direccion"] = direccion
        hashMap["descripción"] = descripcion
        hashMap["construcción"] = construccion
        hashMap["servicios"] = servicios
        hashMap["estadoLegal"] = estadoLegal
        hashMap["titulo"] = titulo
        hashMap["estracto"] = estractoNum
        hashMap["areaConstruida"] = areaConstruidaNum
        hashMap["areaTotal"] = areaTotalNum
        hashMap["precio"] = precioNum
        hashMap["dormitorios"] = dormitoriosNum
        hashMap["baños"] = banosNum
        hashMap["piso"] = pisoNum
        hashMap["mascotas"] = aceptaMascotasBool
        hashMap["administración"] = incluyeAdminBool
        hashMap["estacionamiento"] = estacionamientoBool
        hashMap["latitud"] = latitud
        hashMap["longitud"] = longitud

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncioEditar).updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                cargarImagenesStorage(idAnuncioEditar)
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Error: ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun agregarAnuncio() {
        if (!isPublicarEnabled) return
        progressDialog.setMessage("Agregando...")
        progressDialog.show()

        val tiempo = Constantes.obtenerTiempoDis()
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        val keyId = ref.push().key ?: ""

        // --- CONVERSIONES ---
        val precioNum = precio.toLongOrNull() ?: 0L
        val areaConstruidaNum = areaConstruida.toDoubleOrNull() ?: 0.0
        val areaTotalNum = areaTotal.toDoubleOrNull() ?: 0.0
        val estractoNum = estracto.toIntOrNull() ?: 0
        val dormitoriosNum = dormitorios.toIntOrNull() ?: 0
        val banosNum = banos.toIntOrNull() ?: 0
        val pisoNum = piso.toIntOrNull() ?: 0
        val aceptaMascotasBool = mascotas.equals("Sí", ignoreCase = true)
        val incluyeAdminBool = administracion.equals("Sí", ignoreCase = true)
        val estacionamientoBool = estacionamiento.equals("Sí", ignoreCase = true)

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = keyId
        hashMap["uid"] = "${firebaseAuth.uid}"
        hashMap["tipoInmueble"] = tipoInmueble
        hashMap["direccion"] = direccion
        hashMap["estado"] = "${Constantes.anuncio_disponible}"
        hashMap["descripción"] = descripcion
        hashMap["construcción"] = construccion
        hashMap["servicios"] = servicios
        hashMap["estadoLegal"] = estadoLegal
        hashMap["titulo"] = titulo
        hashMap["estracto"] = estractoNum
        hashMap["areaConstruida"] = areaConstruidaNum
        hashMap["areaTotal"] = areaTotalNum
        hashMap["precio"] = precioNum
        hashMap["dormitorios"] = dormitoriosNum
        hashMap["baños"] = banosNum
        hashMap["piso"] = pisoNum
        hashMap["mascotas"] = aceptaMascotasBool
        hashMap["administración"] = incluyeAdminBool
        hashMap["estacionamiento"] = estacionamientoBool
        hashMap["tiempo"] = tiempo
        hashMap["latitud"] = latitud
        hashMap["longitud"] = longitud
        hashMap["contadorVistas"] = 0

        ref.child(keyId).setValue(hashMap)
            .addOnSuccessListener { cargarImagenesStorage(keyId) }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarImagenesStorage(keyId : String) {
        progressDialog.setMessage("Subiendo imágenes...")

        val imagenesParaSubir = imagenSelecArrayList.filter { !it.deInternet }

        if (imagenesParaSubir.isEmpty()) {
            finalizarFlujo()
            return
        }

        var contadorImagenesSubidas = 0 // El guardián del flujo

        for (modelo in imagenesParaSubir) {
            val nombreImagen = modelo.id
            val storageReference = FirebaseStorage.getInstance().getReference("Anuncios/$nombreImagen")

            storageReference.putFile(modelo.imagenUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        val urlImgCargada = uri.toString()

                        val hashMap = HashMap<String, Any>()
                        hashMap["id"] = nombreImagen
                        hashMap["imagenUrl"] = urlImgCargada

                        FirebaseDatabase.getInstance().getReference("Anuncios")
                            .child(keyId).child("Imagenes").child(nombreImagen)
                            .updateChildren(hashMap)
                            .addOnSuccessListener {
                                contadorImagenesSubidas++

                                if (contadorImagenesSubidas == imagenesParaSubir.size) {
                                    finalizarFlujo()
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    // Si una falla, debemos avisar y cerrar el progress para no quedar bloqueados
                    if (progressDialog.isShowing) progressDialog.dismiss()
                    Toast.makeText(this, "Fallo al subir una imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun finalizarFlujo() {
        if (progressDialog.isShowing) progressDialog.dismiss()

        if (Edicion) {
            Toast.makeText(this, "Anuncio actualizado con éxito", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        } else {
            Toast.makeText(this, "¡Anuncio publicado con éxito!", Toast.LENGTH_SHORT).show()
            limpiarCampos()
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
            if (item.itemId == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) solicitarPermisoCamara.launch(arrayOf(android.Manifest.permission.CAMERA))
                else solicitarPermisoCamara.launch(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
            } else if (item.itemId == 2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) imagenGaleria()
                else solicitarPermisoAlmacenamiento.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            true
        }
    }

    private val solicitarPermisoAlmacenamiento = registerForActivityResult(ActivityResultContracts.RequestPermission()){ esConcedido ->
        if (esConcedido) imagenGaleria()
    }

    private fun imagenGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultadoGaleria_ARL.launch(intent)
    }

    private val resultadoGaleria_ARL = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ resultado ->
        if (resultado.resultCode == Activity.RESULT_OK){
            val uri = resultado.data?.data
            if (uri != null) {
                val tiempo = "${Constantes.obtenerTiempoDis()}"
                imagenSelecArrayList.add(ModeloImageSeleccionada(tiempo, uri, null, false))
                cargarImagenes()
            }
        }
    }

    private val solicitarPermisoCamara = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ res ->
        if (res.values.all { it }) imageCamara()
    }

    private fun imageCamara() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp")
        imagenUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUri)
        resultadoCamara_ARL.launch(intent)
    }

    private val resultadoCamara_ARL = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ res ->
        if (res.resultCode == Activity.RESULT_OK && imagenUri != null){
            val tiempo = "${Constantes.obtenerTiempoDis()}"
            imagenSelecArrayList.add(ModeloImageSeleccionada(tiempo, imagenUri, null, false))
            cargarImagenes()
        }
    }

    private fun cargarImagenes() {
        adaptadorImagenSel = AdaptadorImagenSeleccionada(this, imagenSelecArrayList, idAnuncioEditar)
        binding.RVImagenes.adapter = adaptadorImagenSel
    }

    private val seleccionarUbicacion_ARL = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ res ->
        if (res.resultCode == Activity.RESULT_OK){
            val data = res.data
            if (data != null){
                latitud = data.getDoubleExtra("latitud", 0.0)
                longitud = data.getDoubleExtra("longitud", 0.0)
                direccion = data.getStringExtra("direccion") ?: ""
                binding.Locacion.setText(direccion)
            }
        }
    }
}