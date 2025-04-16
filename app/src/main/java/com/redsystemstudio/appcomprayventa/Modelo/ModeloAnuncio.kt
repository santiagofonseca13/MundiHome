package com.redsystemstudio.appcomprayventa.Modelo

class ModeloAnuncio {

    var id : String = ""
    var uid : String = ""
    var marca : String = ""
    var categoria : String = ""
    var condicion : String = ""
    var direccion : String = ""
    var precio : String = ""
    var titulo : String = ""
    var descripcion : String = ""
    var estado : String = ""
    var tiempo : Long = 0
    var latitud = 0.0
    var longitud = 0.0
    var favorito = false
    var contadorVistas = 0

    constructor()
    constructor(
        id: String,
        uid: String,
        marca: String,
        categoria: String,
        condicion: String,
        direccion: String,
        precio: String,
        titulo: String,
        descripcion: String,
        estado: String,
        tiempo: Long,
        latitud: Double,
        longitud: Double,
        favorito: Boolean,
        contadorVistas: Int
    ) {
        this.id = id
        this.uid = uid
        this.marca = marca
        this.categoria = categoria
        this.condicion = condicion
        this.direccion = direccion
        this.precio = precio
        this.titulo = titulo
        this.descripcion = descripcion
        this.estado = estado
        this.tiempo = tiempo
        this.latitud = latitud
        this.longitud = longitud
        this.favorito = favorito
        this.contadorVistas = contadorVistas
    }


}