package perfectstrong.sonako.sonakoreader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import perfectstrong.sonako.sonakoreader.helper.Config;

/**
 * General class for reading a page
 */
public class PageReadingActivity extends AppCompatActivity {

    private static final String TAG = PageReadingActivity.class.getSimpleName();
    protected static final String UNDEFINED = "Undefined";

    protected String title;
    protected String tag;
    protected WebView pageview = null;

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
        setContentView(R.layout.activity_page_reading);
        setTitle(title);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pageview = findViewById(R.id.page_viewer);
        pageview.setInitialScale(1);
        pageview.setScrollContainer(false);
        pageview.getSettings().setJavaScriptEnabled(false);
        pageview.getSettings().setLoadWithOverviewMode(true);
        pageview.getSettings().setUseWideViewPort(true);
        pageview.getSettings().setBuiltInZoomControls(true);
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
