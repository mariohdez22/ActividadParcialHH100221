package com.example.actividadparcialhh100221

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var cameraStatusText: TextView
    private lateinit var locationStatusText: TextView
    private lateinit var storageStatusText: TextView

    private lateinit var btnCamera: Button
    private lateinit var btnLocation: Button
    private lateinit var btnStorage: Button

    private lateinit var btnOpenCamera: Button
    private lateinit var btnGetLocation: Button
    private lateinit var btnAccessStorage: Button

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var imageUri: Uri
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var openDocumentLauncher: ActivityResultLauncher<Intent>
    private lateinit var manageExternalStoragePermissionLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val REQUEST_CODE_MANAGE_STORAGE = 2296
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Enlazando vistas
        cameraStatusText = findViewById(R.id.tv_camera_status)
        locationStatusText = findViewById(R.id.tv_location_status)
        storageStatusText = findViewById(R.id.tv_storage_status)

        btnCamera = findViewById(R.id.btn_camera)
        btnLocation = findViewById(R.id.btn_location)
        btnStorage = findViewById(R.id.btn_storage)

        // Enlazando los nuevos botones de acción
        btnOpenCamera = findViewById(R.id.btn_open_camera)
        btnGetLocation = findViewById(R.id.btn_get_location)
        btnAccessStorage = findViewById(R.id.btn_access_storage)

        // Solicitar permiso de Cámara
        btnCamera.setOnClickListener {
            requestPermission(Manifest.permission.CAMERA, cameraStatusText)
        }

        // Solicitar permiso de Ubicación
        btnLocation.setOnClickListener {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, locationStatusText)
        }

        // Solicitar permiso de Almacenamiento
        btnStorage.setOnClickListener {
            requestStoragePermission()
        }

        // Mostrar el estado inicial de los permisos
        updatePermissionStatus(Manifest.permission.CAMERA, cameraStatusText)
        updatePermissionStatus(Manifest.permission.ACCESS_FINE_LOCATION, locationStatusText)
        updateStoragePermissionStatus()

        //------------------------------------------------------------------------------------------

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                // La imagen ha sido guardada en imageUri
                Toast.makeText(this, "Foto guardada en la galería", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No se tomó ninguna foto", Toast.LENGTH_SHORT).show()
            }
        }

        //------------------------------------------------------------------------------------------

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //------------------------------------------------------------------------------------------

        openDocumentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    Toast.makeText(this, "Archivo seleccionado: $uri", Toast.LENGTH_LONG).show()
                }
            }
        }

        //------------------------------------------------------------------------------------------

        manageExternalStoragePermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    storageStatusText.text = "Permiso concedido"
                } else {
                    storageStatusText.text = "Permiso no concedido"
                }
                updateUIBasedOnPermissions()
            }
        }

        //------------------------------------------------------------------------------------------

        // Acción para abrir la cámara
        btnOpenCamera.setOnClickListener {
            openCamera()
        }

        // Acción para obtener la ubicación
        btnGetLocation.setOnClickListener {
            getLocation()
        }

        // Acción para acceder al almacenamiento
        btnAccessStorage.setOnClickListener {
            accessStorage()
        }

    }

    private fun updateUIBasedOnPermissions() {
        // Para la cámara
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            btnCamera.visibility = View.GONE
            btnOpenCamera.visibility = View.VISIBLE
        } else {
            btnCamera.visibility = View.VISIBLE
            btnOpenCamera.visibility = View.GONE
        }

        // Para la ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            btnLocation.visibility = View.GONE
            btnGetLocation.visibility = View.VISIBLE
        } else {
            btnLocation.visibility = View.VISIBLE
            btnGetLocation.visibility = View.GONE
        }

        // Para el almacenamiento
        if (isStoragePermissionGranted()) {
            btnStorage.visibility = View.GONE
            btnAccessStorage.visibility = View.VISIBLE
        } else {
            btnStorage.visibility = View.VISIBLE
            btnAccessStorage.visibility = View.GONE
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                storageStatusText.text = "Permiso concedido"
            } else {
                requestManageExternalStoragePermission()
            }
        } else {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, storageStatusText)
        }
    }

    private fun requestManageExternalStoragePermission() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MANAGE_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    storageStatusText.text = "Permiso concedido"
                } else {
                    storageStatusText.text = "Permiso no concedido"
                }
            }
        }
    }

    // Función para solicitar permiso
    private fun requestPermission(permission: String, statusText: TextView) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            statusText.text = "Permiso concedido"
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    // Llamada para manejar el resultado de la solicitud
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        updatePermissionStatus(Manifest.permission.CAMERA, cameraStatusText)
        updatePermissionStatus(Manifest.permission.ACCESS_FINE_LOCATION, locationStatusText)
        updateStoragePermissionStatus()
        updateUIBasedOnPermissions()
    }

    // Función para actualizar el estado del permiso en el TextView
    private fun updatePermissionStatus(permission: String, statusText: TextView) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            statusText.text = "Permiso concedido"
        } else {
            statusText.text = "Permiso no concedido"
        }
    }

    private fun updateStoragePermissionStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                storageStatusText.text = "Permiso concedido"
            } else {
                storageStatusText.text = "Permiso no concedido"
            }
        } else {
            updatePermissionStatus(Manifest.permission.WRITE_EXTERNAL_STORAGE, storageStatusText)
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun openCamera() {
        val imageFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "photo.jpg")
        imageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", imageFile)
        takePictureLauncher.launch(imageUri)
    }

    //----------------------------------------------------------------------------------------------

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    Toast.makeText(this, "Latitud: $latitude, Longitud: $longitude", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show()
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun accessStorage() {
        if (isStoragePermissionGranted()) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            openDocumentLauncher.launch(intent)
        } else {
            Toast.makeText(this, "Permiso de almacenamiento no concedido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    //----------------------------------------------------------------------------------------------

}