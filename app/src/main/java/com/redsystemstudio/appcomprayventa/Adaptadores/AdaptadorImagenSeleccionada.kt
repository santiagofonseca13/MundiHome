package com.redsystemstudio.appcomprayventa.Adaptadores

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.redsystemstudio.appcomprayventa.Modelo.ModeloImageSeleccionada
import com.redsystemstudio.appcomprayventa.R
import com.redsystemstudio.appcomprayventa.databinding.ItemImagenesSeleccionadasBinding

class AdaptadorImagenSeleccionada(
    private val context : Context,
    private val imagenesSelecArrayList : ArrayList<ModeloImageSeleccionada>,
    private val idAnuncio : String
): Adapter<AdaptadorImagenSeleccionada.HolderImagenSeleccionada>(){

    private lateinit var binding : ItemImagenesSeleccionadasBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagenSeleccionada {
        binding = ItemImagenesSeleccionadasBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderImagenSeleccionada(binding.root)
    }

    override fun getItemCount(): Int {
        return imagenesSelecArrayList.size
    }

    override fun onBindViewHolder(holder: HolderImagenSeleccionada, position: Int) {
        val modelo = imagenesSelecArrayList[position]

        if (modelo.deInternet){
            /*Haremos lectura de las imágenes traidas desde Firebase*/
            try {
                val imagenUrl = modelo.imagenUrl
                Glide.with(context)
                    .load(imagenUrl)
                    .placeholder(R.drawable.item_imagen)
                    .into(binding.itemImagen)
            }catch (e: Exception){

            }
        }else{
            /*Haremos lectura de las imagenes seleccionadas desde la galería o tomadas desde la cámara*/
            try {
                val imagenUri = modelo.imagenUri
                Glide.with(context)
                    .load(imagenUri)
                    .placeholder(R.drawable.item_imagen)
                    .into(holder.item_imagen)
            }catch (e:Exception){

            }
        }



        holder.btn_cerrar.setOnClickListener {
            if (modelo.deInternet){
                //Declarar las vistas del diseño
                val Btn_si : MaterialButton
                val Btn_no : MaterialButton
                val dialog = Dialog(context)

                dialog.setContentView(R.layout.cuadro_d_eliminar_imagen)

                Btn_si = dialog.findViewById(R.id.Btn_si)
                Btn_no = dialog.findViewById(R.id.Btn_no)

                Btn_si.setOnClickListener {
                    eliminarImgFirebase(modelo, holder , position)
                    dialog.dismiss()
                }
                Btn_no.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
                dialog.setCanceledOnTouchOutside(false)

            }else{
                imagenesSelecArrayList.remove(modelo)
                notifyDataSetChanged()
            }

        }
    }

    private fun eliminarImgFirebase(modelo: ModeloImageSeleccionada, holder: AdaptadorImagenSeleccionada.HolderImagenSeleccionada, position: Int) {
        val idImagen = modelo.id

        /*La imagen se eliminará en la base de datos - Realtime database*/
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Imagenes").child(idImagen)
            .removeValue()
            .addOnSuccessListener {
                try {
                    imagenesSelecArrayList.remove(modelo)
                    eliminarImgStorage(modelo) /*La imagen también se eliminará del storage*/
                    notifyItemRemoved(position)
                }catch (e:Exception){

                }
            }
            .addOnFailureListener {e->
                Toast.makeText(context, "${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarImgStorage(modelo: ModeloImageSeleccionada) {
        val rutaImagen = "Anuncios/"+modelo.id

        val ref = FirebaseStorage.getInstance().getReference(rutaImagen)
        ref.delete()
            .addOnSuccessListener {
                Toast.makeText(context, "La imagen se ha eliminado",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                Toast.makeText(context, "${e.message}",Toast.LENGTH_SHORT).show()
            }

    }

    inner class HolderImagenSeleccionada(itemView : View) : ViewHolder(itemView){
        var item_imagen = binding.itemImagen
        var btn_cerrar = binding.cerrarItem
    }


}