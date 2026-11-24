"""Génère l'icône de Droplet ID : le monogramme « D. ».

Un « D » de Source Serif 4 (la police des titres de l'app, tracé à la taille
optique « affiche » pour des pleins et déliés fins) et un point en laiton,
sur un fond graphite. Même géométrie pour Android (drawables vectoriels) et
pour l'aperçu SVG.

Usage : python3 outils/icone.py <police SourceSerif4[opsz,wght].ttf> app/src/main/res <dossier des aperçus>
"""
import sys

from fontTools.pens.boundsPen import BoundsPen
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen
from fontTools.ttLib import TTFont
from fontTools.varLib.instancer import instantiateVariableFont

POLICE, RES, OUT = sys.argv[1], sys.argv[2], sys.argv[3]

# ── Le glyphe « D » : Source Serif 4, taille optique 60 (affiche), graisse 560.
police = instantiateVariableFont(TTFont(POLICE), {"opsz": 60, "wght": 560})
jeu = police.getGlyphSet()
nom = police.getBestCmap()[ord("D")]
bornes = BoundsPen(jeu)
jeu[nom].draw(bornes)
x0, y0, x1, y1 = bornes.bounds

# Le « D » fait 44 unités de haut sur 108 ; l'ensemble « D » + point est
# centré optiquement (un peu à gauche et un peu haut : le point pèse à droite).
HAUTEUR = 44.0
k = HAUTEUR / (y1 - y0)
largeur = (x1 - x0) * k
RAYON_POINT, ECART = 5.0, 3.0
total = largeur + ECART + 2 * RAYON_POINT
gauche = (108 - total) / 2 - 0.6
bas = 54 + HAUTEUR / 2 - 2.0
# Police : y vers le haut ; vecteur : y vers le bas.
transformation = (k, 0, 0, -k, gauche - x0 * k, bas + y0 * k)
stylo = SVGPathPen(jeu)
jeu[nom].draw(TransformPen(stylo, transformation))
D = stylo.getCommands()
cx_point = gauche + largeur + ECART + RAYON_POINT
cy_point = bas - RAYON_POINT


def cercle(cx, cy, r):
    return f"M{cx - r:.2f},{cy:.2f} a{r},{r} 0 1,0 {2 * r:.2f},0 a{r},{r} 0 1,0 {-2 * r:.2f},0 Z"


POINT = cercle(cx_point, cy_point, RAYON_POINT)
OMBRE_POINT = cercle(cx_point, cy_point + 0.9, RAYON_POINT + 0.3)

# ── Les couches : (forme, remplissage, décalage y de l'ombre)
# remplissage = couleur "#AARRGGBB" ou dégradé ("lin"|"rad", géométrie, arrêts).
FOND = [
    ("M0,0h108v108h-108z", ("lin", (54, 0, 54, 108), [(0, "#FF303036"), (0.55, "#FF18181B"), (1, "#FF070708")])),
    # Le lustre : une lumière douce qui vient d'en haut.
    ("M0,0h108v108h-108z", ("rad", (54, -6, 70), [(0, "#24FFFFFF"), (1, "#00FFFFFF")])),
]
AVANT = [
    # Une ombre très légère sous le « D », juste assez pour le détacher.
    (D, "#40000000", 1.2),
    # Le « D », du blanc pur au blanc chaud vers le bas.
    (D, ("lin", (54, bas - HAUTEUR, 54, bas), [(0, "#FFFFFFFF"), (1, "#FFE6E2D8")]), 0),
    # Le point : une perle de laiton, éclairée en haut à gauche.
    (OMBRE_POINT, "#55000000", 0),
    (POINT, ("rad", (cx_point - 1.6, cy_point - 1.8, RAYON_POINT * 1.9),
             [(0, "#FFFBE7B2"), (0.45, "#FFDDB960"), (1, "#FF8E6A1C")]), 0),
]


# ── Android ─────────────────────────────────────────────────────────────
def a_grad(g):
    kind, geo, stops = g
    if kind == "lin":
        a, b, c, d = geo
        s = f'<gradient android:type="linear" android:startX="{a:.2f}" android:startY="{b:.2f}" android:endX="{c:.2f}" android:endY="{d:.2f}">'
    else:
        a, b, r = geo
        s = f'<gradient android:type="radial" android:centerX="{a:.2f}" android:centerY="{b:.2f}" android:gradientRadius="{r:.2f}">'
    return s + "".join(f'<item android:offset="{o}" android:color="{c}"/>' for o, c in stops) + "</gradient>"


def a_couche(d, remplissage, dy=0):
    debut = f'  <group android:translateY="{dy}">\n  ' if dy else ""
    fin = "  </group>\n" if dy else ""
    if isinstance(remplissage, str):
        return f'{debut}  <path android:pathData="{d}" android:fillColor="{remplissage}"/>\n{fin}'
    return f'{debut}  <path android:pathData="{d}"><aapt:attr name="android:fillColor">{a_grad(remplissage)}</aapt:attr></path>\n{fin}'


ENTETE = ('<?xml version="1.0" encoding="utf-8"?>\n<!-- {c} Généré par outils/icone.py. -->\n'
          '<vector xmlns:android="http://schemas.android.com/apk/res/android" xmlns:aapt="http://schemas.android.com/aapt"\n'
          '  android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">\n')

open(f"{RES}/drawable/ic_lanceur_fond.xml", "w").write(
    ENTETE.format(c="Fond graphite, lustre doux en haut.") + "".join(a_couche(d, f) for d, f in FOND) + "</vector>\n")
open(f"{RES}/drawable/ic_lanceur_sceau.xml", "w").write(
    ENTETE.format(c="Le monogramme : « D » en Source Serif 4 et point de laiton.") + "".join(a_couche(*c) for c in AVANT) + "</vector>\n")
open(f"{RES}/drawable/ic_lanceur_monochrome.xml", "w").write(
    ENTETE.format(c="Version monochrome (icônes à thème d'Android 13+).")
    + a_couche(D, "#FFFFFFFF") + a_couche(POINT, "#FFFFFFFF") + "</vector>\n")


# ── Aperçu SVG ──────────────────────────────────────────────────────────
def s_couleur(c):
    c = c.lstrip("#")
    return "#" + c[2:], int(c[:2], 16) / 255


defs, n = [], [0]


def s_remp(f):
    if isinstance(f, str):
        rgb, a = s_couleur(f)
        return f'fill="{rgb}" fill-opacity="{a:.3f}"'
    n[0] += 1
    i = f"g{n[0]}"
    kind, geo, stops = f
    arrets = "".join(f'<stop offset="{o}" stop-color="{s_couleur(c)[0]}" stop-opacity="{s_couleur(c)[1]:.3f}"/>' for o, c in stops)
    if kind == "lin":
        a, b, c, d = geo
        defs.append(f'<linearGradient id="{i}" gradientUnits="userSpaceOnUse" x1="{a}" y1="{b}" x2="{c}" y2="{d}">{arrets}</linearGradient>')
    else:
        a, b, r = geo
        defs.append(f'<radialGradient id="{i}" gradientUnits="userSpaceOnUse" cx="{a}" cy="{b}" r="{r}">{arrets}</radialGradient>')
    return f'fill="url(#{i})"'


corps = "".join(f'<path d="{d}" {s_remp(f)}/>' for d, f in FOND)
corps += "".join(f'<path transform="translate(0 {dy})" d="{d}" {s_remp(f)}/>' for d, f, dy in AVANT)
open(f"{OUT}/icone-a.svg", "w").write(
    f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="432" height="432"><defs>{"".join(defs)}'
    f'<clipPath id="forme"><rect width="108" height="108" rx="24"/></clipPath></defs><g clip-path="url(#forme)">{corps}</g>'
    f'<rect x=".5" y=".5" width="107" height="107" rx="23.5" fill="none" stroke="#fff" stroke-opacity=".07"/></svg>')
open(f"{OUT}/icone-a-mono.svg", "w").write(
    f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="432" height="432"><rect width="108" height="108" rx="24" fill="#2E3A2E"/>'
    f'<path d="{D}" fill="#CFE3C8"/><path d="{POINT}" fill="#CFE3C8"/></svg>')
print("ok")
