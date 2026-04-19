package com.mh.mundihome.DetalleAnuncio

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
import com.google.firebase.database.*
import com.mh.mundihome.Adaptadores.AdaptadorImgSlider
import com.mh.mundihome.Anuncios.CrearAnuncio
import com.mh.mundihome.Chat.ChatActivity
import com.mh.mundihome.Constantes
import com.mh.mundihome.DetalleVendedor.DetalleVendedor
import com.mh.mundihome.MainActivity
import com.mh.mundihome.Modelo.ModeloAnuncio
import com.mh.mundihome.Modelo.ModeloImgSlider
import com.mh.mundihome.R
import com.mh.mundihome.databinding.ActivityDetalleAnuncioBinding
import java.text.DecimalFormat
import java.util.HashMap

class DetalleAnuncio : AppCompatActivity() {

    private lateinit var binding : ActivityDetalleAnuncioBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private var idAnuncio = ""

    private var anuncioLatitud = 0.0
    private var anuncioLongitud = 0.0

    private var uidVendedor = ""
    private var telVendedor = ""
    private var favorito = false

    // Variables para el control de Módulos (RBAC)
    private var isChatsEnabled = true
    private var isUbicacionEnabled = true
    private var isPublicarEnabled = true

    private lateinit var imagenSliderArrayList : ArrayList<ModeloImgSlider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Estado inicial oculto por seguridad
        binding.IbEditar.visibility = View.GONE
        binding.IbEliminar.visibility = View.GONE
        binding.BtnMapa.visibility = View.GONE
        binding.BtnLlamar.visibility = View.GONE
        binding.BtnSms.visibility = View.GONE
        binding.BtnChat.visibility = View.GONE

        firebaseAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        idAnuncio = intent.getStringExtra("idAnuncio").toString()

        Constantes.incrementarVistas(idAnuncio)

        // --- INICIAR CONTROL DE MÓDULOS (RBAC) ---
        verificarModulosRBAC()

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        comprobarAnuncioFav()
        cargarInfoAnuncio()
        cargarImgAnuncio()

        // --- LISTENERS CON PROTECCIÓN RBAC ---

        binding.IbEditar.setOnClickListener {
            if (isPublicarEnabled) opcionesDialog()
            else Toast.makeText(this, "Edición deshabilitada por mantenimiento", Toast.LENGTH_SHORT).show()
        }

        binding.IbEliminar.setOnClickListener {
            if (!isPublicarEnabled) {
                Toast.makeText(this, "Acción deshabilitada por mantenimiento", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
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
            if (isUbicacionEnabled) Constantes.mapaIntent(this, anuncioLatitud, anuncioLongitud)
            else Toast.makeText(this, "El mapa está temporalmente deshabilitado", Toast.LENGTH_SHORT).show()
        }

        binding.BtnChat.setOnClickListener {
            if (isChatsEnabled) {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("uidVendedor", uidVendedor)
                startActivity(intent)
            } else {
                Toast.makeText(this, "El servicio de mensajería está en mantenimiento", Toast.LENGTH_SHORT).show()
            }
        }

        binding.IbFav.setOnClickListener {
            if (favorito) Constantes.eliminarAnuncioFav(this, idAnuncio)
            else Constantes.agregarAnuncioFav(this,idAnuncio)
        }

        binding.BtnLlamar.setOnClickListener {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
                val numTel = telVendedor
                if (numTel.isEmpty()) Toast.makeText(this@DetalleAnuncio, "El vendedor no tiene número telefónico", Toast.LENGTH_SHORT).show()
                else Constantes.llamarIntent(this, numTel)
            }else{
                permisoLlamada.launch(Manifest.permission.CALL_PHONE)
            }
        }

        binding.BtnSms.setOnClickListener {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
                val numTel = telVendedor
                if (numTel.isEmpty()) Toast.makeText(this@DetalleAnuncio, "El vendedor no tiene un número telefónico", Toast.LENGTH_SHORT).show()
                else Constantes.smsIntent(this, numTel)
            }else{
                permisoSms.launch(Manifest.permission.SEND_SMS)
            }
        }

        binding.IvInfoVendedor.setOnClickListener {
            val intent = Intent(this, DetalleVendedor::class.java)
            intent.putExtra("uidVendedor", uidVendedor)
            startActivity(intent)
        }
    }

    private fun verificarModulosRBAC() {
        val refModulos = mDatabase.child("Configuracion").child("Modulos")
        refModulos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    isChatsEnabled = snapshot.child("chats_enabled").getValue(Boolean::class.java) ?: true
                    isUbicacionEnabled = snapshot.child("ubicacion_enabled").getValue(Boolean::class.java) ?: true
                    isPublicarEnabled = snapshot.child("publicar_enabled").getValue(Boolean::class.java) ?: true
                    actualizarVisibilidadBotones()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun actualizarVisibilidadBotones() {
        if (uidVendedor.isEmpty()) return

        if (uidVendedor == firebaseAuth.uid) {
            // SOY EL DUEÑO
            binding.IbEditar.visibility = if (isPublicarEnabled) View.VISIBLE else View.GONE
            binding.IbEliminar.visibility = if (isPublicarEnabled) View.VISIBLE else View.GONE
            binding.BtnMapa.visibility = View.GONE
            binding.BtnLlamar.visibility = View.GONE
            binding.BtnSms.visibility = View.GONE
            binding.BtnChat.visibility = View.GONE
            binding.TxtDescrVendedor.visibility = View.GONE
            binding.perfilVendedor.visibility = View.GONE
        } else {
            // SOY VISITANTE
            binding.IbEditar.visibility = View.GONE
            binding.IbEliminar.visibility = View.GONE
            binding.BtnMapa.visibility = if (isUbicacionEnabled) View.VISIBLE else View.GONE
            binding.BtnChat.visibility = if (isChatsEnabled) View.VISIBLE else View.GONE
            binding.BtnLlamar.visibility = View.VISIBLE
            binding.BtnSms.visibility = View.VISIBLE
            binding.TxtDescrVendedor.visibility = View.VISIBLE
            binding.perfilVendedor.visibility = View.VISIBLE
        }
    }

    private fun cargarInfoAnuncio(){
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val modeloAnuncio = snapshot.getValue(ModeloAnuncio::class.java) ?: return

                    uidVendedor = modeloAnuncio.uid
                    anuncioLatitud = modeloAnuncio.latitud
                    anuncioLongitud = modeloAnuncio.longitud

                    actualizarVisibilidadBotones()

                    // Formatear Precio (Número Real)
                    val mFormatter = DecimalFormat("###,###,###")
                    val precioFormateado = "$ ${mFormatter.format(modeloAnuncio.precio)}"
                    val formatoFecha = Constantes.obtenerFecha(modeloAnuncio.tiempo)

                    // Seteamos la información (Convirtiendo tipos si es necesario)
                    binding.TvTipoInmueble.text = modeloAnuncio.tipoInmueble
                    binding.TvEstad.text = modeloAnuncio.estado
                    binding.TvEstracto.text = modeloAnuncio.estracto.toString()
                    binding.TvAreaConstruida.text = modeloAnuncio.areaConstruida.toString()
                    binding.TvAreaTotal.text = modeloAnuncio.areaTotal.toString()
                    binding.TvDormitorios.text = modeloAnuncio.dormitorios.toString()
                    binding.TvBaOs.text = modeloAnuncio.baños.toString()
                    binding.TvPiso.text = modeloAnuncio.piso.toString()
                    binding.TvDetallesContruccion.text = modeloAnuncio.construcción
                    binding.TvServicios.text = modeloAnuncio.servicios
                    binding.TvEstadoLegal.text = modeloAnuncio.estadoLegal
                    binding.TvTitulo.text = modeloAnuncio.titulo
                    binding.TvDescr.text = modeloAnuncio.descripcion
                    binding.TvDireccion.text = modeloAnuncio.direccion
                    binding.TvPrecio.text = precioFormateado
                    binding.TvEstado.text = modeloAnuncio.estado
                    binding.TvFecha.text = formatoFecha
                    binding.TvVistas.text = modeloAnuncio.contadorVistas.toString()

                    // Booleanos a texto "Sí/No"
                    binding.TvEstacionamiento.text = if (modeloAnuncio.estacionamiento) "Sí" else "No"
                    binding.TvAceptaMascotas.text = if (modeloAnuncio.mascotas) "Sí" else "No"
                    binding.TvIncluyeAdministracion.text = if (modeloAnuncio.administración) "Sí" else "No"

                    if (modeloAnuncio.estado == "Disponible") binding.TvEstado.setTextColor(Color.BLUE)
                    else binding.TvEstado.setTextColor(Color.RED)

                    cargarInfoVendedor()
                } catch (e:Exception){}
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun opcionesDialog() {
        val popupMenu = PopupMenu(this, binding.IbEditar)
        popupMenu.menu.add(Menu.NONE,0,0, "Editar")
        popupMenu.menu.add(Menu.NONE,1,1, "Marcar como vendido/arrendado")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {item->
            if (!isPublicarEnabled) {
                Toast.makeText(this, "Módulo deshabilitado.", Toast.LENGTH_SHORT).show()
                return@setOnMenuItemClickListener true
            }
            val itemId = item.itemId
            if (itemId == 0){
                val intent = Intent(this, CrearAnuncio::class.java)
                intent.putExtra("Edicion", true)
                intent.putExtra("idAnuncio", idAnuncio)
                startActivity(intent)
            }else if (itemId == 1){
                dialogMarcarVendido()
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun marcarAnuncioVendido(){
        val hashMap = HashMap<String, Any>()
        hashMap["estado"] = "${Constantes.anuncio_vendido}"

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).updateChildren(hashMap)
            .addOnSuccessListener { Toast.makeText(this, "Estado actualizado", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { e-> Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
    }

    private fun dialogMarcarVendido(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.cuadro_d_marcar_vendido)
        val Btn_si : MaterialButton = dialog.findViewById(R.id.Btn_si)
        val Btn_no : MaterialButton = dialog.findViewById(R.id.Btn_no)

        Btn_si.setOnClickListener { marcarAnuncioVendido(); dialog.dismiss() }
        Btn_no.setOnClickListener { dialog.dismiss() }

        dialog.show()
        dialog.setCanceledOnTouchOutside(false)
    }

    private fun cargarInfoVendedor() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uidVendedor).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val telefono = "${snapshot.child("telefono").value}"
                val codTel = "${snapshot.child("codigoTelefono").value}"
                val nombres = "${snapshot.child("nombres").value}"
                val imagenPerfil = "${snapshot.child("urlImagenPerfil").value}"
                val tiempo_reg = snapshot.child("tiempo").value as? Long ?: 0L

                telVendedor = "$codTel$telefono"
                binding.TvNombres.text = nombres
                binding.TvMiembro.text = Constantes.obtenerFecha(tiempo_reg)

                try {
                    Glide.with(this@DetalleAnuncio)
                        .load(imagenPerfil)
                        .placeholder(R.drawable.img_perfil)
                        .into(binding.ImgPerfil)
                } catch (e:Exception){}
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun cargarImgAnuncio(){
        imagenSliderArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Imagenes").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                imagenSliderArrayList.clear()
                for (ds in snapshot.children){
                    try {
                        val modeloImgSlider = ds.getValue(ModeloImgSlider::class.java)
                        imagenSliderArrayList.add(modeloImgSlider!!)
                    } catch (e:Exception){}
                }
                val adaptadorImgSlider = AdaptadorImgSlider(this@DetalleAnuncio, imagenSliderArrayList)
                binding.imagenSliderVP.adapter = adaptadorImgSlider
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun comprobarAnuncioFav(){
        val uid = firebaseAuth.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uid).child("Favoritos").child(idAnuncio)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    favorito = snapshot.exists()
                    binding.IbFav.setImageResource(if (favorito) R.drawable.ic_anuncio_es_favorito else R.drawable.ic_no_favorito)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun eliminarAnuncio(){
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Anuncio eliminado", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e-> Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show() }
    }

    private val permisoLlamada = registerForActivityResult(ActivityResultContracts.RequestPermission()){ conceder->
        if (conceder && telVendedor.isNotEmpty()) Constantes.llamarIntent(this, telVendedor)
    }

    private val permisoSms = registerForActivityResult(ActivityResultContracts.RequestPermission()){ conceder->
        if (conceder && telVendedor.isNotEmpty()) Constantes.smsIntent(this, telVendedor)
    }
}