import '@capacitor/cli';

type LottieEvent =
  | 'lottieAnimationStart'
  | 'lottieAnimationEnd'
  | 'lottieAnimationCancel'
  | 'lottieAnimationRepeat';

export interface LottieSplashscreenPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  readonly animationEnded: boolean;
  hide(): Promise<string>;
  show(
    location?: string,
    remote?: boolean,
    width?: number,
    height?: number,
  ): Promise<string>;
  on(event: LottieEvent, callback: (ev: Event) => void): void;
  once(event: LottieEvent): Promise<unknown>;
}

declare const lottie: {
  splashscreen: LottieSplashscreenPlugin;
};

/// <reference types="@capacitor/cli" />
declare module '@capacitor/cli' {
  export interface PluginsConfig {
    LottieSplashscreen?: {
      /**
       *  Toggles Lottie's remote mode which allows files to be downloaded/displayed from URLs
       *
       * @since 1.0.0
       * @default false
       * @example true
       */
      LottieRemoteEnabled?: boolean;

      /**
       * Location of the Lottie JSON file that should be loaded in light mode.
       * Can either be a URL (if LottieRemoteEnabled is true) or a local JSON or ZIP file (e.g. www/lottie/error.json)
       *
       * @since 1.0.0
       * @default ""
       * @example "#FF9900"
       */
      LottieAnimationLocationLight?: string;

      /**
       * Android only! Scale type of the view. Can be one of the following: https://developer.android.com/reference/android/widget/ImageView.ScaleType
       *
       * @since 1.0.0
       * @default FIT_CENTER
       * @example "FIT_CENTER"
       */
      LottieScaleType?: string;

      /**
       * Location of the Lottie JSON file that should be loaded in dark mode.
       * Can either be a URL (if LottieRemoteEnabled is true) or a local JSON or ZIP file (e.g. www/lottie/error.json).
       *
       * @since 1.0.0
       * @default ""
       * @example "#FF9900"
       */
      LottieAnimationLocationDark?: string;

      /**
       * Location of the Lottie JSON file that should be loaded as a fallback if there are no dark or light mode animations defined or if one of them is an invalid location.
       * Can either be a URL (if LottieRemoteEnabled is true) or a local JSON or ZIP file (e.g. www/lottie/error.json)
       *
       * @since 1.0.0
       * @default ""
       * @example "#FF9900"
       */
      LottieAnimationLocation?: string;

      /**
       * Android only! Location of the Lottie images folder specified by the JSON.
       *
       * @since 1.0.0
       * @default ""
       * @example "#FF9900"
       */
      LottieImagesLocation?: string;

      /**
       * Immediately cancels the Lottie animation when the user taps on the screen.
       *
       * @since 1.0.0
       * @default false
       * @example false
       */
      LottieCancelOnTap?: boolean;

      /**
       * Duration after which the Lottie animation should be hidden.
       * CAUTION: iOS reads this value in SECONDS, but e.g., 0.5 is supported. Android reads this value in MILLISECONDS!
       *
       * @since 1.0.0
       * @default 0
       * @example 0.2
       */
      LottieHideTimeout?: number;

      /**
       * Background color of the overlay in light.
       * Can be used with alpha values, too. (For more information see the 8 digits notation of RGB notation)
       *
       * @since 1.0.0
       * @default #ffffff
       * @example "#FF9900"
       */
      LottieBackgroundColorLight?: string;

      /**
       * Background color of the overlay in dark mode.
       * Can be used with alpha values, too. (For more information see the 8 digits notation of RGB notation)
       *
       * @since 1.0.0
       * @default #ffffff
       * @example "#FF9900"
       */
      LottieBackgroundColorDark?: string;

      /**
       * Background color of the overlay as a fallback if there are no dark or light mode colors defined.
       * Can be used with alpha values, too. (For more information see the 8 digits notation of RGB notation)
       *
       * @since 1.0.0
       * @default #00000
       * @example "#FF9900"
       */
      LottieBackgroundColor?: string;

      /**
       * Width of the container that's rendering the Lottie animation
       *
       * @since 1.0.0
       * @default 200
       * @example 200
       */
      lottieWidth?: number;

      /**
       * Height of the container that's rendering the Lottie animation
       *
       * @since 1.0.0
       * @default 200
       * @example 200
       */
      LottieHeight?: number;

      /**
       * Uses width and height values as relative values. Specify them as e.g. 0.3 to have 30%.
       *
       * @since 1.0.0
       * @default false
       * @example true
       */
      LottieRelativeSize?: boolean;

      /**
       * Renders the animation in full screen. Ignores properties above.
       * @since 1.0.0
       * @default false
       * @example true
       */
      LottieFullScreen?: boolean;

      /**
       * Loops the animation
       *
       * @since 1.0.0
       * @default false
       * @example true
       */
      LottieLoopAnimation?: boolean;

      /**
       * Hides the Lottie splash screen when the pageDidLoad event fired
       *
       * @since 1.0.0
       * @default false
       * @example true
       */
      LottieAutoHideSplashScreen?: boolean;

      /**
       * Android only! Enables hardware acceleration for the animation view.
       * Not really recommended since Lottie decides automatically whether the hardware mode should be used or not.
       *
       * @since 1.0.0
       * @default false
       * @example true
       */
      LottieEnableHardwareAcceleration?: boolean;
      /**
       * Duration for the fade out animation.
       * CAUTION: iOS reads this value in SECONDS, but e.g., 0.5 is supported. Android reads this value in MILLISECONDS! the Set to 0 disable the fade out animation.
       *
       * @since 1.0.0
       * @default 0
       * @example 0
       */
      LottieFadeOutDuration?: number;

      /**
       * Hides the Lottie splash screen after the animation has been played. Do not use together with LottieAutoHideSplashScreen or LottieLoopAnimation
       *
       * @since 1.0.0
       * @default false
       * @example true
       */
      LottieHideAfterAnimationEnd?: boolean;

      /**
       * Disables caching of animations
       *
       * @since 1.0.0
       * @default false
       * @example true
       */
      LottieCacheDisabled?: boolean;

      /**
       * Enables Merged Paths. If your lottie file is not rendering correctly, try set this to true.
       *
       * @since 1.0.0
       * @default false
       * @example true
       */
      LottieEnableMergePaths?: boolean;
    };
  }
}
