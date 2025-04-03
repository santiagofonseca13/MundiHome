package com.mh.mundihome

import android.icu.text.DateFormat
import java.util.Calendar
import java.util.Locale

object Constantes {

    fun obtenetTiempoDis() : Long{
        return System.currentTimeMillis()
    }

    fun obtenerFecha(tiempo : Long) : String{
        val calendario = Calendar.getInstance(Locale.ENGLISH)
        calendario.timeInMillis = tiempo

        return android.text.format.DateFormat.format("dd/MM/yyyy", calendario).toString()
    }

}