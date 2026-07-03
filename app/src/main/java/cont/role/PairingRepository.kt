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

    // Évaluer les instances à la demande pour éviter d'échouer au chargement de l'objet
    private val db
        get() = FirebaseDatabase.getInstance().reference

    private val auth
        get() = FirebaseAuth.getInstance()

    /** S'assure qu'on est authentifié (anonyme) avant d'utiliser la base. */
    private fun ensureSignedIn(onReady: () -> Unit, onError: (String) -> Unit) {
        try {
            val currentUser = try { auth.currentUser } catch (e: Exception) {
                // Probablement Firebase non initialisé (google-services.json manquant ou mauvaise configuration)
                onError("Firebase non initialisé : placez votre app/google-services.json et vérifiez le package dans la console Firebase. (${e.message})")
                return
            }

            if (currentUser != null) {
                onReady()
                return
            }

            auth.signInAnonymously()
                .addOnSuccessListener { onReady() }
                .addOnFailureListener { onError(it.message ?: "Erreur d'authentification") }
        } catch (e: Exception) {
            onError(e.message ?: "Erreur interne lors de la configuration Firebase")
        }
    }

    /** Génère un code à 6 chiffres côté enfant et l'enregistre en attente de couplage. */
    fun generateCode(
        childDeviceId: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        ensureSignedIn(onReady = {
            try {
                val code = (100000..999999).random().toString()
                db.child("pairings").child(code).child("childDeviceId")
                    .setValue(childDeviceId)
                    .addOnSuccessListener { onResult(code) }
                    .addOnFailureListener { onError(it.message ?: "Impossible de générer le code") }
            } catch (e: Exception) {
                onError(e.message ?: "Erreur lors de l'accès à la base Firebase")
            }
        }, onError = onError)
    }

    /** Côté parent : relie son compte au code fourni par l'enfant. */
    fun connectWithCode(code: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        ensureSignedIn(onReady = {
            try {
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
            } catch (e: Exception) {
                onError(e.message ?: "Erreur lors de l'accès à la base Firebase")
            }
        }, onError = onError)
    }
}

/** Identifiant simple et stable pour l'appareil (à améliorer avec un UUID persistant en prod). */
fun deviceId(): String = Random(System.currentTimeMillis()).nextInt(100000, 999999).toString()
