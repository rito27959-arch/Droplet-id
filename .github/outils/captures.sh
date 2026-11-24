#!/usr/bin/env bash
# Installe l'APK de debug et photographie chaque écran en clair et en sombre.
# Chaque commande adb a sa limite de temps : un écran qui bloque n'arrête pas
# les autres.
set -u
mkdir -p captures
timeout 120 adb install -r app/build/outputs/apk/debug/app-debug.apk
timeout 20 adb shell settings put system screen_off_timeout 1800000
timeout 20 adb shell input keyevent KEYCODE_WAKEUP
timeout 20 adb shell wm dismiss-keyguard || true
# Le journal complet, en continu : on y retrouve un plantage même si l'émulateur s'arrête.
adb logcat -c || true
(adb logcat -v time '*:W' > captures/journal.txt 2>&1 &)
for ecran in presentation bienvenue accueil accueil-action identite acces acces-vide detail securite compte apparence confidentialite apropos approbation approuvee; do
  for sombre in false true; do
    nom="$ecran-$([ "$sombre" = true ] && echo sombre || echo clair)"
    timeout 20 adb shell am force-stop app.dropletmesh.id
    if [ "$ecran" = icone ]; then
      [ "$sombre" = true ] && continue
      timeout 20 adb shell input keyevent KEYCODE_HOME
      sleep 4
    else
      timeout 30 adb shell am start -n app.dropletmesh.id/.CapturesActivity --es ecran "$ecran" --ez sombre "$sombre" > /dev/null
      sleep 7
    fi
    timeout 30 adb exec-out screencap -p > "captures/$nom.png"
    echo "$nom : $(stat -c %s "captures/$nom.png") octets"
  done
done
timeout 30 adb logcat -d -s AndroidRuntime:E > captures/plantages.txt || true
grep -E 'FATAL|AndroidRuntime|DropletID|glasmio|RuntimeShader|ANR' captures/journal.txt | head -200 > captures/extrait.txt || true
