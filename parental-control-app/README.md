# Contrôle Parental — projet de départ

Application Android (Kotlin) qui met en relation un appareil **parent** et un
appareil **enfant** via un code à 6 chiffres, avec :

- 📍 Localisation GPS en temps réel (service au premier plan, **notification permanente visible**)
- 🚫 Blocage d'applications choisies par le parent

Ce projet respecte volontairement une contrainte : **rien n'est caché sur
l'appareil de l'enfant.** L'app apparaît dans le launcher, et le suivi de
position affiche toujours une notification tant qu'il est actif. C'est ce qui
distingue un contrôle parental légal d'un logiciel espion, et c'est aussi ce
qu'exige Google Play.

## 1. Prérequis

- [Android Studio](https://developer.android.com/studio) (dernière version)
- Un compte [Firebase](https://console.firebase.google.com/) (gratuit)

## 2. Configurer Firebase (obligatoire)

1. Va sur console.firebase.google.com → **Ajouter un projet**
2. Ajoute une app Android avec le package `com.parentalcontrol.app`
3. Télécharge le fichier `google-services.json` généré
4. Place-le dans `app/google-services.json` (il n'est pas fourni ici pour des raisons de sécurité — chaque projet doit avoir le sien)
5. Dans la console Firebase → **Realtime Database** → crée une base en mode "test" pour démarrer

## 3. Ouvrir et lancer le projet

1. Ouvre le dossier `parental-control-app` dans Android Studio
2. Laisse Gradle synchroniser
3. Lance l'app sur deux appareils (ou un appareil + un émulateur) :
   - Sur l'un, choisis **"Cet appareil est celui de l'enfant"** → note le code affiché
   - Sur l'autre, choisis **"Je suis le parent"** → entre le code

## 4. Générer l'APK automatiquement via GitHub

Ce repo contient un workflow GitHub Actions (`.github/workflows/build-apk.yml`)
qui compile l'APK à chaque `push` sur `main`.

1. Crée un repo GitHub et pousse ce projet dedans
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/<ton-user>/<ton-repo>.git
   git push -u origin main
   ```
2. **Important** : `google-services.json` contient des identifiants de projet
   Firebase. Ne le commite pas publiquement — ajoute-le à `.gitignore`, ou
   utilise les [GitHub Secrets](https://docs.github.com/actions/security-guides/encrypted-secrets)
   pour l'injecter pendant le build si le repo est public.
3. Va dans l'onglet **Actions** de ton repo GitHub → le build se lance → l'APK
   est disponible en téléchargement dans les "Artifacts" une fois terminé.

## 5. Ce qui manque encore (à toi de continuer)

- Écran carte (Google Maps) au lieu d'afficher juste lat/lng en texte
- Historique des positions (au lieu de la dernière position uniquement)
- Récupération de la vraie liste d'apps installées côté enfant (actuellement
  simplifié : le parent voit ses propres apps)
- Gestion des permissions Android 13+/14 plus fine (notifications, localisation en arrière-plan)
- Authentification Firebase réelle (actuellement les données sont ouvertes en mode "test")

## Limites volontaires de ce projet

Cette app **ne permet pas** de :
- Voir l'écran de l'enfant en direct sans qu'il valide chaque session
- Lire le contenu des SMS ou des appels
- Simuler des clics ou prendre le contrôle du téléphone à distance

Ces fonctions rapprocheraient l'app d'un outil de surveillance caché plutôt
que d'un contrôle parental transparent.
