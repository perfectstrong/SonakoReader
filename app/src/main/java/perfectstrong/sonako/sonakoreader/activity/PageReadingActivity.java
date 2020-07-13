package perfectstrong.sonako.sonakoreader.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.asyncTask.AsyncMassLinkDownloader;
import perfectstrong.sonako.sonakoreader.asyncTask.HistoryAsyncTask;
import perfectstrong.sonako.sonakoreader.component.PageReadingWebView;
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
    private static final long MAX_TOUCH_DURATION = 200;
    private static final float MAX_Y_FLING = 100;
    private static boolean onImmersiveLayout = true;

    private String title;
    private String tag;
    private PageReadingWebView pageViewer;
    private PageReadingWebViewClient webViewClient;
    private View readingTools;
    private Toolbar toolbar;
    private boolean onTextSearching = false;
    private boolean isShowingSearchBox = false;
    private final List<String> headers = new ArrayList<>();
    private final Map<String, String> headersId = new HashMap<>();
    private AlertDialog tocDialog;
    private long m_DownTime;
    private float m_Y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView.enableSlowWholeDocumentDraw();
        }
        setContentView(R.layout.activity_page_reading);
        readingTools = findViewById(R.id.reading_tools);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupPageViewer();
        setupSearchBox();
        if (!onImmersiveLayout) toggleImmersiveLayout(pageViewer);
        if (savedInstanceState == null) {
            setTagAndTitle(getIntent());
            openPage();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateResources();
    }

    private void updateResources() {
        if (pageViewer == null || webViewClient == null) return;
        // Reload custom fonts
        webViewClient.loadCustomFont();
    }

    private void setupPageViewer() {
        pageViewer = findViewById(R.id.page_viewer);
        pageViewer.setOnTouchCallback(this::onTouchHandler);
        pageViewer.setInitialScale(1);
        pageViewer.setScrollContainer(false);
        WebSettings settings = pageViewer.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setAppCachePath(this.getCacheDir().getAbsolutePath());
        webViewClient = new PageReadingWebViewClient(settings);
        pageViewer.setWebViewClient(webViewClient);
        pageViewer.setWebChromeClient(new PageReadingWebChromeClient());
        pageViewer.setOnScrollCallback((l, t, oldl, oldt) -> webViewClient.saveCurrentReadingPosition());
        pageViewer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
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
                        pageViewer.findAllAsync(query);
                        try {
                            //noinspection JavaReflectionMemberAccess
                            Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
                            m.invoke(pageViewer, true);
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
                        m.invoke(pageViewer, false);
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

    private void closeSearchBox() {
        onTextSearching = false;
        pageViewer.clearMatches();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.page_reading_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_return_home:
                Utils.goHome(this);
                break;
            case R.id.action_reload:
                Intent currentIntent = getIntent();
                finish();
                startActivity(currentIntent);
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
        webViewClient.assetsLoaded = false;
        pageViewer.loadUrl(Utils.getFilepath(title, tag));
    }


    public void toggleReadingTools(View view) {
        if (onImmersiveLayout) {
            closeSearchBox();
            Utils.slideOut(readingTools, R.anim.bottom_down);
        } else
            Utils.slideIn(readingTools, R.anim.bottom_up);
    }

    public void plusTextSize(View v) {
        // Delegate to webviewclient
        webViewClient.plusTextSize();
    }

    public void minusTextSize(View v) {
        // Delegate to webviewclient
        webViewClient.minusTextSize();
    }

    public void toggleSearchBox(View view) {
        if (isShowingSearchBox) {
            closeSearchBox();
            Utils.slideOut(findViewById(R.id.search_box), R.anim.bottom_down);
        } else
            Utils.slideIn(findViewById(R.id.search_box), R.anim.bottom_up);
        isShowingSearchBox = !isShowingSearchBox;
    }

    private void buildTOCDialog() {
        tocDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.toc_title)
                .setCancelable(true)
                .setItems(headers.toArray(new String[0]), (dialog, which) ->
                        webViewClient.scrollToId(headersId.get(headers.get(which)))
                )
                .setNegativeButton(R.string.no, null)
                .create();
    }

    public void showTOCDialog(View view) {
        if (tocDialog != null) {
            tocDialog.show();
            // Set height
            Window window = tocDialog.getWindow();
            assert window != null;
            // Get screen width and height in pixels
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            // The absolute height of the available display size in pixels.
            int displayHeight = displayMetrics.heightPixels;

            // Initialize a new window manager layout parameters
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            // Copy the alert dialog window attributes to new layout parameter instance
            layoutParams.copyFrom(window.getAttributes());

            // Set alert dialog max height equal to screen height 70%
            if (layoutParams.height > displayHeight * 0.7f) {
                layoutParams.height = (int) (displayHeight * 0.7f);
                // Apply the newly created layout parameters to the alert dialog window
                tocDialog.getWindow().setAttributes(layoutParams);
            }
        } else
            Toast.makeText(
                    this,
                    R.string.please_wait,
                    Toast.LENGTH_SHORT
            ).show();
    }

    private void onTouchHandler(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                m_DownTime = event.getEventTime(); //init time
                m_Y = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                if (event.getEventTime() - m_DownTime <= MAX_TOUCH_DURATION
                        && Math.abs(event.getY() - m_Y) <= MAX_Y_FLING)
                    // On touch action
                    this.toggleImmersiveLayout(pageViewer);
                break;
            default:
                break; //No-Op
        }
    }

    private void toggleImmersiveLayout(View v) {
        onImmersiveLayout = !onImmersiveLayout;
        if (onImmersiveLayout) {
            // Close search box
            if (isShowingSearchBox)
                toggleSearchBox(v);
            // Close reading tool
            toggleReadingTools(v);
            // Hide toolbar
            Utils.slideOut(toolbar, R.anim.top_up);
            hideSystemUI();
        } else {
            Utils.slideIn(toolbar, R.anim.top_down);
            toggleReadingTools(v);
        }
    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        onImmersiveLayout = false;
        toggleImmersiveLayout(pageViewer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onImmersiveLayout = false;
        toggleImmersiveLayout(pageViewer);
    }

    public class PageReadingWebViewClient extends WebViewClient {

        private final WebSettings settings;
        private boolean assetsLoaded = false;
        private boolean restored = false;
        private Page currentPage;
        private long lastSaveTimestamp;
        private static final long MINIMUM_SAVE_INTERVAL = 200;
        private WebView view;

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
            if (u.startsWith("file://" + Utils.getSaveDirForTag(tag)) && u.contains("?title=")) {
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
            } else {
                Utils.viewExternalLink(PageReadingActivity.this, url);
            }
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            this.view = view;
            if (!assetsLoaded) {
                loadAssetIntoHead(
                        "css/" + Config.SKIN_BASE + ".css",
                        // On call back, overload with skin
                        __ignoredValue1 -> loadAssetIntoHead(
                                "css/" + Utils.getCurrentSkin() + ".css",
                                // On callback, load custom font
                                __ignoredValue2 -> loadCustomFont()
                        )
                );
                assetsLoaded = true;
            }
        }

        private void loadCustomFont() {
            // Load custom font
            String customFontFilename = Utils.getCurrentFont();
            Log.d(TAG, "Loading custom font: " + customFontFilename);
            String currentFontFileUrl = customFontFilename == null ?
                    "null" :
                    Config.FONT_LOCATION + customFontFilename;
            executeJS(this.view,
                    String.format("(function loadCustomFonts() { var currentFontFileUrl = \"%s\"; var currentSkin = \"%s\"; var customFontStyleElement = document.querySelector('style#custom-font'); if (!customFontStyleElement) { customFontStyleElement = document.createElement('style'); customFontStyleElement.type = 'text/css'; customFontStyleElement.id = 'custom-font'; document.head.append(customFontStyleElement); } if (currentFontFileUrl === \"null\") customFontStyleElement.innerHTML = '*{font-family: ' + currentSkin + ', sans-serif;}'; else customFontStyleElement.innerHTML = '@font-face {font-family: \"custom\";src: url(\"' + currentFontFileUrl + '\");}*{font-family: custom, sans-serif;}'; })();", currentFontFileUrl, Utils.getCurrentSkin()),
                    null
            );
        }

        private final String KEY_VALUE_DELIMITER = "______";
        private final String ELEMENT_DELIMITER = ";;;;;;";

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (restored) return;
            // Collect headers
            executeJS(view,
                    String.format("(function(){var headings = [].slice.call(document.body.querySelectorAll('h1, h2, h3, h4, h5, h6'));\nvar headingAutoName = 0;\nvar encodedHeaders = '';\nheadings.forEach(function (heading) {\n    if (!heading.id) {\n        headingAutoName += 1;\n        heading.id = '' + headingAutoName;\n    }\n    var space='';\n    for (var i=1;i<parseInt(heading.tagName.substring(1));i++)\n         space += '    ';\n    encodedHeaders += space + heading.textContent.trim() + '%s' + heading.id + '%s';\n});\nreturn encodedHeaders;})()", KEY_VALUE_DELIMITER, ELEMENT_DELIMITER),
                    value -> {
                        if (value.length() < 3) {
                            Toast.makeText(
                                    PageReadingActivity.this,
                                    R.string.no_heading_no_toc,
                                    Toast.LENGTH_SHORT
                            ).show();
                        } else {
                            // Decode headers
                            value = value.substring(1, value.length() - 1); // Remove surrounding " "
                            String[] keyValues = value.split(ELEMENT_DELIMITER);
                            for (String keyValue : keyValues) {
                                String[] decoded = keyValue.split(KEY_VALUE_DELIMITER);
                                if (decoded.length == 2) {
                                    headers.add(decoded[0]);
                                    headersId.put(decoded[0], decoded[1]);
                                }
                            }
                        }
                        buildTOCDialog();
                    }
            );
            new HistoryAsyncTask.LookUp(title, this::restoreCurrentReadingPosition).execute();
        }

        private void loadAssetIntoHead(String filepath,
                                       ValueCallback<String> callback) {
            Log.d(TAG, "Loading asset " + filepath + " into head");
            try {
                InputStream inputStream = getAssets().open(filepath);
                byte[] buffer = new byte[inputStream.available()];
                //noinspection ResultOfMethodCallIgnored
                inputStream.read(buffer);
                inputStream.close();
                String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
                // Tell the browser to BASE64-decode the string into your script !!!
                String js = String.format("(function() {var parent = document.head;var x = document.createElement('style');x.type = 'text/css';x.innerHTML = window.atob('%s');parent.appendChild(x)})()", encoded);
                executeJS(this.view, js, callback);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        private void executeJS(WebView view,
                               String js, ValueCallback<String> callback) {
            view.evaluateJavascript(js, callback);
        }

        void restoreCurrentReadingPosition(Page page) {
            currentPage = page;
            if (currentPage == null)
                currentPage = new Page(title, tag, Calendar.getInstance().getTime(), 0);
            // Restore last reading position
            executeJS(pageViewer,
                    String.format("(function() {var top = document.body.scrollHeight * %s;var isSmoothScrollSupported = 'scrollBehavior' in document.documentElement.style;if (isSmoothScrollSupported) window.scrollTo({behavior: 'smooth', top: top});else window.scrollTo(0, top);})()", currentPage.getCurrentReadingPosition()), value -> restored = true);
        }

        void saveCurrentReadingPosition() {
            if (currentPage == null) return;
            // Debounce logic to reduce saving calls
            final long currentTimestamp = SystemClock.uptimeMillis();
            if (currentTimestamp - lastSaveTimestamp >= MINIMUM_SAVE_INTERVAL) {
                executeJS(pageViewer,
                        "(function() {return window.pageYOffset / document.body.scrollHeight;})()",
                        value -> {
                            currentPage.setLastRead(Calendar.getInstance().getTime());
                            currentPage.setCurrentReadingPosition(Float.parseFloat(value));
                            new HistoryAsyncTask.Register().execute(currentPage);
                            lastSaveTimestamp = currentTimestamp;
                        }
                );
            }
        }

        /**
         * Percentage
         */
        private static final int MAX_ZOOM = 500;
        private static final int MIN_ZOOM = 20;
        private static final int STEP_ZOOM = 20;

        void minusTextSize() {
            if (settings.getTextZoom() - STEP_ZOOM >= MIN_ZOOM) {
                settings.setTextZoom(settings.getTextZoom() - STEP_ZOOM);
                saveCurrentReadingPosition();
            }
        }

        void plusTextSize() {
            if (settings.getTextZoom() + STEP_ZOOM <= MAX_ZOOM) {
                settings.setTextZoom(settings.getTextZoom() + STEP_ZOOM);
                saveCurrentReadingPosition();
            }
        }

        void scrollToId(String id) {
            executeJS(pageViewer,
                    String.format("document.getElementById('%s').scrollIntoView({behavior: 'smooth'})", id),
                    null);
        }
    }

    static class PageReadingWebChromeClient extends WebChromeClient {
        public boolean onConsoleMessage(ConsoleMessage cm) {
            Log.d(String.format("%sWebviewMessage%s", TAG, cm.messageLevel().name()),
                    String.format("%s \nFrom line %d of %s", cm.message(), cm.lineNumber(), cm.sourceId()));
            return true;
        }
    }
}