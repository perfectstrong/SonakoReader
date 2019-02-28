package perfectstrong.sonako.sonakoreader;

import android.app.Application;

import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabase;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabaseClient;

public class SonakoReaderApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Global database for app
        LightNovelsDatabaseClient.getInstance(this);
    }
}
