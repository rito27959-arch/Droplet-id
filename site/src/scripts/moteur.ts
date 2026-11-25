// ============================================================================
// LE MOTEUR DU SITE — Droplet Design Language.
//
// Les scènes épinglées, le défilement doux, les boutons aimantés et la barre
// de progression sont retirés : le contenu est posé, à son état final. Les
// pages qui branchaient une chorégraphie (surScene) reçoivent toujours p = 1,
// une fois, pour afficher ce que la scène montrait à la fin.
// La seule animation forte du site est la démonstration de l'accueil, en CSS.
// ============================================================================

export const calme = true;

export const borne = (v: number, a = 0, b = 1) => Math.min(b, Math.max(a, v));
export const plage = (p: number, a: number, b: number) => borne((p - a) / (b - a));
export const doux = (t: number) => 1 - Math.pow(1 - t, 3);

export function allerA(cible: HTMLElement) {
  cible.scrollIntoView();
}

type Rappel = (p: number, el: HTMLElement) => void;
export function surScene(el: HTMLElement | null, fn: Rappel) {
  if (!el) return;
  el.style.setProperty('--p', '1');
  fn(1, el);
}

/** Le logo, toujours bouclé. */
export function boucler(logo: SVGElement | null, _t: number) {
  logo?.style.setProperty('--boucle', '1');
}
