package com.mh.mundihome.Modelo

class ModeloAnuncio {

    var id : String = ""
    var uid : String = ""
    var categoria : String = ""
    var tipoInmueble : String = ""
    var estracto : String = ""
    var areaContruida : String = ""
    var areaTotal : String = ""
    var precio : String = ""
    var dormitorios : String = ""
    var baños : String = ""
    var estacionamiento : String = ""
    var piso : String = ""
    var mascotas : String = ""
    var administración : String = ""
    var construcción : String = ""
    var servicios : String = ""
    var estadoLegal : String = ""
    var condicion : String = ""
    var direccion : String = ""
    var estado : String = ""
    var titulo : String = ""
    var descripcion : String = ""
    var tiempo : Long = 0
    var latitud = 0.0
    var longitud = 0.0
    var favorito = false
    var contadorVistas = 0

    constructor()
    constructor(
        precio: String,
        id: String,
        uid: String,
        categoria: String,
        tipoInmueble: String,
        estracto: String,
        areaContruida: String,
        areaTotal: String,
        dormitorios: String,
        baños: String,
        estacionamiento: String,
        piso: String,
        mascotas: String,
        administración: String,
        construcción: String,
        servicios: String,
        estadoLegal: String,
        condicion: String,
        direccion: String,
        estado: String,
        titulo: String,
        descripcion: String,
        tiempo: Long,
        latitud: Double,
        longitud: Double,
        favorito: Boolean,
        contadorVistas: Int
    ) {
        this.precio = precio
        this.id = id
        this.uid = uid
        this.categoria = categoria
        this.tipoInmueble = tipoInmueble
        this.estracto = estracto
        this.areaContruida = areaContruida
        this.areaTotal = areaTotal
        this.dormitorios = dormitorios
        this.baños = baños
        this.estacionamiento = estacionamiento
        this.piso = piso
        this.mascotas = mascotas
        this.administración = administración
        this.construcción = construcción
        this.servicios = servicios
        this.estadoLegal = estadoLegal
        this.condicion = condicion
        this.direccion = direccion
        this.estado = estado
        this.titulo = titulo
        this.descripcion = descripcion
        this.tiempo = tiempo
        this.latitud = latitud
        this.longitud = longitud
        this.favorito = favorito
        this.contadorVistas = contadorVistas
    }


}