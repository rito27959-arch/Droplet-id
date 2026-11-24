# Droplet ID pour Android

L'app native de Droplet ID : votre identité Droplet tient dans ce téléphone. Elle sert à se connecter aux apps et aux sites « Continuer avec Droplet ID » sans mot de passe, sans e-mail et sans numéro.

## Ce que fait l'app

- **Créer son Droplet ID** : une clé Ed25519 est créée sur le téléphone. Le service `id.dropletmesh.app` ne reçoit que la clé publique et le modèle du téléphone.
- **Approuver une connexion** : on scanne le code du site (ou on l'ouvre par le lien « Ouvrir dans Droplet ID »), on tape le nombre affiché, puis on déverrouille le téléphone (empreinte, visage ou code). La requête est signée par la clé de l'appareil.
- **Voir et retirer les accès** : la liste des apps connectées, ce que chacune reçoit, et le retrait en deux gestes.
- **Sécurité** : l'état réel du téléphone (verrouillage, biométrie, puce StrongBox) avec les actions recommandées, et « Oublier Droplet ID sur ce téléphone ».
- **Compte** : apparence (système, clair ou sombre), réduction de la transparence, langue et notifications (réglages Android), ce qui est conservé, licences.

L'API utilisée est celle du service déjà en production, la même que le module Droplet ID de l'app Droplet. Le protocole des requêtes signées est décrit dans `app/src/main/java/app/dropletmesh/id/donnees/Protocole.kt`.

## Direction artistique

- **Composants : [Glasmio](https://github.com/NEORUAA/Glasmio) 0.1.4** (Apache 2.0). Il fournit les listes groupées, les pages à grand titre, la barre d'onglets et les boutons en verre, les alertes, l'interrupteur et le contrôle segmenté.
- **Couleurs : Encre et laiton.** L'encre (graphite) porte les actions. Le laiton est réservé à l'autorité : le sceau, la carte d'identité et le focus. Le rouge sert seulement aux erreurs et aux actions destructives. Les jetons sont dans `core/designsystem/.../Couleurs.kt`.
- **Typographie : Source Serif 4** pour les grands titres, **Source Sans 3** pour le texte et **Source Code Pro** pour les identifiants. Ce sont trois polices variables sous licence SIL OFL 1.1, embarquées dans l'app ; leurs licences sont dans `licences/`.
- **Carte d'identité** : au format carte bancaire et noire dans les deux modes. Son guilloché est calculé à partir de l'identifiant, donc chaque carte a le sien.

## Sécurité

- La graine Ed25519 est chiffrée par Tink (AES-256-GCM), avec une clé maîtresse gardée dans le Keystore Android. Elle n'est jamais envoyée.
- Le serveur ne vérifie pas encore la biométrie. Elle protège seulement l'accès à l'écran d'approbation, car la clé de signature n'est pas encore liée au déverrouillage dans le Keystore. Lier la clé au déverrouillage demande que le serveur accepte une clé Keystore ; c'est la prochaine étape côté serveur.
- Les données de l'app sont exclues des sauvegardes et des transferts d'appareil. Aucun trafic en clair n'est autorisé.
- Aucun secret dans le dépôt ni dans l'APK : pas de clé d'API, pas de secret client.

## Ce qui n'existe pas encore

La récupération de compte depuis un autre appareil, les passkeys côté serveur, l'historique des connexions, les sessions et l'identité vérifiable (SD-JWT) demandent de nouvelles API sur le serveur. L'app affiche honnêtement ce qui manque (par exemple « Récupération : pas encore disponible ») au lieu de montrer de fausses données.

## Compiler

Il faut le JDK 21 et le SDK Android (plateforme 37). Glasmio compile contre l'API 37.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :core:designsystem:testDebugUnitTest
```

La CI (`.github/workflows/compiler.yml`) compile et teste à chaque envoi, puis publie l'APK de debug comme artefact de l'exécution.

Android 12 (API 31) minimum. Sur Android 12 et 12L, Glasmio affiche un verre simplifié ; la réfraction complète demande Android 13.
