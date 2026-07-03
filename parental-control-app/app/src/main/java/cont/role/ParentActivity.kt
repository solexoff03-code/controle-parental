package cont.role

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Côté PARENT.
 * Permet d'entrer le code donné par l'enfant, puis affiche sa position
 * en temps réel et donne accès aux réglages de blocage d'apps.
 */
class ParentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent)

        val codeInput = findViewById<EditText>(R.id.etCode)
        val locationView = findViewById<TextView>(R.id.tvLocation)

        findViewById<Button>(R.id.btnConnect).setOnClickListener {
            val code = codeInput.text.toString().trim()
            PairingRepository.connectWithCode(
                code,
                onSuccess = { childDeviceId ->
                    getSharedPreferences("prefs", MODE_PRIVATE).edit()
                        .putString("linkedChildId", childDeviceId).apply()
                    listenToLocation(childDeviceId, locationView)
                    Toast.makeText(this, getString(R.string.toast_connected_to_child), Toast.LENGTH_SHORT).show()
                },
                onError = { msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
            )
        }

        findViewById<Button>(R.id.btnManageBlockedApps).setOnClickListener {
            val childId = getSharedPreferences("prefs", MODE_PRIVATE).getString("linkedChildId", null)
            if (childId == null) {
                Toast.makeText(this, getString(R.string.toast_connect_first), Toast.LENGTH_SHORT).show()
            } else {
                val i = Intent(this, AppBlockerSettingsActivity::class.java)
                i.putExtra("childDeviceId", childId)
                startActivity(i)
            }
        }

        getSharedPreferences("prefs", MODE_PRIVATE).getString("linkedChildId", null)?.let {
            listenToLocation(it, locationView)
        }
    }

    private fun listenToLocation(childDeviceId: String, locationView: TextView) {
        val ref = FirebaseDatabase.getInstance().reference
            .child("devices").child(childDeviceId).child("location")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lat = snapshot.child("lat").getValue(Double::class.java)
                val lng = snapshot.child("lng").getValue(Double::class.java)
                val ts = snapshot.child("timestamp").getValue(Long::class.java)
                if (lat != null && lng != null) {
                    locationView.text = this@ParentActivity.getString(R.string.location_format, lat.toString(), lng.toString(), ts?.let { java.util.Date(it).toString() } ?: "")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ParentActivity, getString(R.string.toast_read_error), Toast.LENGTH_SHORT).show()
            }
        })
    }
}
