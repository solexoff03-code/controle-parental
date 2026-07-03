package cont.role

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Côté ENFANT.
 * Affiche un code à 6 chiffres à communiquer au parent, demande les permissions
 * nécessaires de façon explicite, puis démarre le service de localisation
 * (qui affichera une notification permanente tant qu'il tourne).
 */
class ChildActivity : AppCompatActivity() {

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private lateinit var childDeviceId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child)

        childDeviceId = getSharedPreferences("prefs", MODE_PRIVATE)
            .getString("deviceId", null) ?: run {
                val newId = deviceId()
                getSharedPreferences("prefs", MODE_PRIVATE).edit().putString("deviceId", newId).apply()
                newId
            }

        val codeView = findViewById<TextView>(R.id.tvCode)

        PairingRepository.generateCode(
            childDeviceId,
            onResult = { code ->
                runOnUiThread { codeView.text = "Code à donner au parent : $code" }
            },
            onError = { msg ->
                // Affiche le message d'erreur complet pour aider au debug et l'inscrire dans Logcat
                val display = "Erreur lors de la génération du code. Voir log pour détails.\n$msg"
                runOnUiThread {
                    codeView.text = display
                }
                Log.e("Pairing", msg)
            }
        )

        findViewById<Button>(R.id.btnRequestPermissions).setOnClickListener {
            requestLocationPermissions()
        }

        findViewById<Button>(R.id.btnOpenAccessibilitySettings).setOnClickListener {
            startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun requestLocationPermissions() {
        val notGranted = locationPermissions.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted) {
            ActivityCompat.requestPermissions(this, locationPermissions, 1001)
        } else {
            startTracking()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startTracking()
        }
    }

    private fun startTracking() {
        val intent = Intent(this, LocationTrackingService::class.java)
        intent.putExtra("childDeviceId", childDeviceId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
