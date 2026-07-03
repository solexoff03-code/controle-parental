package cont.role

import com.google.firebase.database.FirebaseDatabase
import kotlin.random.Random

/**
 * Gère le couplage parent <-> enfant via un code à 6 chiffres.
 *
 * Structure Firebase :
 * /pairings/{code}/childDeviceId
 * /devices/{childDeviceId}/location/{lat,lng,timestamp}
 * /devices/{childDeviceId}/blockedApps/{packageName: true}
 */
object PairingRepository {

    private val db = FirebaseDatabase.getInstance().reference

    /** Génère un code à 6 chiffres côté enfant et l'enregistre en attente de couplage. */
    fun generateCode(childDeviceId: String, onResult: (String) -> Unit) {
        val code = (100000..999999).random().toString()
        db.child("pairings").child(code).child("childDeviceId").setValue(childDeviceId)
        onResult(code)
    }

    /** Côté parent : relie son compte au code fourni par l'enfant. */
    fun connectWithCode(code: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        db.child("pairings").child(code).child("childDeviceId")
            .get()
            .addOnSuccessListener { snapshot ->
                val childDeviceId = snapshot.getValue(String::class.java)
                if (childDeviceId != null) {
                    onSuccess(childDeviceId)
                } else {
                    onError("Code invalide ou expiré")
                }
            }
            .addOnFailureListener { onError(it.message ?: "Erreur réseau") }
    }
}

/** Identifiant simple et stable pour l'appareil (à améliorer avec un UUID persistant en prod). */
fun deviceId(): String = Random(System.currentTimeMillis()).nextInt(100000, 999999).toString()
