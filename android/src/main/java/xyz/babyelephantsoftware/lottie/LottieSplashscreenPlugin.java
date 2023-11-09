package xyz.babyelephantsoftware.lottie;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebView;
import android.widget.ImageView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieCompositionFactory;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieListener;
import com.airbnb.lottie.LottieTask;
import com.airbnb.lottie.RenderMode;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.Locale;

@CapacitorPlugin(name = "LottieSplashscreen")
public class LottieSplashscreenPlugin extends Plugin {

    private LottieSplashscreen implementation = new LottieSplashscreen();
    private Dialog splashDialog;
    private LottieAnimationView animationView;
    private boolean animationEnded = false;
    private static final String LOG_TAG = "LottieSplashscreen";

    @Override
    public void load() {
        super.load();
        try {
            createView(null, null, null, null, null);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }
    }


    private String getAnimationLocation(String location) {
        String animationLocation = location;
        if (animationLocation == null || animationLocation.trim().isEmpty()) {
            animationLocation = getUIModeDependentPreference("LottieAnimationLocation", "");
        }
        return animationLocation;
    }

    private void createLottieComposition(
            boolean remoteEnabled,
            Context context,
            String animationLocation) {

        LottieTask<LottieComposition> comp;
        boolean cacheDisabled = getConfig().getBoolean("LottieCacheDisabled", false);

        if (remoteEnabled) {
            String cacheKey = cacheDisabled ? null : "url_" + animationLocation;
            comp = LottieCompositionFactory.fromUrl(context, animationLocation, cacheKey);
        } else {
            String cacheKey = cacheDisabled ? null : "asset_" + animationLocation;
            comp = LottieCompositionFactory.fromAsset(context, animationLocation, cacheKey);
            String imagesFolder = getConfig().getString("LottieImagesLocation",
                    animationLocation.substring(0, animationLocation.lastIndexOf('/')));
            animationView.setImageAssetsFolder(imagesFolder);
        }

        comp.addListener(new LottieListener<LottieComposition>() {
            @Override
            public void onResult(LottieComposition result) {
                animationView.setComposition(result);
            }
        }).addFailureListener(new LottieListener<Throwable>() {
            @Override
            public void onResult(Throwable result) {
                Log.e(LOG_TAG, "Animation not loadable!");
                Log.e(LOG_TAG, Log.getStackTraceString(result));
                destroyView(callbackContext);
                if (callbackContext != null) {
                    LottieSplashScreenInvalidURLException invalidURLException =
                            new LottieSplashScreenInvalidURLException("The provided animation is invalid");
                    callbackContext.error(invalidURLException.getMessage());
                }
            }
        });
    }

    private void configureAnimationView(Context context) {
        animationView.enableMergePathsForKitKatAndAbove(true);

        if (getConfig().getBoolean("LottieLoopAnimation", false)) {
            animationView.setRepeatCount(LottieDrawable.INFINITE);
        }

        String scaleTypeStr = getConfig().getString("LottieScaleType", "FIT_CENTER").toUpperCase(Locale.ENGLISH);
        ImageView.ScaleType scaleType = ImageView.ScaleType.valueOf(scaleTypeStr);
        animationView.setScaleType(scaleType);

        String backgroundColorStr = getUIModeDependentPreference("LottieBackgroundColor", "#ffffff");
        int color = ColorHelper.parseColor(backgroundColorStr);
        animationView.setBackgroundColor(color);

        boolean fullScreen = getConfig().getBoolean("LottieFullScreen", false);
        splashDialog = new Dialog(context, fullScreen ? android.R.style.Theme_NoTitleBar_Fullscreen : android.R.style.Theme_Translucent_NoTitleBar);
        splashDialog.getWindow().setBackgroundDrawable(new ColorDrawable(color));
        splashDialog.setContentView(animationView);
        splashDialog.setCancelable(false);
    }

    private void calculateAnimationSize(Double width, Double height) {
        boolean fullScreen = getConfig().getBoolean("LottieFullScreen", false);
        if (!fullScreen) {
            boolean relativeSize = getConfig().getBoolean("LottieRelativeSize", false);
            if (relativeSize) {
                DisplayMetrics metrics = webView.getContext().getResources().getDisplayMetrics();
                int animationWidth = (int) (metrics.widthPixels * (width != null ? width : getConfig().getDouble("LottieWidth", 0.2)));
                int animationHeight = (int) (metrics.heightPixels * (height != null ? height : getConfig().getDouble("LottieHeight", 0.2)));
                splashDialog.getWindow().setLayout(animationWidth, animationHeight);
            } else {
                splashDialog.getWindow().setLayout(
                        convertPixelsToDp(width != null ? width : getConfig().getDouble("LottieWidth", 200.0)),
                        convertPixelsToDp(height != null ? height : getConfig().getDouble("LottieHeight", 200.0))
                );
            }
        }
    }
    private void addAnimationListeners() {
        animationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                webView.evaluateJavascript("document.dispatchEvent(new Event('lottieAnimationStart'))", null);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                webView.evaluateJavascript("document.dispatchEvent(new Event('lottieAnimationEnd'))", null);
                boolean hideAfterAnimationDone = getConfig().getBoolean("LottieHideAfterAnimationEnd", false);
                if (hideAfterAnimationDone) {
                    dismissDialog();
                }
                animationEnded = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                webView.evaluateJavascript("document.dispatchEvent(new Event('lottieAnimationCancel'))", null);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                webView.evaluateJavascript("document.dispatchEvent(new Event('lottieAnimationRepeat'))", null);
            }
        });

    }

    private int convertPixelsToDp(double px) {
        return (int) (px * webView.getContext().getResources().getDisplayMetrics().density);
    }

    public void dismissDialog(final CallbackContext callbackContext) {
        int fadeDuration = getConfig().getInteger("LottieFadeOutDuration", 0);
        if (fadeDuration > 0) {
            AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
            fadeOut.setDuration((long) fadeDuration);
            animationView.setAnimation(fadeOut);
            animationView.startAnimation(fadeOut);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // Not implemented
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    splashDialog.dismiss();
                    if (callbackContext != null) {
                        callbackContext.success();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // Not implemented
                }
            });
        } else {
            splashDialog.dismiss();
            if (callbackContext != null) {
                callbackContext.success();
            }
        }
    }

        animationView.setOnClickListener(v -> {
        boolean cancelOnTap = getConfig().getBoolean("LottieCancelOnTap", false);
        if (cancelOnTap) {
            animationView.cancelAnimation();
            dismissDialog();
        }
    });

    // Helper method to convert dp to pixels
    private int convertPixelsToDp(double dp) {
        DisplayMetrics metrics = webView.getContext().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) dp, metrics);
    }

    private String getUIModeDependentPreference(String preferenceBaseName, String defaultValue) {
        if (defaultValue == null) {
            defaultValue = "";
        }

        Resources resources = capacitor.getContext().getResources();
        int nightModeFlags = resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean nightMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;

        String preferenceValue;
        if (nightMode) {
            preferenceValue = getConfig().getString(preferenceBaseName + "Dark", defaultValue);
        } else {
            preferenceValue = getConfig().getString(preferenceBaseName + "Light", defaultValue);
        }

        if (preferenceValue == null || preferenceValue.trim().isEmpty()) {
            preferenceValue = getConfig().getString(preferenceBaseName, defaultValue);
        }

        return preferenceValue;
    }


    @PluginMethod
    public void hide(PluginCall call) {
        getBridge().executeOnMainThread(new Runnable() {
            @Override
            public void run() {
                // Your code to hide the Lottie animation
                // Don't forget to call call.resolve() or call.reject() based on the outcome
            }
        });
    }

    private void destroyView(final PluginCall callbackContext) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                animationView.cancelAnimation();
                if (splashDialog != null && splashDialog.isShowing()) {
                    dismissDialog(callbackContext);
                }
            }
        });
    }


    private void createView(String location, Boolean remote, Double width, Double height, PluginCall callbackContext) throws LottieSplashScreenAnimationAlreadyPlayingException {
        if (splashDialog != null && splashDialog.isShowing()) {
            throw new LottieSplashScreenAnimationAlreadyPlayingException("An animation is already playing, please first hide the current one");
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!getActivity().isFinishing()) {
                    Context context = getBridge().getContext();

                    animationView = new LottieAnimationView(context);
                    boolean useHardwareAcceleration = remote != null ? remote : getConfig().getBoolean("LottieEnableHardwareAcceleration", false);
                    if (useHardwareAcceleration) {
                        animationView.setRenderMode(RenderMode.HARDWARE);
                    }

                    boolean remoteEnabled = remote != null ? remote : getConfig().getBoolean("LottieRemoteEnabled", false);
                    String animationLocation = getAnimationLocation(location);

                    if (animationLocation.isEmpty()) {
                        Log.e(LOG_TAG, "LottieAnimationLocation has to be configured!");
                        destroyView(null);
                        LottieSplashScreenInvalidURLException invalidURLException = new LottieSplashScreenInvalidURLException("The provided animation is invalid");
                        if (callbackContext != null) {
                            callbackContext.reject(invalidURLException.getMessage());
                            return;
                        } else {
                            throw invalidURLException;
                        }
                    }

                    createLottieComposition(remoteEnabled, context, animationLocation, callbackContext);
                    configureAnimationView(context);
                    calculateAnimationSize(width, height);
                    splashDialog.show();
                    addAnimationListeners();
                    animationView.playAnimation();
                    animationEnded = false;

                    int delay = getConfig().getInt("LottieHideTimeout", 0);
                    if (delay > 0) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dismissDialog(null);
                            }
                        }, delay);
                    }
                }
            }
        });
    }
    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    @PluginMethod
    public void show(PluginCall call) {
        String location = call.getString("location");
        Boolean remote = call.getBoolean("remote");
        Double width = call.getDouble("width");
        Double height = call.getDouble("height");
    }
}

