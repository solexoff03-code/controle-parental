package cont.role

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Service d'accessibilité MINIMAL : il observe uniquement quel package passe
 * au premier plan (comportement standard d'un contrôle parental) et, si ce
 * package figure dans la liste bloquée définie par le parent, ramène
 * l'utilisateur à l'écran d'accueil.
 *
 * Ce service NE LIT AUCUN CONTENU (pas de texte, pas de SMS, pas d'écran)
 * et NE SIMULE AUCUN CLIC à la place de l'utilisateur.
 */
class AppBlockerAccessibilityService : AccessibilityService() {

    private var blockedApps: Set<String> = emptySet()

    override fun onServiceConnected() {
        val childDeviceId = getSharedPreferences("prefs", MODE_PRIVATE).getString("deviceId", null) ?: return
        val ref = FirebaseDatabase.getInstance().reference
            .child("devices").child(childDeviceId).child("blockedApps")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blockedApps = snapshot.children.mapNotNull { it.key }.toSet()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (pkg in blockedApps) {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(homeIntent)
        }
    }

    override fun onInterrupt() {}
}
