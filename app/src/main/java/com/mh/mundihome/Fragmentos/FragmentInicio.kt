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
    private lateinit var mDatabase: DatabaseReference

    private var actualLatitud = 0.0
    private var actualLongitud = 0.0
    private var actualDireccion = ""
    private var distanciaSeleccionada: Double = 50.0

    // Variables de estado para RBAC
    private var isUbicacionEnabled = true
    private var isInicioEnabled = true

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

        mDatabase = FirebaseDatabase.getInstance().reference
        locacionSP = mContext.getSharedPreferences("LOCACION_SP", Context.MODE_PRIVATE)

        // Cargar datos de ubicación guardados
        actualLatitud = locacionSP.getFloat("ACTUAL_LATITUD", 0.0f).toDouble()
        actualLongitud = locacionSP.getFloat("ACTUAL_LONGITUD", 0.0f).toDouble()
        actualDireccion = locacionSP.getString("ACTUAL_DIRECCION", "") ?: ""
        distanciaSeleccionada = locacionSP.getFloat("DISTANCIA_PREFERIDA", 50f).toDouble()

        if (actualLatitud != 0.0 && actualLongitud != 0.0) {
            binding.TvLocacion.text = actualDireccion
        }

        // Configurar RecyclerView
        adaptadorAnuncio = AdaptadorAnuncio(mContext, anuncioArrayList)
        binding.anunciosRv.setHasFixedSize(true)
        binding.anunciosRv.adapter = adaptadorAnuncio

        // 1. Activar el sistema de control modular
        verificarModulosRBAC()

        // 2. Configurar listeners de UI
        setupScrollListener()
        setupBusqueda()
        setupBotonesFiltros()
        setupSliderDistancia()

        // Botón para seleccionar ubicación (con chequeo RBAC)
        binding.TvLocacion.setOnClickListener {
            if (isUbicacionEnabled) {
                val intent = Intent(mContext, SeleccionarUbicacion::class.java)
                seleccionarUbicacionARL.launch(intent)
            } else {
                Toast.makeText(mContext, "La selección de ubicación está deshabilitada", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Limpiar
        binding.IbLimpiar.setOnClickListener {
            restablecerTodo()
        }

        // Carga inicial
        cargarAnuncios()
    }

    private fun verificarModulosRBAC() {
        val refModulos = mDatabase.child("Configuracion").child("Modulos")
        refModulos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Control Módulo Inicio
                    isInicioEnabled = snapshot.child("inicio_enabled").getValue(Boolean::class.java) ?: true
                    binding.anunciosRv.visibility = if (isInicioEnabled) View.VISIBLE else View.GONE
                    binding.EtBuscar.isEnabled = isInicioEnabled

                    // Control Módulo Ubicación
                    isUbicacionEnabled = snapshot.child("ubicacion_enabled").getValue(Boolean::class.java) ?: true
                    binding.sliderDistancia.visibility = if (isUbicacionEnabled) View.VISIBLE else View.GONE
                    binding.tvDistanciaActual.visibility = if (isUbicacionEnabled) View.VISIBLE else View.GONE

                    if (!isInicioEnabled) {
                        Toast.makeText(mContext, "Módulo de anuncios en mantenimiento", Toast.LENGTH_LONG).show()
                    }

                    resetearYCargar()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun cargarAnuncios() {
        if (!isInicioEnabled) {
            anuncioArrayList.clear()
            adaptadorAnuncio.notifyDataSetChanged()
            return
        }

        isLoading = true
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")

        // 1. QUITAMOS el limitToFirst de Firebase.
        // Traemos los anuncios y los filtramos en la memoria del teléfono.
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempLista = ArrayList<ModeloAnuncio>()
                for (ds in snapshot.children) {
                    try {
                        val modelo = ds.getValue(ModeloAnuncio::class.java) ?: continue

                        // Solo mostramos los que pasen el filtro
                        if (validarFiltros(modelo)) {
                            tempLista.add(modelo)
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }

                anuncioArrayList.clear()

                // 2. APLICAMOS EL LÍMITE (PAGINACIÓN) AQUÍ.
                // Toma solo los primeros 14, 28, etc., de los que SÍ pasaron el filtro.
                anuncioArrayList.addAll(tempLista.take(itemLimit))

                adaptadorAnuncio.notifyDataSetChanged()
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) { isLoading = false }
        })
    }

    private fun validarFiltros(modelo: ModeloAnuncio): Boolean {
        if (isUbicacionEnabled && actualLatitud != 0.0) {
            val res = FloatArray(1)
            Location.distanceBetween(actualLatitud, actualLongitud, modelo.latitud, modelo.longitud, res)
            if ((res[0] / 1000) > distanciaSeleccionada) return false
        }

        return filtrosSeleccionados.all { (key, value) ->
            if (value == null) true else when(key) {
                // Strings (Se comparan directamente)
                "tipo_inmueble" -> modelo.tipoInmueble == value

                // Ints (Convertimos el texto del menú desplegable a Número)
                "estracto" -> modelo.estracto == (value.toIntOrNull() ?: 0)
                "dormitorios" -> modelo.dormitorios == (value.toIntOrNull() ?: 0)
                "banos" -> modelo.baños == (value.toIntOrNull() ?: 0)

                // Booleans (Comparamos si el menú decía "Sí")
                "estacionamiento" -> modelo.estacionamiento == value.equals("Sí", ignoreCase = true)
                "marcotas" -> modelo.mascotas == value.equals("Sí", ignoreCase = true)
                "administracion" -> modelo.administración == value.equals("Sí", ignoreCase = true)

                // Long (Le pasamos el número real directo de la base de datos)
                "precio" -> validarRangoPrecio(modelo.precio, value)
                else -> true
            }
        }
    }

    private fun setupBusqueda() {
        binding.EtBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(filtro: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adaptadorAnuncio.filter.filter(filtro.toString())
                } catch (e: Exception) { e.printStackTrace() }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun setupBotonesFiltros() {
        binding.btnTipoInmueble.setOnClickListener { mostrarPopupMenu(it, tipo_inmueble, "tipo_inmueble") }
        binding.btnEstrato.setOnClickListener { mostrarPopupMenu(it, estracto, "estracto") }
        binding.btnDormitorios.setOnClickListener { mostrarPopupMenu(it, dormitorios, "dormitorios") }
        binding.btnBanos.setOnClickListener { mostrarPopupMenu(it, banos, "banos") }
        binding.btnEstacionamiento.setOnClickListener { mostrarPopupMenu(it, estacionamiento, "estacionamiento") }
        binding.btnMarcotas.setOnClickListener { mostrarPopupMenu(it, marcotas, "marcotas") }
        binding.btnAdministracion.setOnClickListener { mostrarPopupMenu(it, administracion, "administracion") }
        binding.btnPrecio.setOnClickListener { mostrarPopupMenu(it, precios, "precio") }
    }

    private fun setupSliderDistancia() {
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
    }

    private fun setupScrollListener() {
        binding.anunciosRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && totalItemCount > 0 && totalItemCount <= (lastVisible + 4)) {
                    itemLimit += incremento
                    cargarAnuncios()
                }
            }
        })
    }

    private fun restablecerTodo() {
        binding.EtBuscar.setText("")
        filtrosSeleccionados.clear()
        binding.btnTipoInmueble.text = "Tipo Inmueble"
        binding.btnEstrato.text = "Estrato"
        binding.btnDormitorios.text = "Dormitorios"
        binding.btnBanos.text = "Baños"
        binding.btnEstacionamiento.text = "Estacionamiento"
        binding.btnMarcotas.text = "Mascotas"
        binding.btnAdministracion.text = "Administración"
        binding.btnPrecio.text = "Precio"
        distanciaSeleccionada = 50.0
        binding.sliderDistancia.value = 50f
        resetearYCargar()
    }

    private fun mostrarPopupMenu(v: View, opc: Array<String>, filtroKey: String) {
        val p = PopupMenu(mContext, v)
        opc.forEachIndexed { i, s -> p.menu.add(0, i, i, s) }
        p.setOnMenuItemClickListener {
            val sel = opc[it.itemId]
            (v as MaterialButton).text = if (sel == "N/A") filtroKey.replace("_"," ").replaceFirstChar { it.uppercase() } else sel
            filtrosSeleccionados[filtroKey] = if (sel == "N/A") null else sel
            resetearYCargar()
            true
        }
        p.show()
    }

    private fun validarRangoPrecio(p: Long, rango: String): Boolean {
        return when (rango) {
            "0 - 500k" -> p <= 500_000L
            "500k - 1M" -> p in 500_001L..1_000_000L
            "1M - 2M" -> p in 1_000_001L..2_000_000L
            "2M - 5M" -> p in 2_000_001L..5_000_000L
            "Más de 5M" -> p > 5_000_000L
            else -> true
        }
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