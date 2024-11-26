package proyecto.responsabilidad.proyecto_rs.ui

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import proyecto.responsabilidad.proyecto_rs.R

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.location.Location
import android.os.Build
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class MapsActivity : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DatabaseReference
    private val CHANNEL_ID = "marcadores_channel"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps_activity, container, false)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createChannel()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        database = FirebaseDatabase.getInstance().reference.child("marcadores")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val permisoUbicacion = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                obtenerUbicacion()
            } else {
                Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
        permisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isMyLocationEnabled = true
        cargarMarcadores()
        mMap.setOnMapClickListener { latLng ->
            mostrarDialogoDeDetalles(latLng)
        }
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacion() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val ubicacionActual = LatLng(it.latitude, it.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual, 15f))
                }
            }
    }

    private fun mostrarDialogoDeDetalles(latLng: LatLng) {
        val input = EditText(requireContext())
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Detalles del Marcador")
            .setMessage("Escribe una descripción:")
            .setView(input)
            .setPositiveButton("Aceptar") { _, _ ->
                val descripcion = input.text.toString()
                if (descripcion.isNotEmpty()) {
                    agregarMarcador(latLng, descripcion)
                } else {
                    Toast.makeText(requireContext(), "Descripción vacía", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
        dialog.show()
    }

    private fun agregarMarcador(latLng: LatLng, descripcion: String) {
        mMap.addMarker(MarkerOptions().position(latLng).title(descripcion))
        val marcadorId = database.push().key ?: return
        val marcador = Marcador(descripcion, latLng.latitude, latLng.longitude)
        database.child(marcadorId).setValue(marcador)
            .addOnCompleteListener {
                if (it.isSuccessful) {

                    Toast.makeText(requireContext(), "Marcador agregado correctamente", Toast.LENGTH_SHORT).show()
                    mostrarNotificacion(descripcion)
                } else {
                    Toast.makeText(requireContext(), "Error al agregar el marcador", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun cargarMarcadores() {
        database.get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { child ->
                val marcador = child.getValue(Marcador::class.java)
                marcador?.let {
                    val latLng = LatLng(it.latitud, it.longitud)
                    mMap.addMarker(MarkerOptions().position(latLng).title(it.descripcion))
                }
            }
        }
    }
    fun createChannel(){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            var channel = NotificationChannel(
                CHANNEL_ID,
                "MySuperChannel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "_"
            }
            val notificationManager:NotificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE)as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    private fun mostrarNotificacion(descripcion: String) {
        Log.d("MapsActivity", "Mostrar notificación: $descripcion")

        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_marker)
            .setContentTitle("Nuevo Marcador Creado")
            .setContentText("Descripción: $descripcion")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = NotificationManagerCompat.from(requireContext())
        notificationManager.notify(1, builder.build())
    }



    data class Marcador(val descripcion: String = "", val latitud: Double = 0.0, val longitud: Double = 0.0)
}
