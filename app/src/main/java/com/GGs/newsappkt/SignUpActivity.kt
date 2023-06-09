package com.GGs.newsappkt

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    lateinit var txtUsuario: EditText
    lateinit var txtEmail: EditText
    lateinit var txtContra: EditText
    lateinit var txtReContra: EditText
    lateinit var btnReg: Button
    lateinit var auth: FirebaseAuth
    lateinit var pbCargando: ProgressBar
    lateinit var txvLogInNow: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        txtUsuario = findViewById(R.id.txtUsuario)
        txtEmail = findViewById(R.id.txtEmail)
        txtContra = findViewById(R.id.txtContra)
        txtReContra = findViewById(R.id.txtReContra)
        btnReg = findViewById(R.id.btnRegistrar)
        auth = Firebase.auth
        pbCargando = findViewById(R.id.pbCargandoRegistro)
        txvLogInNow = findViewById(R.id.txvLogInNow)

        txvLogInNow.setOnClickListener(object: View.OnClickListener{
            override fun onClick(view: View) {
                val intent = Intent(applicationContext, LogInActivity::class.java)
                startActivity(intent)
                finish()
            }

        })

        btnReg.setOnClickListener(object: View.OnClickListener {
            override fun onClick(view: View) {
                pbCargando.visibility = View.VISIBLE
                val usuario = txtUsuario.text.toString()
                val email = txtEmail.text.toString()
                val contra = txtContra.text.toString()
                val reContra = txtReContra.text.toString()

                if (usuario == "" || email == "" || contra == "" || reContra == ""  ){
                    Toast.makeText(this@SignUpActivity, "Llena todos los campos", Toast.LENGTH_SHORT).show()
                    pbCargando.visibility = View.GONE
                    return
                }
                if (contra.length < 6){
                    Toast.makeText(this@SignUpActivity, "La contraseña debe tener al menos 6 dígitos", Toast.LENGTH_SHORT).show()
                    pbCargando.visibility = View.GONE
                    return
                }
                if (contra.contains("$")){
                    Toast.makeText(this@SignUpActivity, "Los caracteres $ no son validos", Toast.LENGTH_SHORT).show()
                    pbCargando.visibility = View.GONE
                    return
                }
                if (contra != reContra){
                    Toast.makeText(this@SignUpActivity, "Verifica que las contraseñas sean iguales", Toast.LENGTH_SHORT).show()
                    pbCargando.visibility = View.GONE
                    return
                }


                auth.createUserWithEmailAndPassword(email, contra)
                    .addOnCompleteListener(this@SignUpActivity) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@SignUpActivity, "Registro exitoso.",
                                Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@SignUpActivity, "Registro fallido",
                                Toast.LENGTH_SHORT).show()
                        }
                        pbCargando.visibility = View.GONE
                    }




            }

        })
    }
}