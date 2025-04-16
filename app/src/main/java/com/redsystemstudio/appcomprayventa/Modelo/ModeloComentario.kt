package com.redsystemstudio.appcomprayventa.Modelo

class ModeloComentario {

    var id = ""
    var tiempo = ""
    var uid = ""
    var uid_vendedor = ""
    var comentario = ""

    constructor()


    constructor(id: String, tiempo: String, uid: String, uid_vendedor: String, comentario: String) {
        this.id = id
        this.tiempo = tiempo
        this.uid = uid
        this.uid_vendedor = uid_vendedor
        this.comentario = comentario
    }


}