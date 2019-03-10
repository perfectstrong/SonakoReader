package perfectstrong.sonako.sonakoreader.asyncTask;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.GSONP;
import okhttp3.HttpUrl;
import okhttp3.Response;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.database.LightNovel;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabase;
import perfectstrong.sonako.sonakoreader.fragments.LNShowcaseAdapter;
import perfectstrong.sonako.sonakoreader.fragments.LNShowcaseFragment;
import perfectstrong.sonako.sonakoreader.helper.Config;

/**
 * Load titles from cache. If not downloaded yet, it will fetch from server then cache it.
 */
public class LNShowcaseAsyncLoader extends AsyncTask<Void, String, Void> {
    private final LightNovelsDatabase lndb;
    private List<LightNovel> titles;
    private Wiki wikiClient;
    private ProgressDialog progressDialog;
    private WeakReference<LNShowcaseFragment> fragment;
    private WeakReference<LNShowcaseAdapter> adapter;
    private Exception exception;

    public LNShowcaseAsyncLoader(LightNovelsDatabase lndb,
                                 LNShowcaseFragment context,
                                 LNShowcaseAdapter adapter) {
        this.lndb = lndb;
        this.fragment = new WeakReference<>(context);
        this.adapter = new WeakReference<>(adapter);
    }

    private void fetchTitles() throws IOException {
        // Check db first
        titles = lndb.lnDao().getAll();
        if (titles.size() == 0) {
            // Not download yet
            this.wikiClient = new Wiki(Objects.requireNonNull(HttpUrl.parse(Config.API_ENDPOINT)));
            downloadTitles();
            orderTitlesAlphabetically();
            removeTitleDuplications();
            cacheTitles();
            downloadStatusAndCategories();
        }
    }

    private void downloadTitles() throws IOException {
        publishProgress(fragment.get().getString(R.string.downloading_ln_list));
        downloadOfficialProjects();
        downloadTeaserProjects();
        downloadOLNProjects();
    }

    /**
     * Download unparsed text from project list then parse
     */
    private void downloadOfficialProjects() {
        String unparsedOfficialList = wikiClient.getPageText(Config.OFFICIAL_PROJECTS_LIST);
        //noinspection RegExpRedundantEscape
        Pattern p = Pattern.compile("\\[\\[([^|]*?)\\]\\]|\\[\\[([^|]*?)\\|[^|]*?\\]\\]", Pattern.UNICODE_CASE);
        Matcher m = p.matcher(unparsedOfficialList);
        while (m.find()) {
            String t = m.group(1);
            if (t == null) t = m.group(2);
            if (t.startsWith("Category:")) {
                continue;
            }
            LightNovel ln = new LightNovel(t);
            ln.setStatus(LightNovel.ProjectType.OFFICIAL);
            titles.add(ln);
        }
    }

    /**
     * Get projects with category "Teaser"
     */
    private void downloadTeaserProjects() throws IOException {
        Response res = wikiClient.basicGET("query",
                "list", "categorymembers",
                "cmtitle", "Category:Teaser",
                "cmnamespace", "0",
                "cmsort", "timestamp",
                "cmdir", "desc",
                "cmlimit", "max");
        assert res.body() != null;
        JsonObject jsonObject = GSONP.jp.parse(res.body().string()).getAsJsonObject();
        JsonArray teasers = jsonObject.getAsJsonObject("query").getAsJsonArray("categorymembers");
        for (JsonElement ele : teasers) {
            LightNovel teaser = new LightNovel(ele.getAsJsonObject().get("title").getAsString());
            teaser.setType(LightNovel.ProjectType.TEASER);
            titles.add(teaser);
        }
    }

    /**
     * Get projects with category "Original Light Novel"
     */
    private void downloadOLNProjects() throws IOException {
        Response res = wikiClient.basicGET("query",
                "list", "categorymembers",
                "cmtitle", "Category:Original Light Novel",
                "cmnamespace", "0",
                "cmsort", "timestamp",
                "cmdir", "desc",
                "cmlimit", "max");
        assert res.body() != null;
        JsonObject jsonObject = GSONP.jp.parse(res.body().string()).getAsJsonObject();
        JsonArray teasers = jsonObject.getAsJsonObject("query").getAsJsonArray("categorymembers");
        for (JsonElement ele : teasers) {
            LightNovel teaser = new LightNovel(ele.getAsJsonObject().get("title").getAsString());
            teaser.setType(LightNovel.ProjectType.TEASER);
            titles.add(teaser);
        }
    }

    private void orderTitlesAlphabetically() {
        publishProgress(fragment.get().getString(R.string.ordering_ln_list));
        for (int i = 0; i < titles.size() - 1; i++) {
            for (int j = i + 1; j < titles.size(); j++) {
                if (titles.get(i).getTitle().compareTo(titles.get(j).getTitle()) > 0) {
                    LightNovel ln = titles.get(i);
                    titles.set(i, titles.get(j));
                    titles.set(j, ln);
                }
            }
        }
    }

    private void removeTitleDuplications() {
        int i = 0;
        while (i < titles.size() - 1) {
            if (titles.get(i + 1).equals(titles.get(i))) {
                titles.remove(i + 1);
            } else {
                i++;
            }
        }
    }

    private void cacheTitles() {
        publishProgress(fragment.get().getString(R.string.caching_ln_list));
        lndb.lnDao().insert(titles.toArray(new LightNovel[0]));
    }

    private void downloadStatusAndCategories() {
        publishProgress(fragment.get().getString(R.string.downloading_tags));
        Map<String, LightNovel> mapLN = new HashMap<>();
        for (LightNovel lightNovel : titles) {
            mapLN.put(lightNovel.getTitle(), lightNovel);
        }
        HashMap<String, ArrayList<String>> result = MQuery.getCategoriesOnPage(
                wikiClient,
                mapLN.keySet()
        );
        for (String lntitle : result.keySet()) {
            ArrayList<String> categories = result.get(lntitle);
            LightNovel title = mapLN.get(lntitle);
            assert categories != null;
            for (String category : categories) {
                category = category.substring("Category:".length());
                assert title != null;
                switch (category) {
                    // Status
                    case LightNovel.ProjectStatus.ACTIVE:
                    case LightNovel.ProjectStatus.COMPLETED:
                    case LightNovel.ProjectStatus.IDLE:
                    case LightNovel.ProjectStatus.INACTIVE:
                    case LightNovel.ProjectStatus.STALLED:
                        title.setStatus(category);
                        break;
                    // Type
                    case LightNovel.ProjectType.TEASER:
                    case LightNovel.ProjectType.OLN:
                        title.setType(category);
                        break;
                    // Genres or tag
                    default:
                        if (LightNovel.ProjectGenre.ALL.contains(category))
                            title.getGenres().add(category);
                        else {
                            if (LightNovel.ExceptionTag.ALL.contains(category)) continue;
                            title.setTag(category);
                        }
                        break;
                }
            }
        }
        // Bulk update type and status db
        lndb.lnDao().update(titles.toArray(new LightNovel[0]));
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(fragment.get().getContext(),
                "",
                fragment.get().getString(R.string.loading)
        );
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            fetchTitles();
        } catch (Exception e) {
            e.printStackTrace();
            this.exception = e;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        progressDialog.setMessage(values[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        progressDialog.dismiss();
        if (exception != null) {
            // An error occurred
            Snackbar msgSnackbar = Snackbar.make(
                    Objects.requireNonNull(fragment.get()
                            .getView())
                            .findViewById(R.id.LNTitlesRecyclerView),
                    fragment.get().getString(R.string.error_occurred)
                            + exception.getLocalizedMessage(),
                    Snackbar.LENGTH_INDEFINITE
            );
            ((TextView) msgSnackbar.getView()
                    .findViewById(android.support.design.R.id.snackbar_text))
                    .setMaxLines(5);
            msgSnackbar.show();
        } else {
            adapter.get().setDatalist(titles);
        }
    }
}
