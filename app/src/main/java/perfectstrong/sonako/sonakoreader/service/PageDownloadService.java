package perfectstrong.sonako.sonakoreader.service;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.GSONP;
import okhttp3.HttpUrl;
import okhttp3.Response;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.database.LightNovel;
import perfectstrong.sonako.sonakoreader.helper.Config;
import perfectstrong.sonako.sonakoreader.helper.Utils;

/**
 * Download a page given its title and tag
 */
public class PageDownloadService extends IntentService {

    private static final String TAG = PageDownloadService.class.getSimpleName();
    private String title;
    private String tag;
    private String saveLocation;
    private String filename;
    private ACTION action;
    private boolean forceRefreshText;
    private boolean forceRefreshImages;
    private Wiki wiki;
    private String text;
    private Map<String, String> imagesLinks = new HashMap<>();
    private Handler mHandler;

    public PageDownloadService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
    }

    private void postToast(String msg) {
        mHandler.post(() -> Toast.makeText(
                this,
                msg,
                Toast.LENGTH_LONG)
                .show()
        );
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            title = bundle.getString(Config.EXTRA_TITLE);
            tag = bundle.getString(Config.EXTRA_TAG);
            action = (ACTION) bundle.getSerializable(Config.EXTRA_ACTION);
        }
        if (title == null) {
            stopSelf();
            return;
        }
        // Register
        saveLocation = Utils.getSavDirForTag(tag);
        filename = Utils.sanitize(title) + ".html";
        if (action != null)
            switch (action) {
                case REFRESH_TEXT:
                    forceRefreshText = true;
                    break;
                case REFRESH_ALL:
                    forceRefreshText = true;
                    forceRefreshImages = true;
                    break;
            }
        Log.d(TAG, "title = " + title + ", tag = " + tag + ", action = " + action);
        if (Utils.isNotCached(title, tag) || forceRefreshText)
            try {
                // Check cache
                wiki = new Wiki(Objects.requireNonNull(HttpUrl.parse(Config.API_ENDPOINT)));
                if (!wiki.exists(title)) {
                    throw new IllegalArgumentException(getString(R.string.not_having) + " " + title);
                }
                postToast(getString(R.string.start_downloading) + " " + title);
                downloadText();
                preprocess();
                cacheText();
                downloadImages();
                postToast(getString(R.string.download_finish) + " " + title);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                postToast(e.getMessage());
            }
        else
            postToast(title + " " + getString(R.string.already_download));
    }

    private void publishProgress(String str) {
        // TODO
        Log.d(TAG, str);
    }

    private void downloadText() throws IOException {
        publishProgress(getString(R.string.downloading_text) + " " + title);
        Response res = wiki.basicGET(
                "parse",
                "page", title,
                "format", "json",
                "rvprop", "content",
                "disablelimitreport", "true",
                "disabletoc", "true",
                "prop", "text|categories",
                "useskin", "mercury"
        );
        publishProgress(getString(R.string.analyzing_text) + " " + title);
        assert res.body() != null;
        JsonObject jsonObject = GSONP.jp.parse(res.body().string()).getAsJsonObject();
        // Check error
        if (jsonObject.get("error") != null) {
            JsonObject error = jsonObject.get("error").getAsJsonObject();
            throw new IllegalArgumentException(
                    error.get("code").getAsString()
                            + ": "
                            + error.get("info").getAsString()
            );
        }

        // Parse
        JsonObject parse = jsonObject.get("parse").getAsJsonObject();
        // Raw text
        text = parse.get("text").getAsJsonObject().get("*").getAsString();
        // Get tag
        for (JsonElement element : parse.get("categories").getAsJsonArray()) {
            // Skip status, type and genres
            String t = element.getAsJsonObject().get("*").getAsString();
            if (LightNovel.ProjectGenre.ALL.contains(t)) continue;
            if (LightNovel.ProjectStatus.ALL.contains(t)) continue;
            if (LightNovel.ProjectType.ALL.contains(t)) continue;
            tag = t;
            break; // At the first occurrence
        }
    }

    private void preprocess() {
        publishProgress(getString(R.string.preprocessing_text) + " " + title);
        Document doc = Jsoup.parse(text.replaceAll("\\\\\"", "\""));
        // Retain local navigator
        Element localNav = doc.selectFirst(".localNav");
        // Remove unrelated section
        doc.getElementsByClass("entry-unrelated").remove();
        // Reappend local nav at the end
        if (localNav != null)
            doc.body().appendChild(localNav);
        // Remove edit section link
        doc.getElementsByClass("editsection").remove();
        // Fix figures
        for (Element figure : doc.getElementsByTag("figure")) {
            // Fix gallery
            if (figure.getElementsByTag("figcaption") != null) {
                try {
                    String text = figure.selectFirst("img.lazy.media").attr("data-params");
                    JsonArray imageDescriptions = GSONP.jp.parse(
                            Parser.unescapeEntities(text, true)
                    ).getAsJsonArray();
                    figure.empty();
                    for (JsonElement id : imageDescriptions) {
                        Element img = new Element("img");
                        img.attr("src",
                                id.getAsJsonObject().get("full").getAsString());
                        if (id.getAsJsonObject().get("capt") != null)
                            img.attr("data-capt",
                                    id.getAsJsonObject().get("capt").getAsString());
                        figure.appendChild(img);
                    }
                    figure.attr("width", "80%");
                } catch (Exception e) {
                    Log.d(TAG, "Error on parsing gallery", e);
                }
            } else {
                // Fix direct wiki images
                Element realImg = figure.selectFirst("noscript > img");
                realImg.attr("src",
                        realImg.attr("src").replaceAll("/revision.*", ""));
                Log.d(TAG, realImg.attr("src"));
                figure.empty().appendChild(realImg);
                figure.attr("width", "80%");
            }
        }
        // Fix all images
        for (Element img : doc.getElementsByTag("img")) {
            img.attr("width", "100%");
            img.attr("height", "auto");
            String src = img.attr("src");
            if (src.startsWith("http:")) //noinspection ResultOfMethodCallIgnored
                src = src.replaceFirst("http", "https");
            if (src.contains("wikia.nocookie")) // direct link from wikia
                src = src.replaceAll("/revision.*", "");
            img.attr("data-src", src); // Backup
            String imgName = Utils.getFileNameFromURL(src);
            if (!imgName.equals("")) {
                imagesLinks.put(imgName, src);
                img.attr("src", imgName); // To load from local
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
                if (!Utils.isMainpage(href)) {
                    // Not main page
                    element.attr("href", Config.WEBSITE + href);
                } else
                    // Main page
                    element.attr("href",
                            Utils.removeSubtrait(
                                    Uri.decode(href.replace("/wiki/", "")))
                                    + ".html"
                    );
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
        text = doc.outerHtml();
        Log.d(TAG, text);
    }

    private void cacheText() {
        publishProgress(getString(R.string.caching) + " " + title);
        File dir = new File(saveLocation);
        if (!dir.exists()) //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        File content = new File(dir, filename);
        //noinspection CharsetObjectCanBeUsed
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(content), "UTF-8"))) {
            writer.write(text);
        } catch (IOException e) {
            Log.e(TAG, "Text caching failed", e);
        }
    }

    private void downloadImages() {
        for (String imageName : imagesLinks.keySet()) {
            String url = imagesLinks.get(imageName);
            File file = new File(saveLocation, imageName);
            if (file.exists() && !forceRefreshImages) // already cached
                continue;

            publishProgress(getString(R.string.downloading_image) + " " + imageName);
            String mime = imageName.substring(imageName.lastIndexOf(".") + 1).toLowerCase();
            if (!Config.SUPPORTED_IMAGE_EXTENSIONS.contains(mime)) {
                Log.e(TAG, "Unsupported image extension " + imageName);
                continue;
            }

            Bitmap bitmap = null;
            try (InputStream inputStream = new URL(url).openStream()) {
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                Log.e(TAG, imageName + " downloading failed", e);
            }

            if (bitmap == null) continue;
            try (FileOutputStream fo = new FileOutputStream(file)) {
                switch (mime) {
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
                        throw new UnsupportedOperationException(mime + " unsupported");
                }
            } catch (IOException | UnsupportedOperationException e) {
                Log.e(TAG, imageName + " saving failed", e);
            }
        }
    }

    public enum ACTION {
        /**
         * Reload text page
         */
        REFRESH_TEXT,
        /**
         * Reload text page and images
         */
        REFRESH_ALL,
    }
}
