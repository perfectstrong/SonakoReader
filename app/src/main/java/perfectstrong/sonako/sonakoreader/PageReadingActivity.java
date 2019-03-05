package perfectstrong.sonako.sonakoreader;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;

import perfectstrong.sonako.sonakoreader.helper.Config;

/**
 * General class for reading a page
 */
@SuppressLint("StaticFieldLeak")
public class PageReadingActivity extends AppCompatActivity {

    protected String title = "";
    protected WebView pageview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            title = bundle.getString(Config.EXTRA_TITLE);
        }

        // New title to load
        setContentView(R.layout.activity_page_reading);
        setTitle(title);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pageview = findViewById(R.id.page_viewer);
        pageview.setInitialScale(1);
        pageview.setScrollContainer(false);
//        pageview.setOnTouchListener((v, event) -> (event.getAction() == MotionEvent.ACTION_MOVE));
        pageview.getSettings().setJavaScriptEnabled(false);
        pageview.getSettings().setLoadWithOverviewMode(true);
        pageview.getSettings().setUseWideViewPort(true);
        pageview.getSettings().setBuiltInZoomControls(true);
    }
}
