package perfectstrong.sonako.sonakoreader.helper;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.GSONP;
import okhttp3.HttpUrl;
import okhttp3.Response;
import perfectstrong.sonako.sonakoreader.PageReadingActivity;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.database.LightNovel;

public class PageLoader extends AsyncTask<Void, String, Void> {

    private static final String TAG = PageLoader.class.getSimpleName();
    private String filename;

    private WeakReference<PageReadingActivity> readingActivity;
    private final String title;
    private final String saveLocation;
    private String text;
    private ProgressDialog progressDialog;
    private Exception exception;
    /**
     * Light novel tag
     */
    private String tag;
    private Map<String, String> imagesLinks = new HashMap<>();
    private Wiki wiki;
    private boolean forceRefreshText = false;
    private boolean forceRefreshImages = false;

    public PageLoader(PageReadingActivity readingActivity,
                      String title,
                      String tag,
                      PageReadingActivity.ACTION action) {
        this.readingActivity = new WeakReference<>(readingActivity);
        this.title = title;
        this.tag = tag;
        this.saveLocation = Utils.getSaveLocationForTag(tag);
        this.filename = Utils.sanitize(title) + ".html";
        // action null <=> reading
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
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(
                readingActivity.get(),
                "",
                readingActivity.get().getString(R.string.loading)
        );
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            fetch();
        } catch (IOException e) {
            Log.e(TAG, "Unknown exception", e);
            exception = e;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        Log.d(TAG, values[0]);
        progressDialog.setMessage(values[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        progressDialog.dismiss();
        if (exception != null) {
            // An error occurred
            Snackbar msgSnackbar = Snackbar.make(
                    readingActivity.get().findViewById(R.id.page_viewer),
                    readingActivity.get()
                            .getString(R.string.error_occurred)
                            + exception.getLocalizedMessage(),
                    Snackbar.LENGTH_INDEFINITE
            );
            ((TextView) msgSnackbar.getView()
                    .findViewById(android.support.design.R.id.snackbar_text))
                    .setMaxLines(5);
            msgSnackbar.show();
        } else {
            ((WebView) readingActivity.get().findViewById(R.id.page_viewer))
                    .loadUrl("file://" + saveLocation + filename);
        }
    }

    private void fetch() throws IOException {
        Log.d(TAG, "title = " + title + ", tag = " + tag);
        // Check cache
        if (!hasCache() || forceRefreshText) {
            wiki = new Wiki(Objects.requireNonNull(HttpUrl.parse(Config.API_ENDPOINT)));
            if (!wiki.exists(title))
                throw new IllegalArgumentException(readingActivity.get().getString(R.string.not_having) + " " + title);
            // If not existed, download and cache
            downloadText();
            preprocess();
            cacheText();
            downloadImages();
        }
    }

    private boolean hasCache() {
        if (tag == null) return false;
        File f = new File(saveLocation, filename);
        Log.d(TAG, f.toString());
        return f.exists();
    }

    private void downloadText() throws IOException {
        publishProgress(readingActivity.get().getString(R.string.downloading_text));
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
        publishProgress(readingActivity.get().getString(R.string.analyzing_text));
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
        publishProgress(readingActivity.get().getString(R.string.preprocessing_text));
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
        // Fix direct wiki images
        for (Element figure : doc.getElementsByTag("figure")) {
            Element realImg = figure.selectFirst("noscript > img");
            realImg.attr("src",
                    realImg.attr("src").replaceAll("/revision.*", ""));
            Log.d(TAG, realImg.attr("src"));
            figure.empty().appendChild(realImg);
            figure.attr("width", "80%");
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
                if (
                        href.startsWith("/wiki/Blog:")
                                || href.startsWith("/wiki/Blog_talk:")
                                || href.startsWith("/wiki/Board:")
                                || href.startsWith("/wiki/Board_Thread:")
                                || href.startsWith("/wiki/Category:")
                                || href.startsWith("/wiki/Category_talk:")
                                || href.startsWith("/wiki/File:")
                                || href.startsWith("/wiki/File_talk:")
                                || href.startsWith("/wiki/Help:")
                                || href.startsWith("/wiki/Help_talk:")
                                || href.startsWith("/wiki/Image:")
                                || href.startsWith("/wiki/Media:")
                                || href.startsWith("/wiki/MediaWiki:")
                                || href.startsWith("/wiki/MediaWiki_talk:")
                                || href.startsWith("/wiki/Message_Wall:")
                                || href.startsWith("/wiki/Message_Wall_Greeting:")
                                || href.startsWith("/wiki/Module:")
                                || href.startsWith("/wiki/Module_talk:")
                                || href.startsWith("/wiki/Project:")
                                || href.startsWith("/wiki/Project_talk:")
                                || href.startsWith("/wiki/Related_Videos:")
                                || href.startsWith("/wiki/Special:")
                                || href.startsWith("/wiki/Talk:")
                                || href.startsWith("/wiki/Template:")
                                || href.startsWith("/wiki/Template_talk:")
                                || href.startsWith("/wiki/Thread:")
                                || href.startsWith("/wiki/Topic:")
                                || href.startsWith("/wiki/User:")
                                || href.startsWith("/wiki/User_talk:")
                                || href.startsWith("/wiki/User_blog:")
                                || href.startsWith("/wiki/User_blog_comment:")
                ) {
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
        publishProgress(readingActivity.get().getString(R.string.preprocessing_display));
        doc.head()
                .appendElement("meta")
                .attr("charset", "utf-8");
        doc.head()
                .appendElement("meta")
                .attr("name", "viewport")
                .attr("content", "width=device-width, initial-scale=1");
        doc.head()
                .appendElement("link")
                .attr("rel", "stylesheet")
                .attr("type", "text/css")
                .attr("href", Config.SKIN);
        text = doc.outerHtml();
        Log.d(TAG, text);
    }

    private void cacheText() {
        publishProgress(readingActivity.get().getString(R.string.caching));
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

    private static final List<String> supportedImageExtensions = Arrays.asList("jpg", "jpeg", "png", "webp");

    private void downloadImages() {
        for (String imageName : imagesLinks.keySet()) {
            String url = imagesLinks.get(imageName);
            File file = new File(saveLocation, imageName);
            if (file.exists() && !forceRefreshImages) // already cached
                continue;

            publishProgress(readingActivity.get().getString(R.string.downloading_image) + " " + imageName);
            String mime = imageName.substring(imageName.lastIndexOf(".") + 1).toLowerCase();
            if (!supportedImageExtensions.contains(mime)) {
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
}
