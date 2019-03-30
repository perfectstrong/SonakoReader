package perfectstrong.sonako.sonakoreader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.InputStream;
import java.util.Calendar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import perfectstrong.sonako.sonakoreader.asyncTask.AsyncMassLinkDownloader;
import perfectstrong.sonako.sonakoreader.asyncTask.HistoryAsyncTask;
import perfectstrong.sonako.sonakoreader.database.Page;
import perfectstrong.sonako.sonakoreader.helper.Config;
import perfectstrong.sonako.sonakoreader.helper.Utils;
import perfectstrong.sonako.sonakoreader.service.PageDownloadService;

/**
 * General class for reading a page
 */
public class PageReadingActivity extends SonakoActivity {

    private static final String UNDEFINED = "Undefined";
    private static final String TAG = PageReadingActivity.class.getSimpleName();

    private String title;
    private String tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            title = bundle.getString(Config.EXTRA_TITLE);
            tag = bundle.getString(Config.EXTRA_TAG);
        }
        if (title == null) title = UNDEFINED;
        if (tag == null) tag = UNDEFINED;

        // New title to load
        title = Utils.removeSubtrait(title);
        tag = Utils.removeSubtrait(tag);
        setContentView(R.layout.activity_page_reading);
        setTitle(title);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        WebView pageview = findViewById(R.id.page_viewer);
        pageview.setInitialScale(1);
        pageview.setScrollContainer(false);
        WebSettings settings = pageview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        pageview.setWebViewClient(new PageReadingWebViewClient());

        // The file should be ready
        pageview.loadUrl(Utils.getFilepath(title, tag));

        // Register to history
        new HistoryAsyncTask.Register()
                .execute(new Page(
                        title,
                        tag,
                        Calendar.getInstance().getTime()
                ));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.page_reading_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_download_all_chapters:
                Log.d(TAG, "Download all chapters of " + title);
                new AsyncMassLinkDownloader(title, tag, this).execute();
                break;
            case R.id.action_refresh_text_and_images:
                Log.d(TAG, "Reset text and images of " + title);
                Utils.openOrDownload(title, tag, PageDownloadService.ACTION.REFRESH_ALL, this);
                break;
            case R.id.action_refresh_text:
                Log.d(TAG, "Refresh text of " + title);
                Utils.openOrDownload(title, tag, PageDownloadService.ACTION.REFRESH_TEXT, this);
                break;
            case R.id.action_refresh_missing_images:
                Log.d(TAG, "Refresh missing images of " + title);
                Utils.openOrDownload(title, tag, PageDownloadService.ACTION.REFRESH_MISSING_IMAGES, this);
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

    private class PageReadingWebViewClient extends WebViewClient {

        private final String LINK_PREFIX = "file://" + Utils.getSavDirForTag(tag);

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return shouldOverrideUrlLoading(view, request.getUrl().toString());
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String u = Uri.decode(url);
            Log.d(TAG, "Opening link " + u);
            Context context = view.getContext();
            if (u.startsWith(LINK_PREFIX) && u.contains("?title=")) {
                // Maybe this is an internal page, indicating a chapter
                String newTitle = u.substring(
                        u.lastIndexOf("?title=") + "?title=".length()
                );
                Log.d(TAG, "Opening internal link " + newTitle);
                Utils.openOrDownload(
                        newTitle,
                        tag,
                        null,
                        PageReadingActivity.this
                );
                return true;
            } else {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(i);
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            loadAssetIntoHead(view,
                    "style",
                    "text/css",
                    "css/" + Config.SKIN_BASE + ".css");
            loadAssetIntoHead(view,
                    "style",
                    "text/css",
                    "css/" + Utils.getCurrentSkin() + ".css");
            loadAssetIntoHead(view, "script", "text/javascript", "js/script.js");
        }

        private void loadAssetIntoHead(WebView view,
                                       String elementType,
                                       String srcType,
                                       String filepath) {
            try {
                InputStream inputStream = getAssets().open(filepath);
                byte[] buffer = new byte[inputStream.available()];
                //noinspection ResultOfMethodCallIgnored
                inputStream.read(buffer);
                inputStream.close();
                String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
                String js = "javascript:(function() {" +
                        "var parent = document.getElementsByTagName('head').item(0);" +
                        "var x = document.createElement('" + elementType + "');" +
                        "x.type = '" + srcType + "';" +
                        // Tell the browser to BASE64-decode the string into your script !!!
                        "x.innerHTML = window.atob('" + encoded + "');" +
                        "parent.appendChild(x)" +
                        "})()";
                if (Build.VERSION.SDK_INT >= 19) {
                    view.evaluateJavascript(js, null);
                } else {
                    view.loadUrl(js);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }
}
