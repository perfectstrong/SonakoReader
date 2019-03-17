package perfectstrong.sonako.sonakoreader.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
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

public class LNDatabaseAsyncTask {

    private static final String TAG = LNDatabaseAsyncTask.class.getSimpleName();

    /**
     * Load titles from cache. If not downloaded yet, it will fetch from server then cache it.
     */
    public static class LoadCacheOrDownload extends AsyncTask<Void, String, Void> {
        private final LightNovelsDatabase lndb;
        private List<LightNovel> titles;
        private Wiki wikiClient;
        private WeakReference<LNShowcaseFragment> fragment;
        private WeakReference<LNShowcaseAdapter> adapter;
        private final boolean forceDownload;
        private Exception exception;

        public LoadCacheOrDownload(LightNovelsDatabase lndb,
                                   LNShowcaseFragment context,
                                   LNShowcaseAdapter adapter,
                                   boolean forceDownload) {
            this.lndb = lndb;
            this.fragment = new WeakReference<>(context);
            this.adapter = new WeakReference<>(adapter);
            this.forceDownload = forceDownload;
        }

        private void fetchTitles() throws IOException {
            // Check db first
            if (forceDownload)
                downloadAll();
            else {
                titles = lndb.lnDao().getAll();
            }
        }

        private void downloadAll() throws IOException {
            this.wikiClient = new Wiki(Objects.requireNonNull(HttpUrl.parse(Config.API_ENDPOINT)));
            titles = new ArrayList<>();
            downloadTitles();
            orderTitlesAlphabetically();
            cacheTitles();
            publishProgress(fragment.get().getString(R.string.downloading_tags));
            downloadStatusAndCategories(wikiClient, titles);
            updateLNDB(lndb, titles);
        }

        private void downloadTitles() throws IOException {
            publishProgress(fragment.get().getString(R.string.downloading_ln_list));
            downloadOfficialProjects(wikiClient, titles);
            downloadTeaserProjects(wikiClient, titles);
            downloadOLNProjects(wikiClient, titles);
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

        private void cacheTitles() {
            publishProgress(fragment.get().getString(R.string.caching_ln_list));
            lndb.lnDao().insert(titles.toArray(new LightNovel[0]));
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                fetchTitles();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                this.exception = e;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (fragment.get().getContext() != null)
                Toast.makeText(
                        fragment.get().getContext(),
                        values[0],
                        Toast.LENGTH_SHORT
                ).show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            View fragmentView = fragment.get().getView();
            if (fragmentView == null) return;
            if (exception != null) {
                // An error occurred
                Snackbar msgSnackbar = Snackbar.make(
                        fragmentView.findViewById(R.id.LNTitlesRecyclerView),
                        fragment.get().getString(R.string.error_occurred)
                                + exception.getLocalizedMessage(),
                        Snackbar.LENGTH_INDEFINITE
                );
                ((TextView) msgSnackbar.getView()
                        .findViewById(R.id.snackbar_text))
                        .setMaxLines(5);
                msgSnackbar.show();
            } else {
                adapter.get().setDatalist(titles);
                if (forceDownload || titles.size() != 0) {
                    fragmentView.findViewById(R.id.LNTitlesNoDatabaseGroup).setVisibility(View.GONE);
                    fragmentView.findViewById(R.id.LNTitlesRecyclerView).setVisibility(View.VISIBLE);
                } else {
                    fragmentView.findViewById(R.id.LNTitlesNoDatabaseGroup).setVisibility(View.VISIBLE);
                    fragmentView.findViewById(R.id.LNTitlesRecyclerView).setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * Update light novel database
     */
    public static class Update extends AsyncTask<Void, String, Void> {
        private final LightNovelsDatabase lndb;
        private final Handler mHandler;
        private List<LightNovel> titles;
        private Wiki wikiClient;
        private final WeakReference<Context> _context;
        private Exception exception;

        public Update(LightNovelsDatabase lndb,
                      Context context) {
            this.lndb = lndb;
            _context = new WeakReference<>(context);
            mHandler = new Handler();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Context context = _context.get();
            if (context == null) return;
            Toast.makeText(
                    context,
                    R.string.start_downloading,
                    Toast.LENGTH_SHORT
            ).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                this.wikiClient = new Wiki(Objects.requireNonNull(HttpUrl.parse(Config.API_ENDPOINT)));
                titles = new ArrayList<>();
                publishProgress(_context.get().getString(R.string.downloading_ln_list));
                downloadOfficialProjects(wikiClient, titles);
                downloadTeaserProjects(wikiClient, titles);
                downloadOLNProjects(wikiClient, titles);
                publishProgress(_context.get().getString(R.string.downloading_tags));
                downloadStatusAndCategories(wikiClient, titles);
                updateLNDB(lndb, titles);
            } catch (Exception e) {
                exception = e;
                Log.e(TAG, e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (_context.get() != null)
                mHandler.post(() -> Toast.makeText(
                        _context.get(),
                        values[0],
                        Toast.LENGTH_SHORT
                        ).show()
                );
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (_context.get() != null) {
                Context context = _context.get();
                if (exception == null) {
                    Toast.makeText(
                            context,
                            R.string.update_completed,
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(
                            context,
                            context.getString(R.string.error_occurred) + " " + exception.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        }
    }

    /**
     * Clear all entries of light novel database
     */
    public static class Clear extends AsyncTask<Void, Void, Void> {
        private final LightNovelsDatabase lndb;

        public Clear(LightNovelsDatabase lndb) {
            this.lndb = lndb;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            lndb.lnDao().clear();
            return null;
        }
    }

    /**
     * Download unparsed text from project list then parse
     *
     * @param wikiClient worker
     * @param titles     collector
     */
    private static void downloadOfficialProjects(Wiki wikiClient,
                                                 List<LightNovel> titles) {
        String unparsedOfficialList = wikiClient.getPageText(Config.OFFICIAL_PROJECTS_LIST);
        //noinspection RegExpRedundantEscape
        Pattern p = Pattern.compile("\\[\\[([^|]*?)\\]\\]|\\[\\[([^|]*?)\\|[^|]*?\\]\\]",
                Pattern.UNICODE_CASE);
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
     *
     * @param wikiClient worker
     * @param titles     collector
     */
    private static void downloadTeaserProjects(Wiki wikiClient,
                                               List<LightNovel> titles) throws IOException {
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
     *
     * @param wikiClient worker
     * @param titles     collector
     */
    private static void downloadOLNProjects(Wiki wikiClient,
                                            List<LightNovel> titles) throws IOException {
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

    /**
     * Fetch status and categories
     *
     * @param wikiClient worker
     * @param titles     collector
     */
    private static void downloadStatusAndCategories(Wiki wikiClient,
                                                    List<LightNovel> titles) {
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
    }

    private static void updateLNDB(LightNovelsDatabase lndb,
                                   List<LightNovel> titles) {
        // Bulk update type and status db
        lndb.lnDao().insert(titles.toArray(new LightNovel[0]));
    }
}