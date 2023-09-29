export interface LottieSplashscreenPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
