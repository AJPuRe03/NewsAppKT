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
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class LogInActivity : AppCompatActivity() {

    lateinit var txtEmail: EditText
    lateinit var txtContra: EditText
    lateinit var btnLogIn: Button
    lateinit var btnLGQR: Button
    lateinit var auth: FirebaseAuth
    lateinit var pbCargando: ProgressBar
    lateinit var txvSignUpNow: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        txtEmail = findViewById(R.id.txtEmailLI)
        txtContra = findViewById(R.id.txtContraLI)
        btnLogIn = findViewById(R.id.btnLogIn)
        auth = Firebase.auth
        pbCargando = findViewById(R.id.pbCargandoLI)
        txvSignUpNow = findViewById(R.id.txvSignUpNow)
        btnLGQR = findViewById(R.id.btnLGQR)

        txvSignUpNow.setOnClickListener(object: View.OnClickListener{
            override fun onClick(view: View) {
                val intent = Intent(applicationContext, SignUpActivity::class.java)
                startActivity(intent)
                finish()
            }

        })
        btnLogIn.setOnClickListener(object: View.OnClickListener{
            override fun onClick(view: View) {
                pbCargando.visibility = View.VISIBLE
                val email = txtEmail.text.toString()
                val contra = txtContra.text.toString()

                if (email == "" || contra == ""){
                    Toast.makeText(this@LogInActivity, "Llena todos los campos", Toast.LENGTH_SHORT).show()
                    pbCargando.visibility = View.GONE
                    return
                }

                auth.signInWithEmailAndPassword(email, contra)
                    .addOnCompleteListener(this@LogInActivity) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(baseContext, "Inicio de Sesión exitoso",
                                Toast.LENGTH_SHORT).show()
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(baseContext, "Usuario o contraseña incorrectos",
                                Toast.LENGTH_SHORT).show()
                        }
                    }

            }

        })
        btnLGQR.setOnClickListener(object: View.OnClickListener {
            override fun onClick(view: View) {
                val options = ScanOptions()
                options.setPrompt("Sube el volumen para hacer flash")
                options.setBeepEnabled(true)
                options.setOrientationLocked(true)
                options.setCaptureActivity(ActCaptura::class.java)
                barLauncher.launch(options)
            }
        })

    }

    val barLauncher = registerForActivityResult(ScanContract()) { resultado ->
        if (resultado.contents == null) {
            Toast.makeText(this@LogInActivity,"QR inválido",Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        if (!resultado.contents.contains("$")) {
            Toast.makeText(this@LogInActivity,"QR inválido",Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        txtEmail.setText(resultado.contents.split("$")[0])
        txtContra.setText(resultado.contents.split("$")[1])

    }
}