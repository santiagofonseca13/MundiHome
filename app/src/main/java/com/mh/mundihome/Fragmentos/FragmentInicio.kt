package com.mh.mundihome.Fragmentos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.*
import com.mh.mundihome.Adaptadores.AdaptadorAnuncio
import com.mh.mundihome.Constantes.tipo_inmueble
import com.mh.mundihome.Constantes.estracto
import com.mh.mundihome.Constantes.dormitorios
import com.mh.mundihome.Constantes.banos
import com.mh.mundihome.Constantes.estacionamiento
import com.mh.mundihome.Constantes.marcotas
import com.mh.mundihome.Constantes.administracion
import com.mh.mundihome.Constantes.precios
import com.mh.mundihome.Modelo.ModeloAnuncio
import com.mh.mundihome.SeleccionarUbicacion
import com.mh.mundihome.databinding.FragmentInicioBinding
import java.util.Locale

class FragmentInicio : Fragment() {

    private lateinit var binding: FragmentInicioBinding
    private lateinit var mContext: Context
    private var anuncioArrayList = ArrayList<ModeloAnuncio>()
    private lateinit var adaptadorAnuncio: AdaptadorAnuncio
    private lateinit var locacionSP: SharedPreferences

    private var actualLatitud = 0.0
    private var actualLongitud = 0.0
    private var actualDireccion = ""
    private var distanciaSeleccionada: Double = 50.0

    private var itemLimit = 14
    private var isLoading = false
    private val incremento = 14

    private val filtrosSeleccionados = mutableMapOf<String, String?>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locacionSP = mContext.getSharedPreferences("LOCACION_SP", Context.MODE_PRIVATE)

        actualLatitud = locacionSP.getFloat("ACTUAL_LATITUD", 0.0f).toDouble()
        actualLongitud = locacionSP.getFloat("ACTUAL_LONGITUD", 0.0f).toDouble()
        actualDireccion = locacionSP.getString("ACTUAL_DIRECCION", "") ?: ""
        distanciaSeleccionada = locacionSP.getFloat("DISTANCIA_PREFERIDA", 50f).toDouble()

        if (actualLatitud != 0.0 && actualLongitud != 0.0) {
            binding.TvLocacion.text = actualDireccion
        }

        adaptadorAnuncio = AdaptadorAnuncio(mContext, anuncioArrayList)
        binding.anunciosRv.setHasFixedSize(true)
        binding.anunciosRv.adapter = adaptadorAnuncio

        setupScrollListener()
        cargarAnuncios()

        binding.EtBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(filtro: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adaptadorAnuncio.filter.filter(filtro.toString())
                } catch (e: Exception) { e.printStackTrace() }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        // CORRECCIÓN: Botón Limpiar restablece filtros y UI
        binding.IbLimpiar.setOnClickListener {
            binding.EtBuscar.setText("")
            filtrosSeleccionados.clear()

            // Restablecer textos originales
            binding.btnTipoInmueble.text = "Tipo Inmueble"
            binding.btnEstrato.text = "Estrato"
            binding.btnDormitorios.text = "Dormitorios"
            binding.btnBanos.text = "Baños"
            binding.btnEstacionamiento.text = "Estacionamiento"
            binding.btnMarcotas.text = "Mascotas"
            binding.btnAdministracion.text = "Administración"
            binding.btnPrecio.text = "Precio"

            // Restablecer Slider
            distanciaSeleccionada = 50.0
            binding.sliderDistancia.value = 50f
            binding.tvDistanciaActual.text = "Radio de búsqueda: 50 km"
            locacionSP.edit().putFloat("DISTANCIA_PREFERIDA", 50f).apply()

            Toast.makeText(mContext, "Filtros restablecidos", Toast.LENGTH_SHORT).show()
            resetearYCargar()
        }

        binding.btnTipoInmueble.setOnClickListener { mostrarPopupMenu(it, tipo_inmueble, "tipo_inmueble") }
        binding.btnEstrato.setOnClickListener { mostrarPopupMenu(it, estracto, "estracto") }
        binding.btnDormitorios.setOnClickListener { mostrarPopupMenu(it, dormitorios, "dormitorios") }
        binding.btnBanos.setOnClickListener { mostrarPopupMenu(it, banos, "banos") }
        binding.btnEstacionamiento.setOnClickListener { mostrarPopupMenu(it, estacionamiento, "estacionamiento") }
        binding.btnMarcotas.setOnClickListener { mostrarPopupMenu(it, marcotas, "marcotas") }
        binding.btnAdministracion.setOnClickListener { mostrarPopupMenu(it, administracion, "administracion") }
        binding.btnPrecio.setOnClickListener { mostrarPopupMenu(it, precios, "precio") }

        binding.sliderDistancia.apply {
            value = distanciaSeleccionada.toFloat()
            addOnChangeListener { _, value, fromUser ->
                distanciaSeleccionada = value.toDouble()
                binding.tvDistanciaActual.text = "Radio de búsqueda: ${value.toInt()} km"
                if (fromUser) {
                    locacionSP.edit().putFloat("DISTANCIA_PREFERIDA", value).apply()
                    resetearYCargar()
                }
            }
        }

        binding.TvLocacion.setOnClickListener {
            val intent = Intent(mContext, SeleccionarUbicacion::class.java)
            seleccionarUbicacionARL.launch(intent)
        }
    }

    private fun setupScrollListener() {
        binding.anunciosRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && totalItemCount <= (lastVisible + 4)) {
                    itemLimit += incremento
                    cargarAnuncios()
                }
            }
        })
    }

    private fun cargarAnuncios() {
        isLoading = true
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.limitToFirst(itemLimit).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempLista = ArrayList<ModeloAnuncio>()
                for (ds in snapshot.children) {
                    try {
                        val modelo = ds.getValue(ModeloAnuncio::class.java) ?: continue
                        if (validarFiltros(modelo)) {
                            tempLista.add(modelo)
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
                anuncioArrayList.clear()
                anuncioArrayList.addAll(tempLista)
                adaptadorAnuncio.notifyDataSetChanged()
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) { isLoading = false }
        })
    }

    private fun validarFiltros(modelo: ModeloAnuncio): Boolean {
        if (actualLatitud != 0.0) {
            val res = FloatArray(1)
            Location.distanceBetween(actualLatitud, actualLongitud, modelo.latitud, modelo.longitud, res)
            if ((res[0] / 1000) > distanciaSeleccionada) return false
        }

        return filtrosSeleccionados.all { (key, value) ->
            if (value == null) true else when(key) {
                "tipo_inmueble" -> modelo.tipoInmueble == value
                "estracto" -> modelo.estracto == value
                "dormitorios" -> modelo.dormitorios == value
                "banos" -> modelo.baños == value
                "estacionamiento" -> modelo.estacionamiento == value
                "marcotas" -> modelo.mascotas == value
                "administracion" -> modelo.administración == value
                "precio" -> {
                    val p = modelo.precio.toLongOrNull() ?: 0L
                    validarRangoPrecio(p, value)
                }
                else -> true
            }
        }
    }

    private fun validarRangoPrecio(p: Long, rango: String): Boolean {
        return when (rango) {
            "0 - 500k" -> p <= 500000
            "500k - 1M" -> p in 500001..1000000
            "1M - 2M" -> p in 1000001..2000000
            "2M - 5M" -> p in 2000001..5000000
            "Más de 5M" -> p > 5000000
            else -> true
        }
    }

    private fun mostrarPopupMenu(v: View, opc: Array<String>, filtroKey: String) {
        val p = PopupMenu(mContext, v)
        opc.forEachIndexed { i, s -> p.menu.add(0, i, i, s) }
        p.setOnMenuItemClickListener {
            val sel = opc[it.itemId]
            (v as MaterialButton).text = if (sel == "N/A") filtroKey.replace("_"," ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } else sel
            filtrosSeleccionados[filtroKey] = if (sel == "N/A") null else sel
            resetearYCargar()
            true
        }
        p.show()
    }

    private fun resetearYCargar() {
        itemLimit = 14
        cargarAnuncios()
    }

    private val seleccionarUbicacionARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            val data = resultado.data ?: return@registerForActivityResult
            actualLatitud = data.getDoubleExtra("latitud", 0.0)
            actualLongitud = data.getDoubleExtra("longitud", 0.0)
            actualDireccion = data.getStringExtra("direccion") ?: ""

            locacionSP.edit()
                .putFloat("ACTUAL_LATITUD", actualLatitud.toFloat())
                .putFloat("ACTUAL_LONGITUD", actualLongitud.toFloat())
                .putString("ACTUAL_DIRECCION", actualDireccion)
                .apply()

            binding.TvLocacion.text = actualDireccion
            resetearYCargar()
        }
    }
}