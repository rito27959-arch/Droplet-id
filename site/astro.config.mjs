// @ts-check
// Le site de Droplet ID, à part du site de Droplet.
import { defineConfig } from 'astro/config';

export default defineConfig({
  site: process.env.SITE_URL || 'https://dropletid.dropletmesh.app',
  base: process.env.BASE_PATH || '/',
});
