package com.redsystemstudio.appcomprayventa

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.redsystemstudio.appcomprayventa.Adaptadores.AdaptadorComentario
import com.redsystemstudio.appcomprayventa.Modelo.ModeloComentario
import com.redsystemstudio.appcomprayventa.databinding.ActivityComentariosBinding
import com.redsystemstudio.appcomprayventa.databinding.CuadroDAgregarComentarioBinding
import java.util.HashMap

class Comentarios : AppCompatActivity() {

    private lateinit var binding : ActivityComentariosBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog : ProgressDialog
    private var uidVendedor = ""

    private lateinit var comentarioArrayList : ArrayList<ModeloComentario>
    private lateinit var adaptadorComentario : AdaptadorComentario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComentariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uidVendedor = intent.getStringExtra("uidVendedor").toString()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.IbAgregarComentario.setOnClickListener {
            dialogComentar()
        }

        listarComentarios()
    }

    private fun listarComentarios() {
        comentarioArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("ComentariosVendedores")
        ref.child(uidVendedor).child("Comentarios")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    comentarioArrayList.clear()
                    for (ds in snapshot.children){
                        val modelo = ds.getValue(ModeloComentario::class.java)
                        comentarioArrayList.add(modelo!!)
                    }

                    adaptadorComentario = AdaptadorComentario(this@Comentarios, comentarioArrayList)
                    binding.RvComentarios.adapter = adaptadorComentario
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private var comentario = ""
    private fun dialogComentar() {
        val agregar_com_binding = CuadroDAgregarComentarioBinding.inflate(LayoutInflater.from(this))

        val builder = AlertDialog.Builder(this)
        builder.setView(agregar_com_binding.root)

        val alertDialog = builder.create()
        alertDialog.show()
        alertDialog.setCanceledOnTouchOutside(false)


        agregar_com_binding.IbCerrar.setOnClickListener {
            alertDialog.dismiss()
        }

        agregar_com_binding.BtnComentar.setOnClickListener {
            comentario = agregar_com_binding.EtAgregarComentario.text.toString()
            if (comentario.isEmpty()){
                Toast.makeText(this,
                    "Ingrese un comentario",
                    Toast.LENGTH_SHORT).show()
            }else{
                alertDialog.dismiss()
                agregarComentario()
            }
        }


    }

    private fun agregarComentario() {
        progressDialog.setMessage("Agregando comentario")
        progressDialog.show()

        val tiempo = "${Constantes.obtenerTiempoDis()}"

        val hashMap = HashMap<String, Any> ()
        hashMap["id"] = "$tiempo"
        hashMap["tiempo"] = "$tiempo"
        hashMap["uid"] = "${firebaseAuth.uid}" //Usuario el cual está visualizando los comentarios del vendedor
        hashMap["uid_vendedor"] = uidVendedor
        hashMap["comentario"] = "${comentario}"

        val ref = FirebaseDatabase.getInstance().getReference("ComentariosVendedores")
        ref.child(uidVendedor).child("Comentarios").child(tiempo)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this,
                    "Su comentario se ha publicado",
                    Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this,
                    "${e.message}",
                    Toast.LENGTH_SHORT).show()
            }








    }
}