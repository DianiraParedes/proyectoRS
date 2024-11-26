package proyecto.responsabilidad.proyecto_rs
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import proyecto.responsabilidad.proyecto_rs.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var clienteGoogle: GoogleSignInClient
    private lateinit var autenticacion: FirebaseAuth
    private lateinit var lanzadorSignIn: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val opcionesGoogle = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        clienteGoogle = GoogleSignIn.getClient(this, opcionesGoogle)
        autenticacion = FirebaseAuth.getInstance()

        lanzadorSignIn = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val tarea = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val cuenta = tarea.getResult(Exception::class.java)!!
                autenticarConGoogle(cuenta)
            } catch (e: Exception) {
                Toast.makeText(this, "Error al iniciar sesión: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnGoogle.setOnClickListener {
            val intentoGoogle = clienteGoogle.signInIntent
            lanzadorSignIn.launch(intentoGoogle)
        }

        binding.botonCorreo.setOnClickListener {
            val correo = binding.campoCorreo.text.toString()
            val contrasena = binding.campoContrasena.text.toString()
            if (correo.isNotEmpty() && contrasena.isNotEmpty()) {
                autenticacion.signInWithEmailAndPassword(correo, contrasena)
                    .addOnCompleteListener { tarea ->
                        if (tarea.isSuccessful) {
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Error: ${tarea.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_LONG).show()
            }
        }
        binding.botonRegistrarse.setOnClickListener {
            val intent = Intent(this, RegistrarActivity::class.java)
            startActivity(intent)
        }
    }

    private fun autenticarConGoogle(cuenta: GoogleSignInAccount) {
        val credencial = GoogleAuthProvider.getCredential(cuenta.idToken, null)
        autenticacion.signInWithCredential(credencial)
            .addOnCompleteListener(this) { tarea ->
                if (tarea.isSuccessful) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Error al autenticar con Firebase: ${tarea.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
    }
}