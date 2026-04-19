package com.mh.mundihome.Modelo

class ModeloAnuncio(
    var id: String = "",
    var uid: String = "",
    var tipoInmueble: String = "",
    var direccion: String = "",
    var estado: String = "",
    var estracto: Int = 0,
    var areaConstruida: Double = 0.0,
    var areaTotal: Double = 0.0,
    var precio: Long = 0L,
    var descripcion: String = "",
    var dormitorios: Int = 0,
    var baños: Int = 0,
    var estacionamiento: Boolean = false,
    var piso: Int = 0,
    var mascotas: Boolean = false,
    var administración: Boolean = false,
    var construcción: String = "",
    var servicios: String = "",
    var estadoLegal: String = "",
    var titulo: String = "",
    var tiempo: Long = 0L,
    var latitud: Double = 0.0,
    var longitud: Double = 0.0,
    var contadorVistas: Int = 0
) {
    // Constructor vacío requerido por Firebase
    constructor() : this("", "", "", "", "", 0, 0.0, 0.0, 0L, "", 0, 0, false, 0, false, false, "", "", "", "", 0L, 0.0, 0.0, 0)

}