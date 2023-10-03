package xyz.babyelephantsoftware.lottie;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "LottieSplashscreen")
public class LottieSplashscreenPlugin extends Plugin {

    private LottieSplashscreen implementation = new LottieSplashscreen();

    @Override
    public void load() {
        try {
            implementation.createView();
        } catch (Exception e) {}
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
