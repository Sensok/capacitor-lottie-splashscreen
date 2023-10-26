import { WebPlugin } from '@capacitor/core';

import type { LottieSplashscreenPlugin } from './definitions';

export class LottieSplashscreenWeb
  extends WebPlugin
  implements LottieSplashscreenPlugin
{
  animationEnded: boolean = false;

  async hide(): Promise<string> {
    this.animationEnded = true;
    throw new Error('Method not implemented.');
  }
  async show(
    _location?: string | undefined,
    _remote?: boolean | undefined,
    _width?: number | undefined,
    _height?: number | undefined,
  ): Promise<string> {
    throw new Error('Method not implemented.');
  }
  async on(
    _event:
      | 'lottieAnimationStart'
      | 'lottieAnimationEnd'
      | 'lottieAnimationCancel'
      | 'lottieAnimationRepeat',
    _callback: (ev: Event) => void,
  ): Promise<unknown> {
    throw new Error('Method not implemented.');
  }
  async once(
    _event:
      | 'lottieAnimationStart'
      | 'lottieAnimationEnd'
      | 'lottieAnimationCancel'
      | 'lottieAnimationRepeat',
  ): Promise<unknown> {
    throw new Error('Method not implemented.');
  }
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
