package com.mh.mundihome.DetalleVendedor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log // Importar Log
import android.widget.Toast // Importar Toast
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mh.mundihome.Adaptadores.AdaptadorAnuncio
import com.mh.mundihome.Comentarios
import com.mh.mundihome.Constantes
import com.mh.mundihome.Modelo.ModeloAnuncio
import com.mh.mundihome.R
import com.mh.mundihome.databinding.ActivityDetalleVendedorBinding

class DetalleVendedor : AppCompatActivity() {

    private lateinit var binding : ActivityDetalleVendedorBinding
    private var uidVendedor = ""

    private val TAG = "DetalleVendedor" // Tag para Logcat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleVendedorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uidVendedor = intent.getStringExtra("uidVendedor").toString()

        // Verificar si el uidVendedor es válido
        if (uidVendedor.isNullOrEmpty() || uidVendedor == "null") {
            Toast.makeText(this, "UID de vendedor no válido.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "UID de vendedor recibido es nulo o vacío: $uidVendedor")
            finish() // Cierra la actividad si el UID no es válido
            return
        }

        cargarInfoVendedor()
        cargarAnunciosVendedor()


        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.IvComentarios.setOnClickListener {
            val intent = Intent(this, Comentarios::class.java)
            intent.putExtra("uidVendedor", uidVendedor)
            startActivity(intent)
        }

    }

    private fun cargarAnunciosVendedor(){
        val anuncioArrayList : ArrayList<ModeloAnuncio> = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.orderByChild("uid").equalTo(uidVendedor)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    anuncioArrayList.clear()
                    for (ds in snapshot.children){
                        try {
                            val modeloAnuncio = ds.getValue(ModeloAnuncio::class.java)
                            if (modeloAnuncio != null) { // Asegurarse de que el modelo no sea nulo
                                anuncioArrayList.add(modeloAnuncio)
                            }
                        }catch (e:Exception){
                            Log.e(TAG, "Error al parsear anuncio: ${e.message}", e)
                        }
                    }

                    val adaptador = AdaptadorAnuncio(this@DetalleVendedor, anuncioArrayList)
                    binding.anunciosRv.adapter = adaptador

                    val contadorAnuncios = "${anuncioArrayList.size}"
                    binding.TvNumAnuncios.text = contadorAnuncios
                    Log.d(TAG, "Cargados ${anuncioArrayList.size} anuncios para el vendedor $uidVendedor")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error al cargar anuncios del vendedor $uidVendedor: ${error.message}", error.toException())
                    Toast.makeText(this@DetalleVendedor, "Error al cargar anuncios: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun cargarInfoVendedor(){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uidVendedor)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val nombres = "${snapshot.child("nombres").value}"
                        val imagen = "${snapshot.child("urlImagenPerfil").value}"
                        val tiempo_r = snapshot.child("tiempo").value as? Long ?: 0L // Manejar posible nulo

                        val f_fecha = Constantes.obtenerFecha(tiempo_r)

                        binding.TvNombres.text = nombres
                        binding.TvMiembro.text = f_fecha

                        try {
                            Glide.with(this@DetalleVendedor)
                                .load(imagen)
                                .placeholder(R.drawable.img_perfil)
                                .into(binding.IvVendedor)
                        }catch (e:Exception){
                            Log.e(TAG, "Error de Glide al cargar imagen de perfil: ${e.message}", e)
                        }
                        Log.d(TAG, "Información del vendedor $uidVendedor cargada: $nombres")
                    } else {
                        Log.w(TAG, "No se encontró información para el vendedor $uidVendedor")
                        Toast.makeText(this@DetalleVendedor, "Información del vendedor no disponible.", Toast.LENGTH_SHORT).show()
                        finish() // Finalizar si no hay datos del vendedor
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error al cargar información del vendedor $uidVendedor: ${error.message}", error.toException())
                    Toast.makeText(this@DetalleVendedor, "Error al cargar información del vendedor: ${error.message}", Toast.LENGTH_LONG).show()
                    finish() // Finalizar en caso de error
                }
            })
    }
}