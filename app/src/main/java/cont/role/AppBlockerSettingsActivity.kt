package cont.role

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

/**
 * Le parent coche les applications installées sur SON appareil pour construire
 * la liste des noms de packages à bloquer. (Simplification pédagogique : dans
 * une vraie app, il faudrait récupérer la liste des apps installées côté
 * enfant, par exemple en l'envoyant une fois vers Firebase depuis ChildActivity.)
 */
class AppBlockerSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocker_settings)

        val childDeviceId = intent.getStringExtra("childDeviceId") ?: return
        val listView = findViewById<ListView>(R.id.lvApps)

        val apps = packageManager.getInstalledApplications(0)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            .map { it.packageName }
            .sorted()

        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, apps)
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        findViewById<android.widget.Button>(R.id.btnSaveBlocked).setOnClickListener {
            val ref = FirebaseDatabase.getInstance().reference
                .child("devices").child(childDeviceId).child("blockedApps")
            ref.removeValue()
            for (i in 0 until listView.count) {
                if (listView.isItemChecked(i)) {
                    ref.child(apps[i]).setValue(true)
                }
            }
        }
    }
}
