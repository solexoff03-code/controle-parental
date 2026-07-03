package cont.role

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase

/**
 * Envoie la position GPS toutes les ~60 secondes vers Firebase.
 *
 * TRANSPARENCE : ce service est un foreground service Android, ce qui signifie
 * qu'une notification NON masquable est affichée en permanence tant que le
 * suivi est actif. L'enfant peut désactiver le suivi à tout moment en
 * désinstallant l'app ou en révoquant la permission de localisation.
 */
class LocationTrackingService : Service() {

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var childDeviceId: String
    private val channelId = "tracking_channel"

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        childDeviceId = intent?.getStringExtra("childDeviceId") ?: "unknown"
        startForeground(1, buildNotification())
        requestLocationUpdates()
        return START_STICKY
    }

    private fun buildNotification(): android.app.Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(NotificationManager::class.java)).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(getString(R.string.notif_text))
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
    }

    private fun requestLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 60_000L)
            .build()

        fusedClient.requestLocationUpdates(request, object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                val db = FirebaseDatabase.getInstance().reference
                db.child("devices").child(childDeviceId).child("location").apply {
                    child("lat").setValue(loc.latitude)
                    child("lng").setValue(loc.longitude)
                    child("timestamp").setValue(System.currentTimeMillis())
                }
            }
        }, mainLooper)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
