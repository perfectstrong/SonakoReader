package perfectstrong.sonako.sonakoreader.service;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
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

    public PageDownloadService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        tasks = new HashMap<String, String>() {
            @Override
            public String put(String key, String value) {
                if (this.containsKey(key)) {
                    // An update status
                    mHandler.post(() -> PageDownloadFragment.getInstance().updateJob(key, value));
                } else {
                    // New download
                    mHandler.post(() -> PageDownloadFragment.getInstance().addJob(key));
                }
                return super.put(key, value);
            }

            @Override
            public String remove(Object key) {
                if (this.containsKey(key) && key instanceof String) {
                    // Download complete
                    mHandler.post(() -> PageDownloadFragment.getInstance().removeJob((String) key));
                }
                return super.remove(key);
            }
        };
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
                    tasks.put(title, getString(R.string.download_waiting));
                    announce(title, getString(R.string.download_waiting), intentId, null, false, NotificationCompat.PRIORITY_MIN);
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
        publishProgress(getString(R.string.download_starting));
        filename = Utils.sanitize(Utils.decode(title)) + ".html";
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
            announce(title, getString(R.string.start_downloading) + " " + title, intentId, null, true, NotificationCompat.PRIORITY_MAX);
            if (Utils.isNotCached(title, tag) || forceRefreshText) {
                // Check cache
                wiki = new WikiClient(Config.API_ENDPOINT, Config.USER_AGENT);
                if (!wiki.exists(title)) {
                    throw new IllegalArgumentException(getString(R.string.not_having) + " " + title);
                }
                if (downloadFailedImages) {
                    loadImageLinksFromCachedText();
                    downloadImages();
                } else {
                    downloadText();
                    preprocess();
                    saveLocation = Utils.getSaveDirForTag(tag); // tag shall be not null
                    downloadImages();
                    cacheText();
                }
            }
            // Reading intent to open
            Intent readingIntent = new Intent(this, PageReadingActivity.class);
            Bundle bundle1 = new Bundle();
            bundle1.putString(Config.EXTRA_TITLE, title);
            bundle1.putString(Config.EXTRA_TAG, tag);
            readingIntent.putExtras(bundle1);
            announce(title,
                    getString(R.string.download_finish) + " " + title,
                    intentId,
                    PendingIntent.getActivity(this, intentId, readingIntent, 0),
                    false,
                    NotificationCompat.PRIORITY_MAX);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            announce(title, getString(R.string.error_on_downloading), 0, null, false, NotificationCompat.PRIORITY_MAX);
        }
        tasks.remove(title);
    }

    private void loadImageLinksFromCachedText() throws IOException {
        if (tag == null)
            throw new IOException(getString(R.string.no_cached));
        publishProgress(getString(R.string.analyzing_text));
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
        publishProgress(getString(R.string.downloading_text) + " " + title);
        Response res = wiki.GET(
                "parse",
                "page", title,
                "rvprop", "content",
                "disablelimitreport", "true",
                "disabletoc", "true",
                "prop", "text|categories",
                "useskin", "mercury"
        );
        publishProgress(getString(R.string.analyzing_text) + " " + title);
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

    private void preprocess() {
        publishProgress(getString(R.string.preprocessing_text) + " " + title);
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
        imagesLinks = new ArrayList<>();
        for (Element img : doc.getElementsByTag("img")) {
            String src = img.attr("src");
            if (src.startsWith("http:"))
                src = src.replaceFirst("http", "https");
            if (Utils.isInternalImage(src)) // direct link from wikia
                src = src.replaceAll("/revision.*", "");
            img.removeAttr("width"); // Let viewer client decide later
            img.removeAttr("height"); // Let viewer client decide later
            String imgName = Utils.sanitize(Utils.getFileNameFromURL(Utils.decode(src)));
            if (!imgName.equals("")) {
                imagesLinks.add(img); // Caching temporarily
                img.attr("data-name", imgName);
                img.attr("data-src", src);
                img.attr("src", "");
            }
        }
        // Fix empty tags
        for (Element element : doc.getElementsByTag("div")) {
            if (element.childNodeSize() == 0) element.remove();
        }
        // Fix internal links
        for (Element element : doc.getElementsByTag("a")) {
            String href = element.attr("href");
            if (href.startsWith("/wiki/")) {// internal link
                // Check namespace
                href = href.substring("/wiki/".length());
                if (Utils.isMainpage(href)) {
                    // Main page
                    href = Utils.removeSubtrait(Utils.decode(href));
                    element.attr("href",
                            Utils.sanitize(href) + ".html"
                                    + "?title=" + href
                    );
                    element.attr("data-ns", "0"); // Main page namespace
                } else {
                    // Not main page
                    element.attr("href", Config.WEBSITE + "/wiki/" + href);
                }
            }
        }
        // Add head
        publishProgress(getString(R.string.preprocessing_display) + " " + title);
        doc.head()
                .appendElement("meta")
                .attr("charset", "utf-8");
        doc.head()
                .appendElement("meta")
                .attr("name", "viewport")
                .attr("content", "width=device-width, initial-scale=1");
    }

    private void cacheText() {
        publishProgress(getString(R.string.caching) + " " + title);
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
        // Save location
        File dir = new File(saveLocation);
        if (!dir.exists()) //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        // Compress images to adapt to max length of screen
        compressWikiaImages();
        // Saving images
        OkHttpClient client = WikiClient.getNewHttpClient();
        for (Element img : imagesLinks) {
            if (!img.hasAttr("data-name")) continue;
            String imageName = img.attr("data-name");
            String url = img.attr("data-src");
            if (url == null) continue; // Skip null link
            File file = new File(dir, imageName);
            if (!forceRefreshImages) {
                // Already cached original
                if (file.exists())
                    continue;
                // Already cached webp
                String imgWebp;
                if (imageName.lastIndexOf(".") > -1)
                    imgWebp = imageName.substring(0, imageName.lastIndexOf(".")) + ".webp";
                else
                    imgWebp = imageName + ".webp";
                if (new File(dir, imgWebp).exists()) {
                    // Refer to cached image
                    img.attr("src", imgWebp);
                    continue;
                }
            }
            boolean isInternalImage = Utils.isInternalImage(url);
            if (isInternalImage
                    || Utils.isAllowedToDownloadExternalImages()) {
                // If user allows, download external images as it is
                // Only download not cached images if not forced
                publishProgress(getString(R.string.downloading_image) + " " + imageName);

                Request.Builder requestBuilder = new Request.Builder().url(url);
                if (Utils.isWebpPreferred()) // Check webp
                    requestBuilder.addHeader("Accept", "image/webp,image/*;q=0.8");
                Request request = requestBuilder.build();

                Bitmap bitmap;
                MediaType mime;
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    ResponseBody body = response.body();
                    assert body != null;
                    InputStream inputStream = body.byteStream();
                    try {
                        bitmap = BitmapFactory.decodeStream(inputStream);
                    } finally {
                        inputStream.close();
                    }
                    mime = body.contentType();
                } catch (IOException e) {
                    Log.e(TAG, imageName + " downloading failed", e);
                    continue;
                } finally {
                    if (response != null)
                        response.close();
                }

                assert mime != null;
                if (!mime.type().equals("image")) {
                    Log.e(TAG, "Not an image: " + url);
                    continue;
                }
                if (!Config.SUPPORTED_IMAGE_EXTENSIONS.contains(mime.subtype())) {
                    Log.e(TAG, "Unsupported image extension " + imageName);
                    continue;
                }
                // Change extension
                if (imageName.lastIndexOf(".") > -1)
                    imageName = imageName.substring(0, imageName.lastIndexOf(".")) + "." + mime.subtype();
                else
                    imageName += "." + mime.subtype();
                file = new File(dir, imageName);

                // Saving
                try {
                    FileOutputStream fo = new FileOutputStream(file);
                    try {
                        switch (mime.subtype()) {
                            case "jpg":
                            case "jpeg":
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fo);
                                break;
                            case "png":
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fo);
                                break;
                            case "webp":
                                bitmap.compress(Bitmap.CompressFormat.WEBP, 100, fo);
                                break;
                            default:
                                throw new UnsupportedOperationException(mime.subtype() + " unsupported");
                        }
                    } finally {
                        fo.close();
                    }
                } catch (IOException | UnsupportedOperationException e) {
                    Log.e(TAG, imageName + " saving failed", e);
                }
                // Refer to cached image
                img.attr("src", imageName);
                Log.d(TAG, "imageName = " + imageName + ", link = " + url);
            }
        }
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
}
