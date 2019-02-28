package perfectstrong.sonako.sonakoreader.fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.widget.TextView;

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
import okhttp3.HttpUrl;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.database.Categorization;
import perfectstrong.sonako.sonakoreader.database.LightNovel;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabase;
import perfectstrong.sonako.sonakoreader.helper.Config;

/**
 * Load titles from cache. If not downloaded yet, it will fetch from server then cache it.
 */
public class LNTitlesLoader extends AsyncTask<Void, Void, Void> {
    private final LightNovelsDatabase lndb;
    private List<LightNovel> titles;
    private Wiki wikiClient;
    private ProgressDialog progressDialog;
    private WeakReference<LNShowcaseFragment> fragment;
    private WeakReference<LNTitlesAdapter> adapter;
    private Exception exception;

    LNTitlesLoader(LightNovelsDatabase lndb,
                   LNShowcaseFragment context,
                   LNTitlesAdapter adapter) {
        this.lndb = lndb;
        this.fragment = new WeakReference<>(context);
        this.adapter = new WeakReference<>(adapter);
    }

    private void fetchTitles() {
        // Check db first
        titles = lndb.lnDao().getAll();
        if (titles.size() == 0) {
            // Not download yet
            downloadTitles();
            cacheTitles();
            downloadStatusAndCategories();
        }
    }

    private void downloadTitles() {
        // Official projects
        // Download unparsed text then parse into list
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
        // Teaser projects
        // TODO
        // OLN projects
        // TODO
    }

    private void cacheTitles() {
        lndb.lnDao().insert(titles.toArray(new LightNovel[0]));
    }

    private void fetchStatusAndCategories() {
        // Categories
        for (LightNovel lightNovel : titles) {
            lightNovel.setGenres(lndb.lnDao().getGenres(lightNovel.getTitle()));
        }
        // Status is set beforehand
    }

    private void downloadStatusAndCategories() {
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
                            lndb.lnDao().insert(new Categorization(lntitle, category));
                        else {
                            title.setTag(category);
                        }
                        break;
                }
            }
        }
        // Bulk update
        lndb.lnDao().update(titles.toArray(new LightNovel[0]));
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(fragment.get().getContext(),
                "",
                fragment.get().getString(R.string.loading_titles)
        );
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            this.wikiClient = new Wiki(Objects.requireNonNull(HttpUrl.parse(Config.API_ENDPOINT)));
            fetchTitles();
            fetchStatusAndCategories();
        } catch (Exception e) {
            e.printStackTrace();
            this.exception = e;
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
