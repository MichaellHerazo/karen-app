package com.example.karenapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var nombres: EditText
    private lateinit var apellidos: EditText
    private lateinit var documento: Spinner
    private lateinit var numeroDocumento: EditText
    private lateinit var genero: RadioGroup
    private lateinit var celular: EditText
    private lateinit var correo: EditText
    private lateinit var direccion: EditText
    private lateinit var guardar: Button
    private lateinit var subirFoto: Button
    private var imagenBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nombres = findViewById(R.id.nombres)
        apellidos = findViewById(R.id.apellidos)
        documento = findViewById(R.id.documento)
        numeroDocumento = findViewById(R.id.numeroDocumento)
        genero = findViewById(R.id.genero)
        celular = findViewById(R.id.celular)
        correo = findViewById(R.id.correo)
        direccion = findViewById(R.id.direccion)
        guardar = findViewById(R.id.guardar)
        subirFoto = findViewById(R.id.subirFoto)

        val tiposDocumento = arrayOf("Cédula de Ciudadanía", "Tarjeta de Identidad", "Registro Civíl")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposDocumento)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        documento.adapter = adapter

        subirFoto.setOnClickListener {
            abrirSelectorImagen()
        }

        guardar.setOnClickListener {
            if (validarCampos()) {
                guardarDatosFirestore()
            } else {
                Toast.makeText(this, "Campos incorrectos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validarCampos(): Boolean {
        val nombresText = nombres.text.toString().trim()
        val apellidosText = apellidos.text.toString().trim()
        val numeroDocumentoText = numeroDocumento.text.toString().trim()
        val celularText = celular.text.toString().trim()
        val correoText = correo.text.toString().trim()
        val direccionText = direccion.text.toString().trim()

        return nombresText.isNotEmpty() && apellidosText.isNotEmpty() && numeroDocumentoText.isNotEmpty() &&
                celularText.isNotEmpty() && correoText.isNotEmpty() && direccionText.isNotEmpty()
    }

    private fun guardarDatosFirestore() {
        val datos = hashMapOf(
            "nombre" to nombres.text.toString(),
            "apellido" to apellidos.text.toString(),
            "cedula" to documento.selectedItem.toString(),
            "numeroCedula" to numeroDocumento.text.toString(),
            "genero" to findViewById<RadioButton>(genero.checkedRadioButtonId).text.toString(),
            "telefono" to celular.text.toString(),
            "correoElectronico" to correo.text.toString(),
            "direccion" to direccion.text.toString()
        )

        val imagenBase64 = obtenerImagenBase64(imagenBitmap!!)
        datos["foto"] = imagenBase64

        db.collection("datos")
            .add(datos)
            .addOnSuccessListener {
                Toast.makeText(this, "Datos guardados", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun abrirSelectorImagen() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, CODIGO_SELECCIONAR_IMAGEN)
    }

    private fun obtenerImagenBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODIGO_SELECCIONAR_IMAGEN && resultCode == Activity.RESULT_OK) {
            val imagenSeleccionada = data?.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imagenSeleccionada)
                val resizedImage = Bitmap.createScaledBitmap(bitmap, 200, 200, false)
                imagenBitmap = resizedImage
                Toast.makeText(this, "Foto seleccionada", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al cargar la foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val CODIGO_SELECCIONAR_IMAGEN = 100
    }
}