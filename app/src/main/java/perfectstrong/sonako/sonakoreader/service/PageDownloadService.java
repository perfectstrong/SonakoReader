package perfectstrong.sonako.sonakoreader.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.Objects;

import fastily.jwiki.core.Wiki;
import okhttp3.HttpUrl;
import perfectstrong.sonako.sonakoreader.helper.Config;

/**
 * Download a page given its title and tag
 */
public class PageDownloadService extends IntentService {

    private static final String TAG = PageDownloadService.class.getSimpleName();
    private Wiki wiki;
    private String title;
    private Exception exception;

    public PageDownloadService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            wiki = new Wiki(Objects.requireNonNull(HttpUrl.parse(Config.API_ENDPOINT)));
            if (!wiki.exists(title))
                throw new IllegalArgumentException(title + " không tồn tại.");
            fetch();
        } catch (Exception e) {
            Log.e(TAG, "Unknown exception", e);
            exception = e;
        }
    }

    private void fetch() {
    }
}
