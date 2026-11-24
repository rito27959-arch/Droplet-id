// ============================================================================
// LE MOTEUR D'ANIMATION DU SITE — partagé par toutes les pages.
//
//   - un défilement doux avec inertie (Lenis) ;
//   - [data-scene="epingle"] : section haute dont le contenu reste à l'écran ;
//     --p va de 0 à 1 pendant qu'on la traverse ;
//   - [data-scene="vue"] : section normale ; --p va de 0 (elle entre par le
//     bas) à 1 (elle sort par le haut) ;
//   - .apparait : se lève une fois, quand elle entre à l'écran ;
//   - .bouton : attiré par le pointeur (aimant léger) ;
//   - surScene(el, fn) : une page branche sa propre chorégraphie.
// Si l'on demande moins d'animations, rien ne bouge : tout est à l'état final.
// ============================================================================
import Lenis from 'lenis';

export const calme = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
document.documentElement.classList.toggle('anime', !calme);

export const borne = (v: number, a = 0, b = 1) => Math.min(b, Math.max(a, v));
export const plage = (p: number, a: number, b: number) => borne((p - a) / (b - a));
export const doux = (t: number) => 1 - Math.pow(1 - t, 3);

const lenis = calme ? null : new Lenis({ lerp: 0.085, smoothWheel: true });
export function allerA(cible: HTMLElement) {
  if (lenis) lenis.scrollTo(cible, { offset: -56 });
  else cible.scrollIntoView();
}
document.querySelectorAll<HTMLAnchorElement>('a[href^="#"]').forEach((a) => a.addEventListener('click', (e) => {
  const id = a.getAttribute('href')!;
  const cible = id.length > 1 ? document.querySelector<HTMLElement>(id) : null;
  if (cible) { e.preventDefault(); allerA(cible); }
}));

type Rappel = (p: number, el: HTMLElement) => void;
const rappels = new Map<HTMLElement, Rappel[]>();
export function surScene(el: HTMLElement | null, fn: Rappel) {
  if (!el) return;
  if (!el.dataset.scene) el.dataset.scene = 'vue';
  rappels.set(el, [...(rappels.get(el) ?? []), fn]);
}

// La ceinture qui se boucle : un clic (éclat + vibration) à chaque fermeture.
const fermes = new WeakMap<Element, boolean>();
export function boucler(logo: SVGElement | null, t: number) {
  if (!logo) return;
  logo.style.setProperty('--boucle', String(t));
  const ferme = t >= 0.999;
  if (ferme && !fermes.get(logo)) {
    logo.classList.remove('clic'); void logo.getBoundingClientRect(); logo.classList.add('clic');
    navigator.vibrate?.(12);
  }
  fermes.set(logo, ferme);
}

// Apparitions simples.
const io = new IntersectionObserver((entrees) => {
  for (const e of entrees) if (e.isIntersecting) { e.target.classList.add('est-visible'); io.unobserve(e.target); }
}, { rootMargin: '0px 0px -10% 0px' });
document.querySelectorAll('.apparait').forEach((el) => io.observe(el));

// Boutons aimantés (pointeur fin seulement).
if (!calme && matchMedia('(pointer: fine)').matches) {
  document.querySelectorAll<HTMLElement>('.bouton').forEach((b) => {
    b.addEventListener('pointermove', (e) => {
      const r = b.getBoundingClientRect();
      b.style.translate = `${((e.clientX - r.left) / r.width - 0.5) * 10}px ${((e.clientY - r.top) / r.height - 0.5) * 8}px`;
    });
    b.addEventListener('pointerleave', () => { b.style.translate = ''; });
  });
}

// Le pointeur, partagé (lumières qui suivent la souris).
window.addEventListener('pointermove', (e) => {
  document.documentElement.style.setProperty('--mx', `${e.clientX}px`);
  document.documentElement.style.setProperty('--my', `${e.clientY}px`);
}, { passive: true });

const barre = document.querySelector<HTMLElement>('.progression');
function image(temps: number) {
  lenis?.raf(temps);
  const vh = innerHeight;
  if (barre) barre.style.transform = `scaleX(${borne(scrollY / Math.max(1, document.documentElement.scrollHeight - vh))})`;
  document.querySelectorAll<HTMLElement>('[data-scene]').forEach((s) => {
    const r = s.getBoundingClientRect();
    if (r.bottom < -vh || r.top > 2 * vh) return;
    const p = s.dataset.scene === 'epingle'
      ? borne(-r.top / Math.max(1, r.height - vh))
      : borne((vh - r.top) / (vh + r.height));
    s.style.setProperty('--p', p.toFixed(4));
    rappels.get(s)?.forEach((fn) => fn(calme ? 1 : p, s));
  });
  requestAnimationFrame(image);
}
requestAnimationFrame(image);
