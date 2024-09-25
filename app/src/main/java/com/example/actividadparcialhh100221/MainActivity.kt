package com.example.actividadparcialhh100221

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var cameraStatusText: TextView
    private lateinit var locationStatusText: TextView
    private lateinit var storageStatusText: TextView

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

        val btnCamera = findViewById<Button>(R.id.btn_camera)
        val btnLocation = findViewById<Button>(R.id.btn_location)
        val btnStorage = findViewById<Button>(R.id.btn_storage)

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
}