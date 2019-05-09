package perfectstrong.sonako.sonakoreader.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Calendar;

import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.asyncTask.AsyncMassLinkDownloader;
import perfectstrong.sonako.sonakoreader.asyncTask.HistoryAsyncTask;
import perfectstrong.sonako.sonakoreader.component.MovableFloatingActionButton;
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
    private static final String LAST_INTENT_KEY = "lastIntentKey";
    private static final String SHOULD_RESTORE_HISTORY_KEY = "shouldRestoreHistoryKey";

    private String title;
    private String tag;
    private WebView pageview;
    private PageReadingWebViewClient webviewclient;
    private boolean shouldRestoreHistory;
    private View readingTools;
    private boolean isShowingReadingTools;
    private boolean onTextSearching = false;
    private boolean isShowingSearchBox = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView.enableSlowWholeDocumentDraw();
        }
        setContentView(R.layout.activity_page_reading);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupPageview();
        setupSearchBox();
        setupMovableFAB();
        if (savedInstanceState == null) {
            setTagAndTitle(getIntent());
            openPage();
        }
        readingTools = findViewById(R.id.reading_tools);
        isShowingReadingTools = false;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        shouldRestoreHistory = savedInstanceState.getBoolean(SHOULD_RESTORE_HISTORY_KEY);
        Intent lastIntent = savedInstanceState.getParcelable(LAST_INTENT_KEY);
        if (shouldRestoreHistory && lastIntent != null) {
            setTagAndTitle(lastIntent);
            pageview.restoreState(savedInstanceState);
        }
    }

    private void setupPageview() {
        pageview = findViewById(R.id.page_viewer);
        pageview.setInitialScale(1);
        pageview.setScrollContainer(false);
        WebSettings settings = pageview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setAppCachePath(this.getCacheDir().getAbsolutePath());
        webviewclient = new PageReadingWebViewClient(settings);
        pageview.setWebViewClient(webviewclient);
        pageview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    private void setupSearchBox() {
        // Background search box
        TypedValue typedValue = new TypedValue();
        TypedArray a = this.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
        int color = a.getColor(0, 0);
        a.recycle();
        findViewById(R.id.search_box).setBackgroundColor(color);

        // Search view
        final SearchView searchView = findViewById(R.id.search_view);
        searchView.setSubmitButtonEnabled(true);
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.isEmpty()) {
                    if (!onTextSearching) {
                        pageview.findAllAsync(query);
                        try {
                            //noinspection JavaReflectionMemberAccess
                            Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
                            m.invoke(pageview, true);
                        } catch (Throwable ignored) {
                        }
                        onTextSearching = true;
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    try {
                        //noinspection JavaReflectionMemberAccess
                        Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
                        m.invoke(pageview, false);
                    } catch (Throwable ignored) {
                    }
                    onTextSearching = false;
                }
                return true;
            }
        });
        searchView.setOnCloseListener(() -> {
            closeSearchBox();
            return false;
        });
    }

    private void setupMovableFAB() {
        MovableFloatingActionButton fab = findViewById(R.id.fab);
        CoordinatorLayout.LayoutParams lp = fab.getCoordinatorLayout();
        fab.setCoordinatorLayout(lp);
    }

    private void closeSearchBox() {
        onTextSearching = false;
        pageview.clearMatches();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.page_reading_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_backward:
                if (webviewclient != null)
                    webviewclient.assetsLoaded = false;
                pageview.goBack();
                break;
            case R.id.action_forward:
                if (webviewclient != null)
                    webviewclient.assetsLoaded = false;
                pageview.goForward();
                break;
            case R.id.action_return_home:
                Utils.goHome(this);
                break;
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setTagAndTitle(intent);
        openPage();
    }

    private void setTagAndTitle(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            title = bundle.getString(Config.EXTRA_TITLE);
            tag = bundle.getString(Config.EXTRA_TAG);
        }
        if (title == null) title = UNDEFINED;
        if (tag == null) tag = UNDEFINED;

        // New title to load
        title = Utils.removeSubtrait(title);
        tag = Utils.removeSubtrait(tag);
        setTitle(title);
    }

    private void openPage() {
        // The file should be ready
        webviewclient.assetsLoaded = false;
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
    public void recreate() {
        shouldRestoreHistory = true;
        super.recreate();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (shouldRestoreHistory) {
            pageview.saveState(outState);
            outState.putBoolean(SHOULD_RESTORE_HISTORY_KEY, true);
            Intent lastIntent = new Intent();
            lastIntent.putExtra(Config.EXTRA_TITLE, title);
            lastIntent.putExtra(Config.EXTRA_TAG, tag);
            outState.putParcelable(LAST_INTENT_KEY, lastIntent);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * slide the view from below itself to the current position
     *
     * @param view common view
     */
    private void slideUp(View view) {
        Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
        view.startAnimation(bottomUp);
        view.setVisibility(View.VISIBLE);
    }

    /**
     * Slide the view from its current position to below itself
     *
     * @param view common view
     */
    private void slideDown(View view) {
        Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
        view.startAnimation(bottomUp);
        view.setVisibility(View.GONE);
    }

    public void toggleReadingTools(View view) {
        if (isShowingReadingTools) {
            closeSearchBox();
            slideDown(readingTools);
        } else
            slideUp(readingTools);
        isShowingReadingTools = !isShowingReadingTools;
    }

    public void plusTextSize(View v) {
        // Delegate to webviewclient
        webviewclient.plusTextSize();
    }

    public void minusTextSize(View v) {
        // Delegate to webviewclient
        webviewclient.minusTextSize();
    }

    public void toggleSearchBox(View view) {
        if (isShowingSearchBox) {
            closeSearchBox();
            slideDown(findViewById(R.id.search_box));
        } else
            slideUp(findViewById(R.id.search_box));
        isShowingSearchBox = !isShowingSearchBox;
    }

    private class PageReadingWebViewClient extends WebViewClient {

        private final WebSettings settings;
        private boolean assetsLoaded = false;

        PageReadingWebViewClient(WebSettings settings) {
            super();
            this.settings = settings;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return shouldOverrideUrlLoading(view, request.getUrl().toString());
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String u = Uri.decode(url);
            Log.d(TAG, "Opening link " + u);
            if (u.startsWith("file://" + Utils.getSavDirForTag(tag)) && u.contains("?title=")) {
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
                Utils.viewExternalLink(PageReadingActivity.this, url);
            }
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (!assetsLoaded) {
                loadAssetIntoHead(view,
                        "style",
                        "text/css",
                        "css/" + Config.SKIN_BASE + ".css");
                loadAssetIntoHead(view,
                        "style",
                        "text/css",
                        "css/" + Utils.getCurrentSkin() + ".css");
                loadAssetIntoHead(view, "script", "text/javascript", "js/script.js");
                assetsLoaded = true;
            }
        }

        private void loadAssetIntoHead(WebView view,
                                       String elementType,
                                       String srcType,
                                       String filepath) {
            Log.d(TAG, "Loading asset " + filepath + " into head");
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
                executeJS(view, js);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        private void executeJS(WebView view,
                               String js) {
            view.evaluateJavascript(js, null);
        }

        /**
         * Percentage
         */
        private static final int MAX_ZOOM = 500;
        private static final int MIN_ZOOM = 20;
        private static final int STEP_ZOOM = 20;

        void minusTextSize() {
            if (settings.getTextZoom() - STEP_ZOOM >= MIN_ZOOM)
                settings.setTextZoom(settings.getTextZoom() - STEP_ZOOM);
        }

        void plusTextSize() {
            if (settings.getTextZoom() + STEP_ZOOM <= MAX_ZOOM)
                settings.setTextZoom(settings.getTextZoom() + STEP_ZOOM);
        }
    }
}
