package perfectstrong.sonako.sonakoreader.helper;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.util.HashMap;
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

    public PageLoader(PageReadingActivity readingActivity, String title, String tag) {
        this.readingActivity = new WeakReference<>(readingActivity);
        this.title = title;
        this.tag = tag;
        this.saveLocation = Config.getSaveLocationForTag(tag);
        this.filename = Config.sanitize(title) + ".html";
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
        } catch (Exception e) {
            e.printStackTrace();
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
        if (!hasCache()) {
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
        publishProgress(readingActivity.get().getString(R.string.downloading_content));
        Wiki wiki = new Wiki(Objects.requireNonNull(HttpUrl.parse(Config.API_ENDPOINT)));
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
        publishProgress(readingActivity.get().getString(R.string.analyzing_content));
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
        publishProgress(readingActivity.get().getString(R.string.preprocessing_content));
        Document doc = Jsoup.parse(text.replaceAll("\\\\\"", "\""));
        // Remove unrelated section
        doc.getElementsByClass("entry-unrelated").remove();
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
            img.attr("data-src", src); // Backup
            String imgName = Config.getFileNameFromURL(src);
            if (!imgName.equals("")) {
                imagesLinks.put(imgName, src);
                img.attr("src", imgName); // To load from local
            }
        }
        // Fix empty tags
        for (Element element : doc.getElementsByTag("div")) {
            if (element.childNodeSize() == 0) element.remove();
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

    private void downloadImages() {
        for (String imageName : imagesLinks.keySet()) {
            String url = imagesLinks.get(imageName);
            Log.d(TAG, "imageName = " + imageName + ", link = " + url);
            Bitmap bitmap = null;
            try (InputStream inputStream = new URL(url).openStream()) {
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                Log.e(TAG, imageName + " downloading failed", e);
            }
            if (bitmap == null) continue;
            String mime = imageName.substring(imageName.lastIndexOf(".") + 1).toLowerCase();
            File file = new File(saveLocation, imageName);
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
