import { WebPlugin } from '@capacitor/core';

import type { LottieSplashscreenPlugin } from './definitions';

export class LottieSplashscreenWeb
  extends WebPlugin
  implements LottieSplashscreenPlugin
{
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
