import { registerPlugin } from '@capacitor/core';

import type { LottieSplashscreenPlugin } from './definitions';

const LottieSplashscreen = registerPlugin<LottieSplashscreenPlugin>(
  'LottieSplashscreen',
  {
    web: () => import('./web').then(m => new m.LottieSplashscreenWeb()),
  },
);

export * from './definitions';
export { LottieSplashscreen };
