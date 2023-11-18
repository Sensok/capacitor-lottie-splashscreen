package xyz.babyelephantsoftware.lottie;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebView;
import android.widget.FrameLayout;
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
import java.util.ArrayList;
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
        String animationLocation,
        final PluginCall callbackContext
    ) {
        LottieTask<LottieComposition> comp;
        boolean cacheDisabled = getConfig().getBoolean("LottieCacheDisabled", false);

        if (remoteEnabled) {
            String cacheKey = cacheDisabled ? null : "url_" + animationLocation;
            comp = LottieCompositionFactory.fromUrl(context, animationLocation, cacheKey);
        } else {
            String cacheKey = cacheDisabled ? null : "asset_" + animationLocation;
            comp = LottieCompositionFactory.fromAsset(context, animationLocation, cacheKey);
            String imagesFolder = getConfig()
                .getString("LottieImagesLocation", animationLocation.substring(0, animationLocation.lastIndexOf('/')));
            animationView.setImageAssetsFolder(imagesFolder);
        }

        comp
            .addListener(
                new LottieListener<LottieComposition>() {
                    @Override
                    public void onResult(LottieComposition result) {
                        animationView.setComposition(result);
                    }
                }
            )
            .addFailureListener(
                new LottieListener<Throwable>() {
                    @Override
                    public void onResult(Throwable result) {
                        Log.e(LOG_TAG, "Animation not loadable!");
                        Log.e(LOG_TAG, Log.getStackTraceString(result));
                        destroyView(callbackContext);
                        if (callbackContext != null) {
                            LottieSplashScreenInvalidURLException invalidURLException = new LottieSplashScreenInvalidURLException(
                                "The provided animation is invalid"
                            );
                            callbackContext.error(invalidURLException.getMessage());
                        }
                    }
                }
            );
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
        animationView.setApplyingOpacityToLayersEnabled(true);
        //        Log.e(LOG_TAG,animationView.);
        boolean fullScreen = getConfig().getBoolean("LottieFullScreen", false);
        splashDialog =
            new Dialog(context, fullScreen ? android.R.style.Theme_NoTitleBar_Fullscreen : android.R.style.Theme_Translucent_NoTitleBar);
        splashDialog.getWindow().setBackgroundDrawable(new ColorDrawable(color));
        splashDialog.setContentView(animationView);
        splashDialog.setCancelable(false);
    }

    private void calculateAnimationSize(Double width, Double height) {
        boolean fullScreen = getConfig().getBoolean("LottieFullScreen", false);
        if (!fullScreen) {
            boolean relativeSize = getConfig().getBoolean("LottieRelativeSize", false);
            if (relativeSize) {
                DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
                int animationWidth = (int) (
                    metrics.widthPixels * (width != null ? width : Double.parseDouble(getConfig().getString("LottieWidth", "0.2")))
                );
                int animationHeight = (int) (
                    metrics.heightPixels * (height != null ? height : Double.parseDouble(getConfig().getString("LottieHeight", "0.2")))
                );
                splashDialog.getWindow().setLayout(animationWidth, animationHeight);
            } else {
                splashDialog
                    .getWindow()
                    .setLayout(
                        convertPixelsToDp(width != null ? width : Double.parseDouble(getConfig().getString("LottieWidth", "200.0"))),
                        convertPixelsToDp(height != null ? height : Double.parseDouble(getConfig().getString("LottieHeight", "200.0")))
                    );
            }
        }
    }

    private void addAnimationListeners(final PluginCall callbackContext) {
        animationView.addAnimatorListener(
            new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    notifyListeners("lottieAnimationStart", null);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    notifyListeners("lottieAnimationEnd", null);
                    boolean hideAfterAnimationDone = getConfig().getBoolean("LottieHideAfterAnimationEnd", false);
                    if (hideAfterAnimationDone) {
                        dismissDialog(callbackContext);
                    }
                    animationEnded = true;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    notifyListeners("lottieAnimationCancel", null);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    notifyListeners("lottieAnimationRepeat", null);
                }
            }
        );
    }

    public void dismissDialog(final PluginCall callbackContext) {
        int fadeDuration = getConfig().getInt("LottieFadeOutDuration", 0);

        if (fadeDuration > 0) {
            AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
            fadeOut.setDuration((long) fadeDuration);
            animationView.setAnimation(fadeOut);
            animationView.startAnimation(fadeOut);
            fadeOut.setAnimationListener(
                new Animation.AnimationListener() {
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
                }
            );
        } else {
            ArrayList<String> warnings = animationView.getComposition().getWarnings();
            Log.e(LOG_TAG, "THIS IS A TEST TO SEE********************");
            warnings.forEach(w -> Log.e(LOG_TAG, w));
            Log.e(LOG_TAG, "*****************************************");
            splashDialog.dismiss();
            if (callbackContext != null) {
                callbackContext.success();
            }
        }
    }

    // Helper method to convert dp to pixels
    private int convertPixelsToDp(double dp) {
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) dp, metrics);
    }

    private String getUIModeDependentPreference(String preferenceBaseName, String defaultValue) {
        if (defaultValue == null) {
            defaultValue = "";
        }

        Resources resources = getContext().getResources();
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

    private void destroyView(final PluginCall callbackContext) {
        getActivity()
            .runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        animationView.cancelAnimation();
                        if (splashDialog != null && splashDialog.isShowing()) {
                            dismissDialog(callbackContext);
                        }
                    }
                }
            );
    }

    private void createView(String location, Boolean remote, Double width, Double height, PluginCall callbackContext)
        throws LottieSplashScreenAnimationAlreadyPlayingException, LottieSplashScreenInvalidURLException {
        if (splashDialog != null && splashDialog.isShowing()) {
            throw new LottieSplashScreenAnimationAlreadyPlayingException(
                "An animation is already playing, please first hide the current one"
            );
        }
        getActivity()
            .runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        if (!getActivity().isFinishing()) {
                            Context context = getBridge().getContext();

                            animationView = new LottieAnimationView(context);
                            boolean useHardwareAcceleration = remote != null
                                ? remote
                                : getConfig().getBoolean("LottieEnableHardwareAcceleration", false);
                            if (useHardwareAcceleration) {
                                animationView.setRenderMode(RenderMode.HARDWARE);
                            }

                            boolean remoteEnabled = remote != null ? remote : getConfig().getBoolean("LottieRemoteEnabled", false);
                            String animationLocation = getAnimationLocation(location);

                            if (animationLocation.isEmpty()) {
                                Log.e(LOG_TAG, "LottieAnimationLocation has to be configured!");
                                destroyView(null);
                                String exceptionMessage = "The provided animation is invalid";
                                callbackContext.reject(exceptionMessage);
                                return;
                            }

                            createLottieComposition(remoteEnabled, context, animationLocation, callbackContext);
                            configureAnimationView(context);
                            calculateAnimationSize(width, height);
                            splashDialog.show();
                            addAnimationListeners(callbackContext);
                            animationView.playAnimation();
                            animationEnded = false;

                            int delay = getConfig().getInt("LottieHideTimeout", 0);
                            if (delay > 0) {
                                new Handler(Looper.getMainLooper())
                                    .postDelayed(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                dismissDialog(null);
                                            }
                                        },
                                        delay
                                    );
                            }
                        }
                    }
                }
            );
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
