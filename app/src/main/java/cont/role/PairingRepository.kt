package cont.role

import com.google.firebase.auth.FirebaseAuth
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
    private val auth = FirebaseAuth.getInstance()

    /** S'assure qu'on est authentifié (anonyme) avant d'utiliser la base. */
    private fun ensureSignedIn(onReady: () -> Unit, onError: (String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            onReady()
            return
        }
        auth.signInAnonymously()
            .addOnSuccessListener { onReady() }
            .addOnFailureListener { onError(it.message ?: "Erreur d'authentification") }
    }

    /** Génère un code à 6 chiffres côté enfant et l'enregistre en attente de couplage. */
    fun generateCode(
        childDeviceId: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        ensureSignedIn(onReady = {
            val code = (100000..999999).random().toString()
            db.child("pairings").child(code).child("childDeviceId")
                .setValue(childDeviceId)
                .addOnSuccessListener { onResult(code) }
                .addOnFailureListener { onError(it.message ?: "Impossible de générer le code") }
        }, onError = onError)
    }

    /** Côté parent : relie son compte au code fourni par l'enfant. */
    fun connectWithCode(code: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        ensureSignedIn(onReady = {
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
        }, onError = onError)
    }
}

/** Identifiant simple et stable pour l'appareil (à améliorer avec un UUID persistant en prod). */
fun deviceId(): String = Random(System.currentTimeMillis()).nextInt(100000, 999999).toString()
