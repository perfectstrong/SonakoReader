package perfectstrong.sonako.sonakoreader;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import perfectstrong.sonako.sonakoreader.helper.Config;
import perfectstrong.sonako.sonakoreader.helper.Utils;

public class SonakoReaderApp extends Application {

    private static SonakoReaderApp instance;

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        instance = this;
        Utils.updateTheme(this);
        Utils.hideImagesFromGallery(this);
        createDownloaderNotificationChannel();
        super.onCreate();
    }

    private void createDownloaderNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = Config.APP_NAME + " Downloader";
            String description = getString(R.string.downloader_notification_channel_description);
            NotificationChannel channel = new NotificationChannel(Config.CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
