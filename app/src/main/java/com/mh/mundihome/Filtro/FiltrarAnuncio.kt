package com.mh.mundihome.Filtro

import android.widget.Filter
import com.mh.mundihome.Adaptadores.AdaptadorAnuncio
import com.mh.mundihome.Modelo.ModeloAnuncio
import java.util.Locale

class FiltrarAnuncio(
    private val adaptador: AdaptadorAnuncio,
    private val filtroLista: ArrayList<ModeloAnuncio>
) : Filter() {

    override fun performFiltering(filtro: CharSequence?): FilterResults {
        val resultados = FilterResults()

        if (!filtro.isNullOrEmpty()) {
            // Convertimos la búsqueda a mayúsculas para que no importen las minúsculas
            val consulta = filtro.toString().uppercase(Locale.getDefault()).trim()
            val listaFiltrada = ArrayList<ModeloAnuncio>()

            for (anuncio in filtroLista) {
                // CORRECCIÓN Y MEJORA:
                // Buscamos por Título, Tipo de Inmueble, Dirección o Estado
                if (anuncio.titulo.uppercase(Locale.getDefault()).contains(consulta) ||
                    anuncio.tipoInmueble.uppercase(Locale.getDefault()).contains(consulta) ||
                    anuncio.direccion.uppercase(Locale.getDefault()).contains(consulta) ||
                    anuncio.estado.uppercase(Locale.getDefault()).contains(consulta)
                ) {
                    listaFiltrada.add(anuncio)
                }
            }
            resultados.count = listaFiltrada.size
            resultados.values = listaFiltrada
        } else {
            // Si no hay texto, devolvemos la lista original completa
            resultados.count = filtroLista.size
            resultados.values = filtroLista
        }
        return resultados
    }

    override fun publishResults(filtro: CharSequence?, resultados: FilterResults) {
        // Aplicamos los cambios al adaptador
        adaptador.anuncioArrayList = resultados.values as ArrayList<ModeloAnuncio>
        adaptador.notifyDataSetChanged()
    }
}