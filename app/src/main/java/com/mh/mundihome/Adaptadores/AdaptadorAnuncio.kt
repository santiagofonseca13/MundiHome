package com.mh.mundihome.Adaptadores

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mh.mundihome.Constantes
import com.mh.mundihome.DetalleAnuncio.DetalleAnuncio
import com.mh.mundihome.Filtro.FiltrarAnuncio
import com.mh.mundihome.Modelo.ModeloAnuncio
import com.mh.mundihome.R
import com.mh.mundihome.databinding.ItemAnuncioNuevaVersionBinding
import java.text.DecimalFormat

class AdaptadorAnuncio : RecyclerView.Adapter<AdaptadorAnuncio.HolderAnuncio>, Filterable {

    private var context: Context
    var anuncioArrayList: ArrayList<ModeloAnuncio>
    private var firebaseAuth: FirebaseAuth
    private var filtroLista: ArrayList<ModeloAnuncio>
    private var filtro: FiltrarAnuncio? = null

    constructor(context: Context, anuncioArrayList: ArrayList<ModeloAnuncio>) {
        this.context = context
        this.anuncioArrayList = anuncioArrayList
        this.firebaseAuth = FirebaseAuth.getInstance()
        this.filtroLista = anuncioArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAnuncio {
        // CORRECCIÓN: El binding se infla de forma local para cada Holder
        val binding = ItemAnuncioNuevaVersionBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderAnuncio(binding)
    }

    override fun getItemCount(): Int {
        return anuncioArrayList.size
    }

    override fun onBindViewHolder(holder: HolderAnuncio, position: Int) {
        val modeloAnuncio = anuncioArrayList[position]

        // --- EXTRACCIÓN DE DATOS ---
        val titulo = modeloAnuncio.titulo
        val descripcion = modeloAnuncio.descripcion
        val direccion = modeloAnuncio.direccion
        val condicion = modeloAnuncio.estado // Usamos estado o condicion según tu modelo
        val precio = modeloAnuncio.precio // Ahora es Long
        val tiempo = modeloAnuncio.tiempo

        // Formatear precio para que aparezca con puntos (ej: 200.000.000)
        val mFormatter = DecimalFormat("###,###,###")
        val precioFormateado = "$ ${mFormatter.format(precio)}"

        val formatoFecha = Constantes.obtenerFecha(tiempo)

        // Cargar datos en la UI
        holder.binding.TvTitulo.text = titulo
        holder.binding.TvDescripcion.text = descripcion
        holder.binding.TvDireccion.text = direccion
        holder.binding.TvCondicion.text = condicion
        holder.binding.TvPrecio.text = precioFormateado // Se asigna el String formateado
        holder.binding.TvFecha.text = formatoFecha

        // Funciones auxiliares
        cargarPrimeraImgAnuncio(modeloAnuncio, holder)
        comprobarFavorito(modeloAnuncio, holder)

        // Color según condición (Ejemplo de personalización)
        when (condicion) {
            "Disponible" -> holder.binding.TvCondicion.setTextColor(Color.parseColor("#48C9B0"))
            "Vendido" -> holder.binding.TvCondicion.setTextColor(Color.parseColor("#E74C3C"))
            else -> holder.binding.TvCondicion.setTextColor(Color.parseColor("#5DADE2"))
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetalleAnuncio::class.java)
            intent.putExtra("idAnuncio", modeloAnuncio.id)
            context.startActivity(intent)
        }

        holder.binding.IbFav.setOnClickListener {
            // El campo favorito ahora se maneja dentro del objeto modeloAnuncio
            val uid = firebaseAuth.uid
            if (uid == null) {
                return@setOnClickListener
            }

            // Nota: Aquí solemos usar un booleano temporal.
            // La lógica de Firebase en comprobarFavorito actualizará el icono automáticamente.
            val esFavorito = snapshotFavoritos[modeloAnuncio.id] ?: false
            if (esFavorito) {
                Constantes.eliminarAnuncioFav(context, modeloAnuncio.id)
            } else {
                Constantes.agregarAnuncioFav(context, modeloAnuncio.id)
            }
        }
    }

    // Mapa temporal para evitar parpadeos en los favoritos (Opcional, mejora UX)
    private val snapshotFavoritos = HashMap<String, Boolean>()

    private fun comprobarFavorito(modeloAnuncio: ModeloAnuncio, holder: HolderAnuncio) {
        val uid = firebaseAuth.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")

        ref.child(uid).child("Favoritos").child(modeloAnuncio.id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        holder.binding.IbFav.setImageResource(R.drawable.ic_anuncio_es_favorito)
                    } else {
                        holder.binding.IbFav.setImageResource(R.drawable.ic_no_favorito)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun cargarPrimeraImgAnuncio(modeloAnuncio: ModeloAnuncio, holder: HolderAnuncio) {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(modeloAnuncio.id).child("Imagenes").limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener { // Cambiado aquí también
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val imagenUrl = "${ds.child("imagenUrl").value}"
                        Glide.with(context).load(imagenUrl).placeholder(R.drawable.ic_imagen).into(holder.binding.imagenIv)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    inner class HolderAnuncio(val binding: ItemAnuncioNuevaVersionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getFilter(): Filter {
        if (filtro == null) {
            filtro = FiltrarAnuncio(this, filtroLista)
        }
        return filtro as FiltrarAnuncio
    }
}