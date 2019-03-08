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

import perfectstrong.sonako.sonakoreader.helper.Config;
import perfectstrong.sonako.sonakoreader.helper.PageLoader;
import perfectstrong.sonako.sonakoreader.helper.Utils;

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
                if (u.startsWith("file://" + Utils.getSaveLocationForTag(tag))) {
                    // Maybe this is an internal page, indicating a chapter
                    String newTitle = u.replace("file://" + Utils.getSaveLocationForTag(tag), "");
                    Log.d(TAG, "Opening internal link " + newTitle);
                    Intent i = new Intent(context, PageReadingActivity.class);
                    i.putExtra(Config.EXTRA_TITLE, newTitle);
                    i.putExtra(Config.EXTRA_TAG, tag);
                    context.startActivity(i);
                    return true;
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(i);
                }
                return true;
            }
        });

        new PageLoader(this, title, tag).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.page_reading_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_page:
                // TODO refresh page
                break;
            case R.id.action_purge_cache_page:
                // TODO purge cache
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
