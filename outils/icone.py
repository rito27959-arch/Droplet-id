"""Génère l'icône de Droplet ID : la carte d'identité Droplet.

Une carte ivoire inclinée sur un fond graphite : le monogramme « D. » en
Source Serif 4 (la police des titres de l'app), un guilloché léger, le filet
et le tampon « vérifié » en laiton. Même géométrie pour Android (drawables
vectoriels) et pour l'aperçu SVG.

Usage : python3 outils/icone.py <SourceSerif4[opsz,wght].ttf> app/src/main/res <dossier des aperçus>
"""
import math
import sys

from fontTools.pens.boundsPen import BoundsPen
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen
from fontTools.ttLib import TTFont
from fontTools.varLib.instancer import instantiateVariableFont

POLICE, RES, OUT = sys.argv[1], sys.argv[2], sys.argv[3]

# ── Le « D » : Source Serif 4, taille optique 60, graisse 600, 20 unités de haut.
police = instantiateVariableFont(TTFont(POLICE), {"opsz": 60, "wght": 600})
jeu = police.getGlyphSet()
nom = police.getBestCmap()[ord("D")]
b = BoundsPen(jeu)
jeu[nom].draw(b)
x0, y0, x1, y1 = b.bounds
H_D = 20.0
k = H_D / (y1 - y0)
L_D = (x1 - x0) * k
X_D, Y_D = 27.0, 40.0  # coin haut gauche du « D » sur la carte
stylo = SVGPathPen(jeu)
jeu[nom].draw(TransformPen(stylo, (k, 0, 0, -k, X_D - x0 * k, Y_D + H_D + y0 * k)))
D = stylo.getCommands()

# ── La carte : 74 × 47, coins de 7, inclinée de −8° autour de (54, 56).
CX, CY, CW, CH, CR = 17.0, 32.0, 74.0, 47.0, 7.0
ANGLE, PIVOT, ECHELLE = -8.0, (54.0, 56.0), 0.84


def rect_arrondi(x, y, w, h, r):
    return (f"M{x + r},{y} H{x + w - r} A{r},{r} 0 0,1 {x + w},{y + r} V{y + h - r} "
            f"A{r},{r} 0 0,1 {x + w - r},{y + h} H{x + r} A{r},{r} 0 0,1 {x},{y + h - r} "
            f"V{y + r} A{r},{r} 0 0,1 {x + r},{y} Z")


def cercle(cx, cy, r):
    return f"M{cx - r:.2f},{cy:.2f} a{r},{r} 0 1,0 {2 * r:.2f},0 a{r},{r} 0 1,0 {-2 * r:.2f},0 Z"


def rosace(cx, cy, R, a, lobes, ph, n=160):
    pts = []
    for i in range(n + 1):
        t = 2 * math.pi * i / n
        r = R + a * math.sin(lobes * t + ph)
        pts.append(f"{cx + r * math.cos(t):.2f},{cy + r * math.sin(t):.2f}")
    return "M" + " L".join(pts) + "Z"


CARTE = rect_arrondi(CX, CY, CW, CH, CR)
OMBRE = rect_arrondi(CX, CY + 2.6, CW, CH, CR)
# Guilloché allégé : 7 lignes, lisibles même en petit.
GUILLOCHE = [rosace(75, 53, 4 + 2.3 * j, 0.8, 8, j * 0.5) for j in range(7)]
FILET = f"M{CX},{CY} h3.2 v{CH} h-3.2 Z"
POINT = cercle(X_D + L_D + 3.4, Y_D + H_D - 2.2, 2.3)
LIGNE1 = rect_arrondi(27, 66, 30, 2.6, 1.3)
LIGNE2 = rect_arrondi(27, 71, 19, 2.2, 1.1)
TAMPON = cercle(76, 66, 7.4)
COCHE = "M72.8,66.2 L75,68.4 L79.3,63.8"

LAITON_DEGRADE = ("lin", (60, 58, 84, 74), [(0, "#FFF4D58E"), (0.5, "#FFD6B35F"), (1, "#FF9C7A2A")])

FOND = [
    ("M0,0h108v108h-108z", ("lin", (54, 0, 54, 108), [(0, "#FF303036"), (0.55, "#FF18181B"), (1, "#FF070708")])),
    ("M0,0h108v108h-108z", ("rad", (54, -6, 70), [(0, "#24FFFFFF"), (1, "#00FFFFFF")])),
]


# ── Android ─────────────────────────────────────────────────────────────
def a_grad(g):
    kind, geo, stops = g
    if kind == "lin":
        a, bb, c, d = geo
        s = f'<gradient android:type="linear" android:startX="{a}" android:startY="{bb}" android:endX="{c}" android:endY="{d}">'
    else:
        a, bb, r = geo
        s = f'<gradient android:type="radial" android:centerX="{a}" android:centerY="{bb}" android:gradientRadius="{r}">'
    return s + "".join(f'<item android:offset="{o}" android:color="{c}"/>' for o, c in stops) + "</gradient>"


def a_plein(d, f, ind="    "):
    if isinstance(f, str):
        return f'{ind}<path android:pathData="{d}" android:fillColor="{f}"/>\n'
    return f'{ind}<path android:pathData="{d}"><aapt:attr name="android:fillColor">{a_grad(f)}</aapt:attr></path>\n'


def a_trait(d, couleur, ep, ind="    ", rond=False):
    fin = ' android:strokeLineCap="round" android:strokeLineJoin="round"' if rond else ""
    if isinstance(couleur, str):
        return f'{ind}<path android:pathData="{d}" android:strokeColor="{couleur}" android:strokeWidth="{ep}" android:fillColor="#00000000"{fin}/>\n'
    return (f'{ind}<path android:pathData="{d}" android:strokeWidth="{ep}" android:fillColor="#00000000"{fin}>'
            f'<aapt:attr name="android:strokeColor">{a_grad(couleur)}</aapt:attr></path>\n')


ENTETE = ('<?xml version="1.0" encoding="utf-8"?>\n<!-- {c} Généré par outils/icone.py. -->\n'
          '<vector xmlns:android="http://schemas.android.com/apk/res/android" xmlns:aapt="http://schemas.android.com/aapt"\n'
          '  android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">\n')
GROUPE = (f'  <group android:rotation="{ANGLE}" android:scaleX="{ECHELLE}" android:scaleY="{ECHELLE}" '
          f'android:pivotX="{PIVOT[0]}" android:pivotY="{PIVOT[1]}">\n')

avant = ENTETE.format(c="La carte d'identité Droplet : « D. », guilloché, filet et tampon en laiton.") + GROUPE
avant += a_plein(OMBRE, "#73000000")
avant += a_plein(CARTE, ("lin", (54, CY, 54, CY + CH), [(0, "#FFFFFFFF"), (1, "#FFEBE6DC")]))
avant += f'    <group>\n      <clip-path android:pathData="{CARTE}"/>\n'
avant += "".join(a_trait(d, "#8CB8913E", 0.5, ind="      ") for d in GUILLOCHE)
avant += a_plein(FILET, "#FFC9A24A", ind="      ")
avant += "    </group>\n"
avant += a_plein(D, "#FF17181C")
avant += a_plein(POINT, "#FFC9A24A")
avant += a_plein(LIGNE1, "#BF17181C") + a_plein(LIGNE2, "#4D17181C")
avant += a_plein(TAMPON, "#FFFFFFFF")
avant += a_trait(TAMPON, LAITON_DEGRADE, 1.6)
avant += a_trait(COCHE, "#FF9C7A2A", 1.7, rond=True)
avant += "  </group>\n</vector>\n"
open(f"{RES}/drawable/ic_lanceur_sceau.xml", "w").write(avant)

fond = ENTETE.format(c="Fond graphite, lustre doux en haut.") + "".join(a_plein(d, f, ind="  ") for d, f in FOND) + "</vector>\n"
open(f"{RES}/drawable/ic_lanceur_fond.xml", "w").write(fond)

mono = ENTETE.format(c="Version monochrome (icônes à thème d'Android 13+).") + GROUPE
mono += a_trait(CARTE, "#FFFFFFFF", 2.2)
mono += a_plein(D, "#FFFFFFFF") + a_plein(POINT, "#FFFFFFFF") + a_plein(LIGNE1, "#FFFFFFFF")
mono += a_trait(TAMPON, "#FFFFFFFF", 1.8) + a_trait(COCHE, "#FFFFFFFF", 1.8, rond=True)
mono += "  </group>\n</vector>\n"
open(f"{RES}/drawable/ic_lanceur_monochrome.xml", "w").write(mono)


# ── Aperçus SVG ─────────────────────────────────────────────────────────
def s_couleur(c):
    c = c.lstrip("#")
    return "#" + c[2:], int(c[:2], 16) / 255


defs, n = [], [0]


def s_ref(f):
    if isinstance(f, str):
        rgb, a = s_couleur(f)
        return rgb, f"{a:.3f}"
    n[0] += 1
    i = f"g{n[0]}"
    kind, geo, stops = f
    arrets = "".join(f'<stop offset="{o}" stop-color="{s_couleur(c)[0]}" stop-opacity="{s_couleur(c)[1]:.3f}"/>' for o, c in stops)
    if kind == "lin":
        a, bb, c, d = geo
        defs.append(f'<linearGradient id="{i}" gradientUnits="userSpaceOnUse" x1="{a}" y1="{bb}" x2="{c}" y2="{d}">{arrets}</linearGradient>')
    else:
        a, bb, r = geo
        defs.append(f'<radialGradient id="{i}" gradientUnits="userSpaceOnUse" cx="{a}" cy="{bb}" r="{r}">{arrets}</radialGradient>')
    return f"url(#{i})", "1"


def s_plein(d, f):
    c, a = s_ref(f)
    return f'<path d="{d}" fill="{c}" fill-opacity="{a}"/>'


def s_trait(d, f, ep):
    c, a = s_ref(f)
    return f'<path d="{d}" fill="none" stroke="{c}" stroke-opacity="{a}" stroke-width="{ep}" stroke-linecap="round" stroke-linejoin="round"/>'


corps = "".join(s_plein(d, f) for d, f in FOND)
corps += f'<g transform="translate({PIVOT[0]} {PIVOT[1]}) rotate({ANGLE}) scale({ECHELLE}) translate({-PIVOT[0]} {-PIVOT[1]})">'
corps += s_plein(OMBRE, "#73000000") + s_plein(CARTE, ("lin", (54, CY, 54, CY + CH), [(0, "#FFFFFFFF"), (1, "#FFEBE6DC")]))
defs.append(f'<clipPath id="carte"><path d="{CARTE}"/></clipPath>')
corps += '<g clip-path="url(#carte)">' + "".join(s_trait(d, "#8CB8913E", 0.5) for d in GUILLOCHE) + s_plein(FILET, "#FFC9A24A") + "</g>"
corps += s_plein(D, "#FF17181C") + s_plein(POINT, "#FFC9A24A") + s_plein(LIGNE1, "#BF17181C") + s_plein(LIGNE2, "#4D17181C")
corps += s_plein(TAMPON, "#FFFFFFFF") + s_trait(TAMPON, LAITON_DEGRADE, 1.6) + s_trait(COCHE, "#FF9C7A2A", 1.7) + "</g>"
open(f"{OUT}/icone-carte.svg", "w").write(
    f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="432" height="432"><defs>{"".join(defs)}'
    f'<clipPath id="forme"><rect width="108" height="108" rx="24"/></clipPath></defs><g clip-path="url(#forme)">{corps}</g>'
    f'<rect x=".5" y=".5" width="107" height="107" rx="23.5" fill="none" stroke="#fff" stroke-opacity=".07"/></svg>')
open(f"{OUT}/icone-carte-mono.svg", "w").write(
    f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="432" height="432"><rect width="108" height="108" rx="24" fill="#2E3A2E"/>'
    f'<g transform="translate({PIVOT[0]} {PIVOT[1]}) rotate({ANGLE}) scale({ECHELLE}) translate({-PIVOT[0]} {-PIVOT[1]})" fill="#CFE3C8" stroke="#CFE3C8"><path d="{CARTE}" fill="none" stroke-width="2.2"/>'
    f'<path d="{D}" stroke="none"/><path d="{POINT}" stroke="none"/><path d="{LIGNE1}" stroke="none"/>'
    f'<path d="{TAMPON}" fill="none" stroke-width="1.8"/><path d="{COCHE}" fill="none" stroke-width="1.8" stroke-linecap="round"/></g></svg>')
print("ok")
