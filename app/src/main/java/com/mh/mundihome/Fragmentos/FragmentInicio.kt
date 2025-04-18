package com.mh.mundihome.Fragmentos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mh.mundihome.Adaptadores.AdaptadorAnuncio
import com.mh.mundihome.Constantes
import com.mh.mundihome.Constantes.tipo_inmueble
import com.mh.mundihome.Constantes.estracto
import com.mh.mundihome.Constantes.dormitorios
import com.mh.mundihome.Constantes.banos
import com.mh.mundihome.Constantes.estacionamiento
import com.mh.mundihome.Constantes.marcotas
import com.mh.mundihome.Constantes.administracion
import com.mh.mundihome.Modelo.ModeloAnuncio
import com.mh.mundihome.SeleccionarUbicacion
import com.mh.mundihome.databinding.FragmentInicioBinding
import kotlin.text.compareTo


class FragmentInicio : Fragment() {

    private lateinit var binding : FragmentInicioBinding


    private companion object{
        private const val MAX_DISTANCIA_MOSTRAR_ANUNCIO = 50
    }

    private lateinit var mContext : Context

    private lateinit var anuncioArrayList : ArrayList<ModeloAnuncio>
    private lateinit var adaptadorAnuncio : AdaptadorAnuncio
    private lateinit var locacionSP : SharedPreferences

    private var actualLatitud = 0.0
    private var actualLongitud = 0.0
    private var actualDireccion = ""

    private val filtrosSeleccionados = mutableMapOf<String, String?>()

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentInicioBinding.inflate(LayoutInflater.from(mContext),container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locacionSP = mContext.getSharedPreferences("LOCACION_SP", Context.MODE_PRIVATE)

        actualLatitud = locacionSP.getFloat("ACTUAL_LATITUD", 0.0f).toDouble()
        actualLongitud = locacionSP.getFloat("ACTUAL_LONGITUD", 0.0f).toDouble()
        actualDireccion = locacionSP.getString("ACTUAL_DIRECCION", "")!!

        if (actualLatitud != 0.0 && actualLongitud !=0.0){
            binding.TvLocacion.text = actualDireccion
        }

        cargarAnuncios()

        binding.TvLocacion.setOnClickListener {
            val intent = Intent(mContext, SeleccionarUbicacion::class.java)
            seleccionarUbicacionARL.launch(intent)
        }

        binding.EtBuscar.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(filtro: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    val consulta = filtro.toString()
                    adaptadorAnuncio.filter.filter(consulta)
                }catch (e:Exception){

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        binding.IbLimpiar.setOnClickListener {
            val consulta = binding.EtBuscar.text.toString().trim()
            if (consulta.isNotEmpty()){
                binding.EtBuscar.setText("")
                Toast.makeText(context,"Se ha limpiado el campo de búsqueda",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context,"No se ha ingresado una consulta",Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnTipoInmueble.setOnClickListener { mostrarPopupMenu(it, tipo_inmueble, "tipo_inmueble") }
        binding.btnEstrato.setOnClickListener { mostrarPopupMenu(it, estracto, "estracto") }
        binding.btnDormitorios.setOnClickListener { mostrarPopupMenu(it, dormitorios, "dormitorios") }
        binding.btnBanos.setOnClickListener { mostrarPopupMenu(it, banos, "banos") }
        binding.btnEstacionamiento.setOnClickListener { mostrarPopupMenu(it, estacionamiento, "estacionamiento") }
        binding.btnMarcotas.setOnClickListener { mostrarPopupMenu(it, marcotas, "marcotas") }
        binding.btnAdministracion.setOnClickListener { mostrarPopupMenu(it, administracion, "administracion") }

    }

    private fun mostrarPopupMenu(view: View, opciones: Array<String>, filtroKey: String) {
        val popupMenu = PopupMenu(requireContext(), view)
        for ((index, opcion) in opciones.withIndex()) {
            popupMenu.menu.add(0, index, index, opcion)
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            val selectedOption = opciones[menuItem.itemId]
            (view as? MaterialButton)?.text = "$filtroKey: $selectedOption"

            filtrosSeleccionados[filtroKey] = if (selectedOption != "N/A") selectedOption else null

            cargarAnuncios()

            true
        }
        popupMenu.show()
    }

    private val seleccionarUbicacionARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){resultado->
        if (resultado.resultCode == Activity.RESULT_OK){
            val data = resultado.data
            if (data!=null){
                actualLatitud = data.getDoubleExtra("latitud", 0.0)
                actualLongitud = data.getDoubleExtra("longitud",0.0)
                actualDireccion = data.getStringExtra("direccion").toString()

                locacionSP.edit()
                    .putFloat("ACTUAL_LATITUD", actualLatitud.toFloat())
                    .putFloat("ACTUAL_LONGITUD", actualLongitud.toFloat())
                    .putString("ACTUAL_DIRECCION", actualDireccion)
                    .apply()

                binding.TvLocacion.text = actualDireccion

                cargarAnuncios()
            }else{
                Toast.makeText(
                    context, "Cancelado",Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun cargarAnuncios(){
        anuncioArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                anuncioArrayList.clear()
                for (ds in snapshot.children){
                    try {
                        val modeloAnuncio = ds.getValue(ModeloAnuncio::class.java)
                        val distancia = calcularDistanciaKM(
                            modeloAnuncio?.latitud ?: 0.0,
                            modeloAnuncio?.longitud ?: 0.0
                        )

                        val cumpleFiltros = distancia <= MAX_DISTANCIA_MOSTRAR_ANUNCIO  &&
                                filtrosSeleccionados.all { (filtroKey, filtroValor) ->
                                    when (filtroKey) {
                                        "tipo_inmueble" -> filtroValor == null || modeloAnuncio?.tipoInmueble.toString() == filtroValor
                                        "estrato" -> filtroValor == null || modeloAnuncio?.estracto.toString() == filtroValor
                                        "dormitorios" -> filtroValor == null || modeloAnuncio?.dormitorios.toString() == filtroValor
                                        "banos" -> filtroValor == null || modeloAnuncio?.baños.toString() == filtroValor
                                        "estacionamiento" -> filtroValor == null || modeloAnuncio?.estacionamiento.toString() == filtroValor
                                        "mascotas" -> filtroValor == null || modeloAnuncio?.mascotas.toString() == filtroValor
                                        "administracion" -> filtroValor == null || modeloAnuncio?.administración.toString() == filtroValor

                                        else -> true
                                    }
                                }
                        if (cumpleFiltros) {
                            modeloAnuncio?.let { anuncioArrayList.add(it) }
                        }

                    }catch (e:Exception){

                    }
                }
                adaptadorAnuncio = AdaptadorAnuncio(mContext, anuncioArrayList)
                binding.anunciosRv.adapter = adaptadorAnuncio

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun calcularDistanciaKM(latitud : Double , longitud : Double) : Double{
        val puntoPartida = Location(LocationManager.NETWORK_PROVIDER)
        puntoPartida.latitude = actualLatitud
        puntoPartida.longitude = actualLongitud

        val puntoFinal = Location(LocationManager.NETWORK_PROVIDER)
        puntoFinal.latitude = latitud
        puntoFinal.longitude = longitud

        val distanciaMetros = puntoPartida.distanceTo(puntoFinal).toDouble()
        return distanciaMetros/1000
    }


}