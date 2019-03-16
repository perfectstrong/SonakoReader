package perfectstrong.sonako.sonakoreader;

import android.app.Application;
import android.content.Context;

public class SonakoReaderApp extends Application {

    private static SonakoReaderApp instance;

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }
}
