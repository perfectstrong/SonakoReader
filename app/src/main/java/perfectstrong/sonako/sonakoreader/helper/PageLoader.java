package perfectstrong.sonako.sonakoreader.helper;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.gson.JsonObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Objects;

import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.GSONP;
import okhttp3.HttpUrl;
import okhttp3.Response;
import perfectstrong.sonako.sonakoreader.PageReadingActivity;
import perfectstrong.sonako.sonakoreader.R;

public class PageLoader extends AsyncTask<Void, String, Void> {

    private WeakReference<PageReadingActivity> readingActivity;
    private final String title;
    private String text;
    private ProgressDialog progressDialog;
    private Exception exception;

    public PageLoader(PageReadingActivity readingActivity, String title) {
        this.readingActivity = new WeakReference<>(readingActivity);
        this.title = title;
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
                    .loadDataWithBaseURL(
                            Config.ASSETS_DIR,
                            text,
                            "text/html",
                            "utf-8",
                            null);
        }
    }

    private void fetch() throws IOException {
        // Check cache
        // TODO
        // If not existed, download
        download();
        preprocess();
        cache();
    }

    private void download() throws IOException {
        Wiki wiki = new Wiki(Objects.requireNonNull(HttpUrl.parse(Config.API_ENDPOINT)));
        Response res = wiki.basicGET(
                "parse",
                "page", title,
                "format", "json",
                "rvprop", "content",
                "disablelimitreport", "true",
                "disabletoc", "true",
                "prop", "text",
                "useskin", "mercury"
        );
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
    }

    private void preprocess() {
        Document doc = Jsoup.parse(text.replaceAll("\\\\\"", "\""));
        // Remove unrelated section
        doc.getElementsByClass("entry-unrelated").remove();
        // Remove edit section link
        doc.getElementsByClass("editsection").remove();
        // Fix images
        for (Element figure: doc.getElementsByTag("figure")) {
            Element a = figure.getElementsByTag("a").first();
            a.attr("href", a.attr("href").replace("/wiki/", ""));
            Element realImg = a.selectFirst("noscript > img");
            realImg.attr("href", realImg.attr("href").replace("\\?cb=.*", ""));
            a.empty().appendChild(realImg);
        }
        // TODO more
        // Add head
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
        System.out.println(text);
    }

    private void cache() {
        // TODO cache preprocessed html and images, with _text
    }
}
