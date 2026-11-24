"""Génère l'icône de Droplet ID (drawables Android) et ses aperçus SVG.

Une médaille de laiton : rebord éclairé en haut à gauche, face en creux
(éclairage inversé, d'où le biseau), gravure guillochée, centre en relief,
reflet spéculaire, ombre portée douce. Fond noir profond.
"""
import math, sys

# Usage : python3 outils/icone.py app/src/main/res <dossier des aperçus>
RES = sys.argv[1]
OUT = sys.argv[2]
cx = cy = 54.0


def cercle(r, x=cx, y=cy):
    return f"M{x - r:.2f},{y:.2f} a{r},{r} 0 1,0 {2 * r:.2f},0 a{r},{r} 0 1,0 {-2 * r:.2f},0 Z"


def rosace(R0, a, k, ph, n=240):
    pts = []
    for i in range(n + 1):
        t = 2 * math.pi * i / n
        r = R0 + a * math.sin(k * t + ph)
        pts.append((cx + r * math.cos(t), cy + r * math.sin(t)))
    return "M" + " L".join(f"{x:.2f},{y:.2f}" for x, y in pts) + "Z"


LIGNES = [rosace(10.5 + 2.4 * j, 1.9, 8, j * 0.5) for j in range(5)]
R_REBORD, R_FACE, R_CENTRE = 30.0, 25.6, 5.2
CLAIR, LAITON, MOYEN, BRONZE, GRAVURE = "#FFF4DA9C", "#FFD6B35F", "#FFB08A3A", "#FF6E5214", "#995A420E"

# (forme, dégradé) : la même liste sert à Android et au SVG.
COUCHES_FOND = [
    ("M0,0h108v108h-108z", ("lin", (54, 0, 54, 108), [(0, "#FF1C1C1F"), (1, "#FF050506")])),
    ("M0,0h108v108h-108z", ("rad", (54, 50, 52), [(0, "#2ED6B35F"), (1, "#00D6B35F")])),
]
COUCHES_AVANT = [
    (cercle(34, y=cy + 2.5), ("rad", (cx, cy + 2.5, 34), [(0, "#99000000"), (0.82, "#55000000"), (1, "#00000000")])),
    (cercle(R_REBORD), ("lin", (32, 26, 78, 84), [(0, CLAIR), (0.45, LAITON), (1, BRONZE)])),
    (cercle(R_FACE), ("lin", (36, 30, 72, 80), [(0, "#FF8C6A20"), (0.55, "#FFC29C47"), (1, "#FFE2C276")])),
    "GRAVURE",
    (cercle(R_CENTRE + 0.9), ("lin", (48, 48, 60, 60), [(0, "#FF5A420E"), (1, "#FFB08A3A")])),
    (cercle(R_CENTRE), ("lin", (49, 48, 59, 60), [(0, CLAIR), (0.6, LAITON), (1, MOYEN)])),
    "REFLET",
]
REFLET = (f"M{cx - 40},{cy - 40} h80 v32 h-80 Z", ("lin", (cx, cy - 32, cx, cy - 8), [(0, "#40FFFFFF"), (1, "#00FFFFFF")]))


# ---------- Android ----------
def a_grad(g):
    kind, geo, stops = g
    if kind == "lin":
        x1, y1, x2, y2 = geo
        s = f'<gradient android:type="linear" android:startX="{x1}" android:startY="{y1}" android:endX="{x2}" android:endY="{y2}">'
    else:
        x, y, r = geo
        s = f'<gradient android:type="radial" android:centerX="{x}" android:centerY="{y}" android:gradientRadius="{r}">'
    for o, c in stops:
        s += f'<item android:offset="{o}" android:color="{c}"/>'
    return s + "</gradient>"


def a_rempli(d, g, ind="  "):
    return f'{ind}<path android:pathData="{d}"><aapt:attr name="android:fillColor">{a_grad(g)}</aapt:attr></path>\n'


def a_trait(d, col, w):
    return f'  <path android:pathData="{d}" android:strokeColor="{col}" android:strokeWidth="{w}" android:fillColor="#00000000" android:strokeLineJoin="round"/>\n'


ENTETE = ('<?xml version="1.0" encoding="utf-8"?>\n<!-- {c} Généré par l\'outil d\'icône. -->\n'
          '<vector xmlns:android="http://schemas.android.com/apk/res/android" xmlns:aapt="http://schemas.android.com/aapt"\n'
          '  android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">\n')

avant = ENTETE.format(c="Le sceau de Droplet ID : médaille de laiton guillochée.")
for c in COUCHES_AVANT:
    if c == "GRAVURE":
        for d in LIGNES:
            avant += a_trait(d, GRAVURE, 0.75)
    elif c == "REFLET":
        avant += f'  <group>\n    <clip-path android:pathData="{cercle(R_REBORD)}"/>\n' + a_rempli(*REFLET, ind="    ") + "  </group>\n"
    else:
        avant += a_rempli(*c)
open(f"{RES}/drawable/ic_lanceur_sceau.xml", "w").write(avant + "</vector>\n")

fond = ENTETE.format(c="Noir profond, plus clair en haut, lueur de laiton derrière la médaille.")
for c in COUCHES_FOND:
    fond += a_rempli(*c)
open(f"{RES}/drawable/ic_lanceur_fond.xml", "w").write(fond + "</vector>\n")

mono = ENTETE.format(c="Version monochrome (icônes à thème d'Android 13+).")
mono += f'  <path android:pathData="{cercle(R_REBORD)} {cercle(R_FACE)}" android:fillColor="#FFFFFFFF" android:fillType="evenOdd"/>\n'
for d in LIGNES:
    mono += a_trait(d, "#FFFFFFFF", 0.9)
mono += f'  <path android:pathData="{cercle(R_CENTRE)}" android:fillColor="#FFFFFFFF"/>\n'
open(f"{RES}/drawable/ic_lanceur_monochrome.xml", "w").write(mono + "</vector>\n")


# ---------- Aperçus SVG ----------
def s_couleur(c):
    c = c.lstrip("#")
    return "#" + c[2:], int(c[:2], 16) / 255


compteur = [0]


def s_grad(g, defs):
    compteur[0] += 1
    i = f"g{compteur[0]}"
    kind, geo, stops = g
    if kind == "lin":
        x1, y1, x2, y2 = geo
        s = f'<linearGradient id="{i}" gradientUnits="userSpaceOnUse" x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}">'
        fin = "</linearGradient>"
    else:
        x, y, r = geo
        s = f'<radialGradient id="{i}" gradientUnits="userSpaceOnUse" cx="{x}" cy="{y}" r="{r}">'
        fin = "</radialGradient>"
    for o, c in stops:
        rgb, a = s_couleur(c)
        s += f'<stop offset="{o}" stop-color="{rgb}" stop-opacity="{a:.3f}"/>'
    defs.append(s + fin)
    return i


defs, corps = [], []
for d, g in COUCHES_FOND:
    corps.append(f'<path d="{d}" fill="url(#{s_grad(g, defs)})"/>')
for c in COUCHES_AVANT:
    if c == "GRAVURE":
        rgb, a = s_couleur(GRAVURE)
        corps += [f'<path d="{d}" fill="none" stroke="{rgb}" stroke-opacity="{a:.2f}" stroke-width=".75" stroke-linejoin="round"/>' for d in LIGNES]
    elif c == "REFLET":
        defs.append(f'<clipPath id="cl"><path d="{cercle(R_REBORD)}"/></clipPath>')
        corps.append(f'<path clip-path="url(#cl)" d="{REFLET[0]}" fill="url(#{s_grad(REFLET[1], defs)})"/>')
    else:
        corps.append(f'<path d="{c[0]}" fill="url(#{s_grad(c[1], defs)})"/>')
open(f"{OUT}/icone2.svg", "w").write(
    f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="432" height="432"><defs>{"".join(defs)}'
    f'<clipPath id="forme"><rect width="108" height="108" rx="24"/></clipPath></defs><g clip-path="url(#forme)">{"".join(corps)}</g></svg>')
open(f"{OUT}/icone2-mono.svg", "w").write(
    f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="432" height="432"><rect width="108" height="108" rx="24" fill="#2E3A2E"/>'
    f'<path d="{cercle(R_REBORD)} {cercle(R_FACE)}" fill="#CFE3C8" fill-rule="evenodd"/>'
    + "".join(f'<path d="{d}" fill="none" stroke="#CFE3C8" stroke-width=".9"/>' for d in LIGNES)
    + f'<path d="{cercle(R_CENTRE)}" fill="#CFE3C8"/></svg>')
print("ok")
