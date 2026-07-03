package cont.role

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

/**
 * Écran d'accueil : l'utilisateur choisit s'il installe l'app côté "Parent"
 * (celui qui supervise) ou côté "Enfant" (l'appareil supervisé).
 *
 * Important : le rôle "Enfant" affiche toujours une icône dans le launcher
 * et une notification permanente dès que le suivi est actif. Rien n'est caché.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnParent).setOnClickListener {
            startActivity(Intent(this, ParentActivity::class.java))
        }

        findViewById<Button>(R.id.btnChild).setOnClickListener {
            startActivity(Intent(this, ChildActivity::class.java))
        }
    }
}
