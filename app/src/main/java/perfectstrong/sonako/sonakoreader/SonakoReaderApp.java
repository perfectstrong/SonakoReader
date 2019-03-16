package perfectstrong.sonako.sonakoreader;

import android.app.Application;
import android.content.Context;

import androidx.preference.PreferenceManager;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabaseClient;

public class SonakoReaderApp extends Application {

    private static SonakoReaderApp instance;

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        // Global database for app
        LightNovelsDatabaseClient.getInstance(this);
    }
}
