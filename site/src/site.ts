// Les liens partagés par tout le site.
export const APK = 'https://github.com/rito27959-arch/Droplet-id/raw/telechargements/droplet-id.apk';
export const APK_ANCIENS = 'https://github.com/rito27959-arch/Droplet-id/raw/telechargements/droplet-id-anciens-telephones.apk';
export const SOURCES = 'https://github.com/rito27959-arch/Droplet-id';
export const SIGNALER = 'https://github.com/rito27959-arch/Droplet-id/issues';

const base = import.meta.env.BASE_URL.replace(/\/$/, '');
/** Un chemin du site, qui tient compte d'un éventuel sous-dossier. */
export const lien = (chemin: string) => `${base}${chemin}`;
