package perfectstrong.sonako.sonakoreader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Calendar;

import perfectstrong.sonako.sonakoreader.asyncTask.HistoryAsyncTask;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabaseClient;
import perfectstrong.sonako.sonakoreader.database.Page;
import perfectstrong.sonako.sonakoreader.helper.Config;
import perfectstrong.sonako.sonakoreader.helper.Utils;
import perfectstrong.sonako.sonakoreader.service.PageDownloadService;

/**
 * General class for reading a page
 */
public class PageReadingActivity extends AppCompatActivity {

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
        pageview.getSettings().setJavaScriptEnabled(false);
        pageview.getSettings().setLoadWithOverviewMode(true);
        pageview.getSettings().setUseWideViewPort(true);
        pageview.getSettings().setBuiltInZoomControls(true);
        pageview.setWebViewClient(new WebViewClient() {

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
                if (u.startsWith(LINK_PREFIX)) {
                    // Maybe this is an internal page, indicating a chapter
                    String newTitle = u.replace(LINK_PREFIX, "")
                            .replace(".html", "");
                    Log.d(TAG, "Opening internal link " + newTitle);
                    Utils.openOrDownload(
                            context,
                            newTitle,
                            tag,
                            null
                    );
                    return true;
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(i);
                }
                return true;
            }
        });

        // The file should be ready
        pageview.loadUrl(Utils.getFilepath(title, tag));

        // Register to history
        new HistoryAsyncTask.Register(LightNovelsDatabaseClient.getInstance(this))
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
            case R.id.action_purge_cache_page:
                Log.d(TAG, "Reset " + title);
                Utils.openOrDownload(this, title, tag, PageDownloadService.ACTION.REFRESH_ALL);
                break;
            case R.id.action_refresh_page:
                Log.d(TAG, "Refresh text " + title);
                Utils.openOrDownload(this, title, tag, PageDownloadService.ACTION.REFRESH_TEXT);
                break;
            case R.id.action_settings:
                // TODO change settings
                break;
            default:
                break;
        }
        return true;
    }
}
