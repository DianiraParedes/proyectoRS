package proyecto.responsabilidad.proyecto_rs

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import proyecto.responsabilidad.proyecto_rs.databinding.ActivityRegistrarBinding

class RegistrarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarBinding
    private lateinit var autenticacion: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        autenticacion = FirebaseAuth.getInstance()

        binding.botonRegistrar.setOnClickListener {
            val correo = binding.campoCorreo.text.toString()
            val contrasena = binding.campoContrasena.text.toString()
            val confirmarContrasena = binding.campoConfirmarContrasena.text.toString()

            if (correo.isNotEmpty() && contrasena.isNotEmpty() && confirmarContrasena.isNotEmpty()) {
                if (contrasena == confirmarContrasena) {
                    if (contrasena.length >= 6) {
                        autenticacion.createUserWithEmailAndPassword(correo, contrasena)
                            .addOnCompleteListener { tarea ->
                                if (tarea.isSuccessful) {
                                    Toast.makeText(this, "Usuario registrado con éxito", Toast.LENGTH_LONG).show()
                                    finish()
                                } else {
                                    Toast.makeText(this, "Error: ${tarea.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_LONG).show()
            }
        }
    }
}