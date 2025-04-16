package com.redsystemstudio.appcomprayventa.DetalleAnuncio

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.redsystemstudio.appcomprayventa.Adaptadores.AdaptadorImgSlider
import com.redsystemstudio.appcomprayventa.Anuncios.CrearAnuncio
import com.redsystemstudio.appcomprayventa.Chat.ChatActivity
import com.redsystemstudio.appcomprayventa.Constantes
import com.redsystemstudio.appcomprayventa.DetalleVendedor.DetalleVendedor
import com.redsystemstudio.appcomprayventa.MainActivity
import com.redsystemstudio.appcomprayventa.Modelo.ModeloAnuncio
import com.redsystemstudio.appcomprayventa.Modelo.ModeloImgSlider
import com.redsystemstudio.appcomprayventa.R
import com.redsystemstudio.appcomprayventa.databinding.ActivityDetalleAnuncioBinding
import java.util.HashMap

class DetalleAnuncio : AppCompatActivity() {

    private lateinit var binding : ActivityDetalleAnuncioBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var idAnuncio = ""

    private var anuncioLatitud = 0.0
    private var anuncioLongitud = 0.0

    private var uidVendedor = ""
    private var telVendedor = ""

    private var favorito = false

    private lateinit var imagenSliderArrayList : ArrayList<ModeloImgSlider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.IbEditar.visibility = View.GONE
        binding.IbEliminar.visibility = View.GONE
        binding.BtnMapa.visibility = View.GONE
        binding.BtnLlamar.visibility = View.GONE
        binding.BtnSms.visibility = View.GONE
        binding.BtnChat.visibility = View.GONE

        firebaseAuth = FirebaseAuth.getInstance()

        idAnuncio = intent.getStringExtra("idAnuncio").toString()

        Constantes.incrementarVistas(idAnuncio)

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        comprobarAnuncioFav()
        cargarInfoAnuncio()
        cargarImgAnuncio()

        binding.IbEditar.setOnClickListener {
            opcionesDialog()
        }

        binding.IbFav.setOnClickListener {
            if (favorito){
                //true
                Constantes.eliminarAnuncioFav(this, idAnuncio)
            }else{
                //false
                Constantes.agregarAnuncioFav(this,idAnuncio)
            }
        }

        binding.IbEliminar.setOnClickListener {
            val mAlertDialog = MaterialAlertDialogBuilder(this)
            mAlertDialog.setTitle("Eliminar anuncio")
                .setMessage("¿Estás seguro de eliminar este anuncio?")
                .setPositiveButton("Eliminar"){dialog, which->
                    eliminarAnuncio()
                }
                .setNegativeButton("Cancelar"){dialog, which->
                    dialog.dismiss()
                }.show()
        }

        binding.BtnMapa.setOnClickListener {
            Constantes.mapaIntent(this, anuncioLatitud, anuncioLongitud)
        }

        binding.BtnLlamar.setOnClickListener {
            if (ContextCompat.checkSelfPermission(applicationContext,
                android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
                val numTel = telVendedor
                if (numTel.isEmpty()){
                    Toast.makeText(this@DetalleAnuncio,
                        "El vendedor no tiene número telefónico",
                        Toast.LENGTH_SHORT).show()
                }else{
                    Constantes.llamarIntent(this, numTel)
                }
            }else{
                permisoLlamada.launch(Manifest.permission.CALL_PHONE)
            }

        }

        binding.BtnSms.setOnClickListener {
            if (ContextCompat.checkSelfPermission(applicationContext,
                android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
                val numTel = telVendedor
                if (numTel.isEmpty()){
                    Toast.makeText(this@DetalleAnuncio,
                        "El vendedor no tiene un número telefónico",
                        Toast.LENGTH_SHORT).show()
                }else{
                    Constantes.smsIntent(this, numTel)
                }
            }else{
                permisoSms.launch(android.Manifest.permission.SEND_SMS)
            }

        }

        binding.BtnChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("uidVendedor", uidVendedor)
            startActivity(intent)
        }

        binding.IvInfoVendedor.setOnClickListener {
            val intent = Intent(this, DetalleVendedor::class.java)
            intent.putExtra("uidVendedor", uidVendedor)
            Toast.makeText(this,"El uid del vendedor es ${uidVendedor}",Toast.LENGTH_SHORT).show()
            startActivity(intent)
        }

    }

    private fun opcionesDialog() {
        val popupMenu = PopupMenu(this, binding.IbEditar)

        popupMenu.menu.add(Menu.NONE,0,0, "Editar")
        popupMenu.menu.add(Menu.NONE,1,1, "Marcar como vendido")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {item->
            val itemId = item.itemId

            if (itemId == 0){
                //Editar
                val intent = Intent(this, CrearAnuncio::class.java)
                intent.putExtra("Edicion", true)
                intent.putExtra("idAnuncio", idAnuncio)
                startActivity(intent)
            }else if (itemId == 1){
                //Marcar como vendido
                dialogMarcarVendido()
            }

            return@setOnMenuItemClickListener true
        }

    }

    private fun cargarInfoAnuncio(){
        var ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val modeloAnuncio = snapshot.getValue(ModeloAnuncio::class.java)

                        uidVendedor = "${modeloAnuncio!!.uid}"
                        val titulo = modeloAnuncio.titulo
                        val descripcion = modeloAnuncio.descripcion
                        val direccion = modeloAnuncio.direccion
                        val condicion = modeloAnuncio.condicion
                        val categoria = modeloAnuncio.categoria
                        val precio = modeloAnuncio.precio
                        val estado = modeloAnuncio.estado
                        val vista = modeloAnuncio.contadorVistas
                        anuncioLatitud = modeloAnuncio.latitud
                        anuncioLongitud = modeloAnuncio.longitud
                        val tiempo = modeloAnuncio.tiempo

                        val formatoFecha = Constantes.obtenerFecha(tiempo)

                        if (uidVendedor == firebaseAuth.uid){
                            //Si el usuario que ha realizado la publicación, visualiza
                            //la información del anuncio

                            //SI tendrá disponible
                            binding.IbEditar.visibility = View.VISIBLE
                            binding.IbEliminar.visibility = View.VISIBLE

                            //No tendrá disponible
                            binding.BtnMapa.visibility = View.GONE
                            binding.BtnLlamar.visibility = View.GONE
                            binding.BtnSms.visibility = View.GONE
                            binding.BtnChat.visibility = View.GONE

                            binding.TxtDescrVendedor.visibility = View.GONE
                            binding.perfilVendedor.visibility = View.GONE
                        }else{
                            //NO Tendrá disponible
                            binding.IbEditar.visibility = View.GONE
                            binding.IbEliminar.visibility = View.GONE

                            //SI tendrá disponible
                            binding.BtnMapa.visibility = View.VISIBLE
                            binding.BtnLlamar.visibility = View.VISIBLE
                            binding.BtnSms.visibility = View.VISIBLE
                            binding.BtnChat.visibility = View.VISIBLE

                            binding.TxtDescrVendedor.visibility = View.VISIBLE
                            binding.perfilVendedor.visibility = View.VISIBLE
                        }

                        //Seteamos la información en las vistas
                        binding.TvTitulo.text = titulo
                        binding.TvDescr.text = descripcion
                        binding.TvDireccion.text = direccion
                        binding.TvCondicion.text = condicion
                        binding.TvCat.text = categoria
                        binding.TvPrecio.text = precio
                        binding.TvEstado.text = estado
                        binding.TvFecha.text = formatoFecha
                        binding.TvVistas.text = vista.toString()

                        if (estado.equals("Disponible")){
                            binding.TvEstado.setTextColor(Color.BLUE)
                        }else{
                            binding.TvEstado.setTextColor(Color.RED)
                        }

                        //Información del vendedor
                        cargarInfoVendedor()


                    }catch (e:Exception){

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

    }

    private fun marcarAnuncioVendido(){
        val hashMap = HashMap<String, Any>()
        hashMap["estado"] = "${Constantes.anuncio_vendido}"

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this,
                    "El anuncio ha sido marcado como vendido",
                    Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {e->
                Toast.makeText(this,
                    "No se marcó como vendido debido a ${e.message}",
                    Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun dialogMarcarVendido(){
        val Btn_si : MaterialButton
        val Btn_no : MaterialButton
        val dialog = Dialog(this)

        dialog.setContentView(R.layout.cuadro_d_marcar_vendido)

        Btn_si = dialog.findViewById(R.id.Btn_si)
        Btn_no = dialog.findViewById(R.id.Btn_no)

        Btn_si.setOnClickListener {
            marcarAnuncioVendido()
            dialog.dismiss()
        }
        Btn_no.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.setCanceledOnTouchOutside(false)
    }

    private fun cargarInfoVendedor() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uidVendedor)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val telefono = "${snapshot.child("telefono").value}"
                    val codTel = "${snapshot.child("codigoTelefono").value}"
                    val nombres = "${snapshot.child("nombres").value}"
                    val imagenPerfil = "${snapshot.child("urlImagenPerfil").value}"
                    val tiempo_reg = snapshot.child("tiempo").value as Long

                    val for_fecha = Constantes.obtenerFecha(tiempo_reg)

                    telVendedor = "$codTel$telefono"

                    binding.TvNombres.text = nombres
                    binding.TvMiembro.text = for_fecha

                    try {
                        Glide.with(this@DetalleAnuncio)
                            .load(imagenPerfil)
                            .placeholder(R.drawable.img_perfil)
                            .into(binding.ImgPerfil)
                    }catch (e:Exception){

                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })


    }

    private fun cargarImgAnuncio(){
        imagenSliderArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Imagenes")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    imagenSliderArrayList.clear()
                    for (ds in snapshot.children){
                        try {
                            val modeloImgSlider = ds.getValue(ModeloImgSlider::class.java)
                            imagenSliderArrayList.add(modeloImgSlider!!)
                        }catch (e:Exception){

                        }
                    }

                    val adaptadorImgSlider = AdaptadorImgSlider(this@DetalleAnuncio,imagenSliderArrayList)
                    binding.imagenSliderVP.adapter = adaptadorImgSlider

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun comprobarAnuncioFav(){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}").child("Favoritos").child(idAnuncio)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    favorito = snapshot.exists()

                    if (favorito){
                        //Favorito = true
                        binding.IbFav.setImageResource(R.drawable.ic_anuncio_es_favorito)
                    }else{
                        //Favorito = false
                        binding.IbFav.setImageResource(R.drawable.ic_no_favorito)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun eliminarAnuncio(){
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio)
            .removeValue()
            .addOnSuccessListener {
                startActivity(Intent(this@DetalleAnuncio, MainActivity::class.java))
                finishAffinity()
                Toast.makeText(
                    this,
                    "Se eliminó el anuncio con éxito",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {e->
                Toast.makeText(
                    this,
                    "${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    private val permisoLlamada =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){conceder->
            if (conceder){
                //True
                val numTel = telVendedor
                if (numTel.isEmpty()){
                    Toast.makeText(this@DetalleAnuncio,
                        "El vendedor no tiene número telefónico",
                        Toast.LENGTH_SHORT).show()
                }else{
                    Constantes.llamarIntent(this, numTel)
                }
            }else{
                Toast.makeText(this@DetalleAnuncio,
                    "El permiso de realizar llamadas telefónicas no está concedida, por favor habilítela en los ajustes del dispositivo",
                    Toast.LENGTH_SHORT).show()
            }
        }

    private val permisoSms =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){conceder->
            if (conceder){
                //true
                val numTel = telVendedor
                if (numTel.isEmpty()){
                    Toast.makeText(this@DetalleAnuncio,
                        "El vendedor no tiene un número telefónico",
                        Toast.LENGTH_SHORT).show()
                }else{
                    Constantes.smsIntent(this, numTel)
                }
            }else{
                //false
                Toast.makeText(this@DetalleAnuncio,
                    "El permiso de envío de mensajes SMS no está concedido, por favor habilítelo en los ajustes del teléfono",
                    Toast.LENGTH_SHORT).show()
            }

        }
}