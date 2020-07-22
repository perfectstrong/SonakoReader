package perfectstrong.sonako.sonakoreader.service;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.liulishuo.okdownload.DownloadContext;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.DownloadListener2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.activity.PageReadingActivity;
import perfectstrong.sonako.sonakoreader.asyncTask.BiblioAsyncTask;
import perfectstrong.sonako.sonakoreader.database.CachePage;
import perfectstrong.sonako.sonakoreader.database.LightNovel;
import perfectstrong.sonako.sonakoreader.fragments.PageDownloadFragment;
import perfectstrong.sonako.sonakoreader.helper.Config;
import perfectstrong.sonako.sonakoreader.helper.Utils;
import perfectstrong.sonako.sonakoreader.helper.WikiClient;

/**
 * Download a page given its title and tag
 */
public class PageDownloadService extends IntentService {

    private static final String TAG = PageDownloadService.class.getSimpleName();
    private String title;
    private String tag;
    private ACTION action;
    private int intentId;
    private String flag;
    private String saveLocation;
    private String filename;
    private boolean forceRefreshText;
    private boolean forceRefreshImages;
    private WikiClient wiki;
    private String text;
    private List<Element> imagesLinks;
    private Handler mHandler;

    private static final String FLAG_DOWNLOAD = Config.APP_PREFIX + ".flag.download";
    private static final String FLAG_DOWNLOAD_DUPLICATE = "DUPLICATE";

    private static Map<String, String> tasks;
    private boolean downloadFailedImages;
    private Document doc;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManagerCompat notificationManager;
    private final Pattern internalLinkRegex = Pattern.compile("/(\\w+/)?wiki/(.*)");
    private final Map<String, Element> mapUrlsWithCorrespondingImgElement = new ConcurrentHashMap<>();

    public PageDownloadService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        tasks = new PageDownloadingTaskMap(mHandler);
        notificationBuilder = new NotificationCompat.Builder(this, Config.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_sonakoreaderlogo_bw)
                .setColor(getResources().getColor(R.color.holySecondaryColor))
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setAutoCancel(true) // Some notifications will disappear on dev mode
                .setOnlyAlertOnce(true);
        notificationManager = NotificationManagerCompat.from(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String title = bundle.getString(Config.EXTRA_TITLE);
            int intentId = bundle.getInt(Config.EXTRA_ID);
            if (title != null && !title.isEmpty())
                if (!tasks.containsKey(title)) {
                    tasks.put(title, getString(R.string.download_waiting, title));
                    announce(title, getString(R.string.download_waiting, title), intentId, null, false, NotificationCompat.PRIORITY_MIN);
                } else {
                    bundle.putString(FLAG_DOWNLOAD, FLAG_DOWNLOAD_DUPLICATE);
                    intent.putExtras(bundle);
                }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void publishNotification(String title,
                                     String msg,
                                     int intentId,
                                     PendingIntent pendingIntent,
                                     boolean onProgress,
                                     int importance) {
        if (title != null && !title.isEmpty() && intentId != 0) {
            notificationBuilder.setContentTitle(title)
                    .setContentText(msg)
                    .setContentIntent(pendingIntent)
                    .setProgress(0, 0, onProgress)
                    .setPriority(importance)
            ;
            notificationManager.notify(intentId, notificationBuilder.build());
        }
    }

    private void announce(String title,
                          String msg,
                          int intentId,
                          PendingIntent pendingIntent,
                          boolean onProgress,
                          int importance) {
        Log.d(TAG, title + ": " + msg);
        if (intentId == 0)
            mHandler.post(() -> Toast.makeText(
                    this,
                    msg,
                    Toast.LENGTH_SHORT)
                    .show()
            );
        else
            publishNotification(title, msg, intentId, pendingIntent, onProgress, importance);
    }

    private void publishProgress(String str) {
        tasks.put(title, str);
        Log.v(TAG, title + " " + str);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            title = bundle.getString(Config.EXTRA_TITLE);
            tag = bundle.getString(Config.EXTRA_TAG);
            action = (ACTION) bundle.getSerializable(Config.EXTRA_ACTION);
            intentId = bundle.getInt(Config.EXTRA_ID);
            flag = bundle.getString(FLAG_DOWNLOAD);
        }
        if (title == null) {
            stopSelf();
            return;
        }
        if (FLAG_DOWNLOAD_DUPLICATE.equals(flag)) {
            stopSelf();
            return;
        }
        // Connection check
        try {
            Utils.checkConnection();
        } catch (ConnectException e) {
            Log.w(TAG, "" + e.getMessage());
            announce(title, getString(R.string.connection_error), intentId, null, false, NotificationCompat.PRIORITY_MAX);
            stopSelf();
            return;
        }

        // Register
        String decodedTitle = Utils.sanitize(Utils.decode(title));
        publishProgress(getString(R.string.download_starting, decodedTitle));
        filename = decodedTitle + ".html";
        if (action != null)
            switch (action) {
                case REFRESH_TEXT:
                    forceRefreshText = true;
                    break;
                case REFRESH_MISSING_IMAGES:
                    downloadFailedImages = true;
                    break;
                case REFRESH_ALL:
                    forceRefreshText = true;
                    forceRefreshImages = true;
                    break;
            }
        Log.d(TAG, "title = " + title + ", tag = " + tag + ", action = " + action);
        try {
            // Real start
            announce(title, getString(R.string.start_downloading_fmt, title), intentId, null, true, NotificationCompat.PRIORITY_MAX);
            if (Utils.isNotCached(title, tag) || forceRefreshText) {
                // Check cache
                wiki = new WikiClient(Config.API_ENDPOINT, Config.USER_AGENT);
                if (!wiki.exists(title)) {
                    throw new IllegalArgumentException(getString(R.string.not_having, title));
                }
                if (downloadFailedImages) {
                    loadImageLinksFromCachedText();
                } else {
                    downloadText();
                    assert tag != null;
                    saveLocation = Utils.getSaveDirForTag(tag); // tag shall be not null
                    preprocessText(); // Including caching temporarily image links from text
                    cacheText();
                }
                downloadImages();
            }
            // Reading intent to open
            Intent readingIntent = new Intent(this, PageReadingActivity.class);
            Bundle bundle1 = new Bundle();
            bundle1.putString(Config.EXTRA_TITLE, title);
            bundle1.putString(Config.EXTRA_TAG, tag);
            readingIntent.putExtras(bundle1);
            announce(title,
                    getString(R.string.download_finish, title),
                    intentId,
                    PendingIntent.getActivity(this, intentId, readingIntent, 0),
                    false,
                    NotificationCompat.PRIORITY_MAX);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            announce(title, getString(R.string.error_on_downloading, title), 0, null, false, NotificationCompat.PRIORITY_MAX);
        }
        tasks.remove(title);
    }

    private void loadImageLinksFromCachedText() throws IOException {
        if (tag == null)
            throw new IOException(getString(R.string.no_cached, title));
        publishProgress(getString(R.string.analyzing_text, title));
        imagesLinks = new ArrayList<>();
        Document doc = Jsoup.parse(
                Utils.getTextFile(title, tag),
                "UTF-8");
        for (Element img : doc.getElementsByTag("img")) {
            if (img.hasAttr("data-name")) {
                imagesLinks.add(img);
            } else {
                String imgName = Utils.sanitize(
                        Utils.getFileNameFromURL(Utils.decode(img.attr("src"))));
                if (!imgName.equals("")) {
                    img.attr("data-name", imgName);
                    imagesLinks.add(img);
                }
            }
        }
    }

    private void downloadText() throws IOException, JSONException {
        publishProgress(getString(R.string.downloading_text, title));
        Response res = wiki.GET(
                "parse",
                "page", title,
                "rvprop", "content",
                "disablelimitreport", "true",
                "disabletoc", "true",
                "prop", "text|categories",
                "useskin", "mercury"
        );
        publishProgress(getString(R.string.analyzing_text, title));
        assert res.body() != null;
        JSONObject jsonObject = new JSONObject(Objects.requireNonNull(res.body()).string());
        // Check error
        if (jsonObject.optJSONObject("error") != null) {
            JSONObject error = jsonObject.getJSONObject("error");
            throw new IllegalArgumentException(
                    error.getString("code")
                            + ": "
                            + error.getString("info")
            );
        }

        // Parse
        JSONObject parse = jsonObject.getJSONObject("parse");
        // Raw text
        text = parse.getJSONObject("text").getString("*");
        // Get tag if not defined
        if (tag == null) {
            JSONArray elements = parse.getJSONArray("categories");
            for (int i = 0; i < elements.length(); i++) {
                JSONObject element = elements.getJSONObject(i);
                // Skip status, type and genres
                String t = Utils.removeSubtrait(element.getString("*"));
                if (LightNovel.ProjectGenre.ALL.contains(t)) continue;
                if (LightNovel.ProjectStatus.ALL.contains(t)) continue;
                if (LightNovel.ProjectType.ALL.contains(t)) continue;
                if (LightNovel.ExceptionTag.ALL.contains(t)) continue;
                tag = t;
                break; // At the first occurrence
            }
        }
    }

    private void preprocessText() {
        publishProgress(getString(R.string.preprocessing_text, title));
        doc = Jsoup.parse(text.replaceAll("\\\\\"", "\""));
        // Retain local navigator
        Element localNav = doc.selectFirst(".localNav");
        // Remove unrelated section
        doc.getElementsByClass("entry-unrelated").remove();
        // Remove hidden section
        doc.getElementsByClass("hidden").remove();
        // Reappend local nav at the end
        if (localNav != null)
            doc.body().appendChild(localNav);
        // Remove edit section link
        doc.getElementsByClass("editsection").remove();
        // Fix figures
        for (Element figure : doc.getElementsByTag("figure")) {
            // Fix gallery
            if (figure.selectFirst("img.lazy.media") != null
                    && figure.selectFirst("img.lazy.media").attr("data-params") != null) {
                try {
                    String text = figure.selectFirst("img.lazy.media").attr("data-params");
                    JSONArray imageDescriptions = new JSONArray(
                            Parser.unescapeEntities(text, true)
                    );
                    figure.empty();
                    for (int i = 0; i < imageDescriptions.length(); i++) {
                        JSONObject id = imageDescriptions.getJSONObject(i);
                        Element img = new Element("img");
                        img.attr("src",
                                id.getString("full"));
                        if (img.attributes().hasKeyIgnoreCase("capt"))
                            img.attr("data-capt",
                                    id.getString("capt"));
                        figure.appendChild(img);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Error on parsing gallery", e);
                }
            } else {
                // Fix direct wiki images
                Element realImg = figure.selectFirst("noscript > img");
                if (realImg == null)
                    // Non existing image
                    continue;
                realImg.attr("src",
                        realImg.attr("src").replaceAll("/revision.*", ""));
                Log.d(TAG, realImg.attr("src"));
                figure.empty().appendChild(realImg);
            }
        }
        // Fix and get all images' names
        File dir = new File(saveLocation);
        if (!dir.exists()) //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        imagesLinks = new ArrayList<>();
        for (Element img : doc.getElementsByTag("img")) {
            String urlSrc = img.attr("src");
            if (urlSrc.startsWith("http:"))
                urlSrc = urlSrc.replaceFirst("http", "https");
            if (Utils.isInternalImage(urlSrc)) // direct link from wikia
                urlSrc = urlSrc.replaceAll("/revision.*", "");
            img.removeAttr("width"); // Let viewer client decide later
            img.removeAttr("height"); // Let viewer client decide later
            String imgName = Utils.sanitize(Utils.getFileNameFromURL(Utils.decode(urlSrc)));
            img.attr("data-name", imgName);
            img.attr("data-src", urlSrc);
            // Calculate the local src attribute
            if (!imgName.equals("")) {
                File imgFile = new File(dir, imgName);
                String imgNameAsWebp; // Check whether the webp version existed
                if (imgName.lastIndexOf(".") > -1)
                    // Replace extension
                    imgNameAsWebp = imgName.substring(0, imgName.lastIndexOf(".")) + ".webp";
                else
                    imgNameAsWebp = imgName + ".webp";
                if (!forceRefreshImages) {
                    if (!imgFile.exists()) { // If file has not been cached
                        if (new File(dir, imgNameAsWebp).exists()) {
                            // Refer to cached image
                            img.attr("src", imgNameAsWebp);
                        } else {
                            // Else refer in advance to the img that'll be downloaded later
                            img.attr("src",
                                    Utils.isWebpPreferred() ? imgNameAsWebp : imgName);
                        }
                    } else {
                        img.attr("src", imgName);
                    }
                } else {
                    img.attr("src",
                            Utils.isWebpPreferred() ? imgNameAsWebp : imgName);
                }

                imagesLinks.add(img); // Caching temporarily for later downloading
            }
        }
        // Fix empty tags
        for (Element element : doc.getElementsByTag("div")) {
            if (element.childNodeSize() == 0) element.remove();
        }
        // Fix internal links
        for (Element element : doc.getElementsByTag("a")) {
            String href = element.attr("href");
            Matcher m = internalLinkRegex.matcher(href);
            if (m.matches()) {// internal link
                // Check namespace
                href = m.group(m.groupCount());
                assert href != null;
                if (Utils.isMainpage(href)) {
                    // Main page
                    href = Utils.removeSubtrait(Utils.decode(href));
                    element.attr("href",
                            String.format("%s.html?title=%s", Utils.sanitize(href), href)
                    );
                    element.attr("data-ns", "0"); // Main page namespace
                } else {
                    // Not main page
                    element.attr("href", Config.WEBSITE + "/wiki/" + href);
                }
            }
        }
        // Add head
        publishProgress(getString(R.string.preprocessing_display, title));
        doc.head()
                .appendElement("meta")
                .attr("charset", "utf-8");
        doc.head()
                .appendElement("meta")
                .attr("name", "viewport")
                .attr("content", "width=device-width, initial-scale=1");
    }

    private void cacheText() {
        publishProgress(getString(R.string.caching, title));
        File dir = new File(saveLocation);
        if (!dir.exists()) //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        File content = new File(dir, filename);
        try {
            @SuppressWarnings("CharsetObjectCanBeUsed")
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(content), "UTF-8"));
            try {
                writer.write(doc.outerHtml());
                // Update biblio
                new BiblioAsyncTask.Register().execute(new CachePage(
                        title,
                        tag,
                        new Date(content.lastModified())
                ));
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Text caching failed", e);
        }
    }

    private void downloadImages() throws IOException, JSONException {
        publishProgress(getString(R.string.downloading_starting_images, title));
        // Save location
        File dir = new File(saveLocation);
        if (!dir.exists()) //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        // Compress images to adapt to max length of screen
        compressWikiaImages();
        // Saving images
        DownloadContext.Builder taskMassDownloadBuilder = new DownloadContext.QueueSet()
                .setParentPath(saveLocation)
                .setWifiRequired(Utils.isNotAuthorizedDownloadingOverCellularConnection(this))
                .commit();
        Map<String, List<String>> webpHeaders = new HashMap<>();
        webpHeaders.put("Accept", Arrays.asList("image/webp", "image/*;q=0.8"));
        Map<String, List<String>> emptyHeaders = new HashMap<>();
        for (Element img : imagesLinks) {
            if (!img.hasAttr("data-src") || !img.hasAttr("src")) continue;
            String imageName = img.attr("src");
            String url = img.attr("data-src");
            File file = new File(dir, imageName);
            // Already cached original
            if (!forceRefreshImages && file.exists()) continue;
            boolean isInternalImage = Utils.isInternalImage(url);
            // If user allows, download external images as it is
            // Only download not cached images if not forced
            if (isInternalImage
                    || Utils.isAllowedToDownloadExternalImages()) {
                // Queue task to downloading context
                taskMassDownloadBuilder.bind(
                        new DownloadTask.Builder(url, dir)
                                .setFilenameFromResponse(false)
                                .setFilename(imageName)
                                .setHeaderMapFields(imageName.endsWith(".webp") ?
                                        webpHeaders : emptyHeaders));
                mapUrlsWithCorrespondingImgElement.put(url, img);
            }
        }
        taskMassDownloadBuilder.build().startOnParallel(new DownloadListener2() {
            @Override
            public void taskStart(@NonNull DownloadTask task) {
                // Do nothing
                publishProgress(getString(R.string.download_starting, title));
            }

            @Override
            public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Exception realCause) {
                String url = task.getUrl();
                if (realCause != null)
                    Log.w(TAG, "downloadImages: Failed for " + url, realCause);
                else {
                    if (mapUrlsWithCorrespondingImgElement.containsKey(url)) {
                        mapUrlsWithCorrespondingImgElement.remove(url);
                        publishProgress(getString(R.string.download_finish, task.getFilename()));
                    }
                }
            }
        });
    }

    private void compressWikiaImages() throws IOException, JSONException {
        if (Utils.isAllowedToDownloadOriginalImages())
            return;
        int maxImageWidth = Utils.getPreferredSize();
        Log.d(TAG, "maxImageWidth = " + maxImageWidth);
        List<String> wikiImgList = new ArrayList<>();
        for (Element img : imagesLinks) {
            String src = img.attr("data-src");
            if (Utils.isInternalImage(src)) {
                wikiImgList.add(img.attr("data-name"));
            }
        }
        // Image name with resizing url
        Map<String, String> resizedImgLinks = wiki.resizeToWidth(maxImageWidth, wikiImgList);
        for (Element img : imagesLinks) {
            String imgName = img.attr("data-name");
            if (resizedImgLinks.containsKey(imgName))
                img.attr("data-src", resizedImgLinks.get(imgName));
        }
    }

    public enum ACTION {
        /**
         * Redownload failed images
         */
        REFRESH_MISSING_IMAGES,
        /**
         * Reload text page
         */
        REFRESH_TEXT,
        /**
         * Reload text page and images
         */
        REFRESH_ALL,
    }

    /**
     * Download the page
     */
    public static final String DOWNLOAD = Config.EXTRA_ACTION + ".download";

    private static class PageDownloadingTaskMap extends HashMap<String, String> {
        private final Handler handler;

        public PageDownloadingTaskMap(Handler mHandler) {
            this.handler = mHandler;
        }

        @Override
        public String put(String key, String value) {
            if (this.containsKey(key)) {
                // An update status
                handler.post(() -> PageDownloadFragment.getInstance().updateJob(key, value));
            } else {
                // New download
                handler.post(() -> PageDownloadFragment.getInstance().addJob(key));
            }
            return super.put(key, value);
        }

        @Override
        public String remove(Object key) {
            if (this.containsKey(key) && key instanceof String) {
                // Download complete
                handler.post(() -> PageDownloadFragment.getInstance().removeJob((String) key));
            }
            return super.remove(key);
        }
    }
}
