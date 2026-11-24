#!/usr/bin/env bash
# Installe l'APK de debug et photographie chaque écran en clair et en sombre.
set -u
mkdir -p captures
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell settings put system screen_off_timeout 600000
adb shell input keyevent KEYCODE_WAKEUP
adb shell wm dismiss-keyguard || true
for ecran in presentation bienvenue accueil accueil-action identite acces acces-vide detail securite compte apparence confidentialite apropos approbation approuvee; do
  for sombre in false true; do
    nom="$ecran-$([ "$sombre" = true ] && echo sombre || echo clair)"
    adb shell am force-stop app.dropletmesh.id
    adb shell am start -W -n app.dropletmesh.id/.CapturesActivity --es ecran "$ecran" --ez sombre "$sombre" > /dev/null
    sleep 6
    adb exec-out screencap -p > "captures/$nom.png"
    echo "$nom : $(stat -c %s "captures/$nom.png") octets"
  done
done
adb logcat -d -s AndroidRuntime:E > captures/plantages.txt || true
