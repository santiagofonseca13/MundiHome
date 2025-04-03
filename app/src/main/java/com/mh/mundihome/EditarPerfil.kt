package com.mh.mundihome

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mh.mundihome.databinding.ActivityEditarPerfilBinding

class EditarPerfil : AppCompatActivity() {

    private lateinit var binding: ActivityEditarPerfilBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var imageUri : Uri ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        cargarInfo()

        binding.FABCambiarImg.setOnClickListener{
            selec_imagen_de()
        }
    }

    private fun cargarInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = "${snapshot.child("nombres").value}"
                    val imagen = "${snapshot.child("urlImagenPerfil").value}"
                    val f_nac = "${snapshot.child("fecha_nac").value}"
                    val telefono = "${snapshot.child("telefono").value}"
                    val codTelefono = "${snapshot.child("codigoTelefono").value}"

                    //Setear anterior informacion
                    binding.EtNombre.setText(nombres)
                    binding.EtFNac.setText(f_nac)
                    binding.EtTelefono.setText(telefono)

                    try {
                        Glide.with(applicationContext)
                            .load(imagen)
                            .placeholder(R.drawable.login)
                            .into(binding.imgPerfil)
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@EditarPerfil,
                            "${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    try {
                        val codigo = codTelefono.replace("+", "").toInt() //+51 - 51
                        binding.selectorCod.setCountryForPhoneCode(codigo)
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@EditarPerfil,
                            "${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

    }

    private fun selec_imagen_de() {
        val popupMenu = PopupMenu(this, binding.FABCambiarImg)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Cámara")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Galería")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId
            if (itemId == 1) {
                //Cámara
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    concederPermisoCamara.launch(arrayOf(android.Manifest.permission.CAMERA))
                }else{
                    concederPermisoCamara.launch(arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ))
                }
            } else if (itemId == 2) {
                //Galería
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    imagenGaleria()
                }else{
                    concederPermisoAlmacenamiento.launch(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }
            }
            return@setOnMenuItemClickListener true

        }

    }

    private val concederPermisoCamara =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){resultado->
            var concedidoTodos = true
            for (seConcede in resultado.values){
                concedidoTodos = concedidoTodos && seConcede
            }

            if (concedidoTodos){
                imageCamara()
            }else{
                Toast.makeText(
                    this,
                    "El permiso de la cámara o almacenamiento ha sido denegado, o ambas fueron denegadas",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun imageCamara() {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE,"Titulo_imagen")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Descripcion_imagen")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
        resultadoCamara_ARL.launch(intent)

    }
    private val resultadoCamara_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado->
            if (resultado.resultCode == Activity.RESULT_OK){
                try{
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.usuario)
                        .into(binding.imgPerfil)
                }catch (e: Exception){

                }
            }else{
                Toast.makeText(
                    this,
                    "Cancelado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    private val concederPermisoAlmacenamiento =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permisos ->
            val esConcedido = permisos.all { it.value } // Verifica si todos los permisos son "true"

            if (esConcedido) {
                imagenGaleria()
            } else {
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
                imageUri = data!!.data

                try{
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.usuario)
                        .into(binding.imgPerfil)
                }catch (e: Exception){

                }
            }else{
                Toast.makeText(
                    this,
                    "Cancelado",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
}