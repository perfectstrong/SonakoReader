package perfectstrong.sonako.sonakoreader.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.ViewCompat;
import androidx.preference.PreferenceManager;

import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.helper.Config;
import perfectstrong.sonako.sonakoreader.helper.Utils;
import perfectstrong.sonako.sonakoreader.service.PageDownloadService;

public class DumbActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            // Implicit action from external app
            onViewIntent(intent);
        } else if (action == null || action.isEmpty()) {
            Bundle extras = intent.getExtras();
            if (extras == null) return;
            String extraAction = extras.getString(Config.EXTRA_ACTION);
            // Explicit intent to download
            if (PageDownloadService.DOWNLOAD.equals(extraAction)) onDownloadIntent(intent);
        }
    }

    private void onViewIntent(Intent intent) {
        Uri data = intent.getData();
        assert data != null;
        String title = data.getLastPathSegment();
        if (title == null || title.isEmpty())
            return;
        title = title.replace("_", Utils.REPLACEMENT_CHAR);
        // Check existence
        // We try to check in save dir
        String tag = Utils.getCachedTagFor(title);
        // If tag is still null, no cached has been saved
        // Or if title has not been created
        if (tag == null || Utils.isNotCached(title, tag)) {
            if (PreferenceManager.getDefaultSharedPreferences(this)
                    .getBoolean(
                            this.getString(R.string.key_pref_download_noncached_pages),
                            this.getResources().getBoolean(R.bool.default_download_noncached_pages)
                    ))
                Utils.startDownloadTask(this, title, tag, null);
            else {
                // Create push notification to download
                Intent downloadIntent = new Intent(this, DumbActivity.class);
                downloadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                downloadIntent.putExtra(Config.EXTRA_TITLE, title);
                downloadIntent.putExtra(Config.EXTRA_TAG, tag);
                downloadIntent.putExtra(Config.EXTRA_ACTION, PageDownloadService.DOWNLOAD);
                int intentId = ViewCompat.generateViewId();
                downloadIntent.putExtra(Config.EXTRA_ID, intentId);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Config.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_sonakoreaderlogo_bw)
                        .setColor(getResources().getColor(R.color.holySecondaryColor))
                        .setContentTitle(title)
                        .setContentText(getString(R.string.wanna_download))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(PendingIntent.getActivity(this, 0, downloadIntent, 0))
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(intentId, builder.build());
            }
        } else
            Utils.startReadingActivity(this, title, tag);
    }

    private void onDownloadIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) return;
        String title = extras.getString(Config.EXTRA_TITLE);
        String tag = extras.getString(Config.EXTRA_TAG);
        Utils.startDownloadTask(this, title, tag, null);
    }
}