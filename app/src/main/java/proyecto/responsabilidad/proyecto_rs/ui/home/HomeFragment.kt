package proyecto.responsabilidad.proyecto_rs.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.zxing.Result
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeEncoder
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import proyecto.responsabilidad.proyecto_rs.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var galeriaLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Registrar para seleccionar imagen desde la galería
        galeriaLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imagenUri: Uri? = data?.data
                if (imagenUri != null) {
                    procesarImagenGaleria(imagenUri)
                }
            }
        }

        binding.botonCamara.setOnClickListener {
            if (tienePermisoCamara()) {
                iniciarEscanerQR()
            } else {
                solicitarPermisoCamara()
            }
        }

        binding.botonGaleria.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galeriaLauncher.launch(intent)
        }

        return binding.root
    }

    private fun tienePermisoCamara(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun solicitarPermisoCamara() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            PERMISO_CAMARA
        )
    }

    private fun iniciarEscanerQR() {
        IntentIntegrator.forSupportFragment(this)
            .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            .setPrompt("Escanea un código QR")
            .setCameraId(0) // Cámara trasera
            .setBeepEnabled(true)
            .setBarcodeImageEnabled(false)
            .initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                Toast.makeText(requireContext(), "Contenido del QR: ${result.contents}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Escaneo cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun procesarImagenGaleria(imagenUri: Uri) {
        try {
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imagenUri)
            val intArray = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

            val source = com.google.zxing.RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
            val binaryBitmap = com.google.zxing.BinaryBitmap(com.google.zxing.common.HybridBinarizer(source))
            val reader = com.google.zxing.MultiFormatReader()
            val result = reader.decode(binaryBitmap)
            val contenidoQR = result.text
            if (android.util.Patterns.WEB_URL.matcher(contenidoQR).matches()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(contenidoQR))
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Contenido del QR: $contenidoQR", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al procesar la imagen: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }



    companion object {
        private const val PERMISO_CAMARA = 101
    }
}