package com.mh.mundihome

import android.icu.text.DateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.arrayOf

object Constantes {

    fun obtenetTiempoDis() : Long{
        return System.currentTimeMillis()
    }

    const val  anuncio_disponible = "Disponible"
    const val anuncio_nodisponible = "No disponible / Vendido"

    val tipo_inmueble = arrayOf(
        "Apartamento",
        "Finca",
        "Casa",
        "Local comercial",
    )

    val ciudad = arrayOf(
        "Zipaquira"
    )

    val estrato = arrayOf(
        "N/A",
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "10"
    )

    val dormitorios = arrayOf(
        "N/A",
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "10"
    )

    val banos = arrayOf(
        "N/A",
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "10"
    )

    val estacionamiento = arrayOf(
        "N/A",
        "Si",
        "No"
    )

    val marcotas = arrayOf(
        "N/A",
        "Si",
        "No"
    )

    val administracion = arrayOf(
        "N/A",
        "Si",
        "No"
    )

    fun obtenerFecha(tiempo : Long) : String{
        val calendario = Calendar.getInstance(Locale.ENGLISH)
        calendario.timeInMillis = tiempo

        return android.text.format.DateFormat.format("dd/MM/yyyy", calendario).toString()
    }

}