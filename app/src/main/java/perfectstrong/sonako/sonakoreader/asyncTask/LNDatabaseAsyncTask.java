package perfectstrong.sonako.sonakoreader.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.SonakoReaderApp;
import perfectstrong.sonako.sonakoreader.database.LightNovel;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabase;
import perfectstrong.sonako.sonakoreader.helper.Config;
import perfectstrong.sonako.sonakoreader.helper.Utils;
import perfectstrong.sonako.sonakoreader.helper.WikiClient;

public class LNDatabaseAsyncTask {

    private static final String TAG = LNDatabaseAsyncTask.class.getSimpleName();

    /**
     * Load titles from cache. If not downloaded yet, it will fetch from server then cache it.
     */
    public static class LoadCacheOrDownload extends AsyncTask<Void, String, Void> {
        private final LightNovelsDatabase lndb;
        private List<LightNovel> titles;
        private WikiClient wikiClient;
        private final boolean forceDownload;
        private Exception exception;

        public LoadCacheOrDownload(LightNovelsDatabase lndb,
                                   boolean forceDownload) {
            this.lndb = lndb;
            this.forceDownload = forceDownload;
        }

        private void downloadAll() {
            this.wikiClient = new WikiClient(Config.API_ENDPOINT, Config.USER_AGENT);
            titles = new ArrayList<>();

            Context context = SonakoReaderApp.getContext();

            publishProgress(context.getString(R.string.downloading_ln_list));
            downloadTitles();

            publishProgress(context.getString(R.string.downloading_tags));
            downloadStatusAndCategories(wikiClient, titles);

            publishProgress(context.getString(R.string.ordering_ln_list));
            orderTitlesAlphabetically(titles);

            publishProgress(context.getString(R.string.caching_ln_list));
            cacheLNDB(lndb, titles);
        }

        private void downloadTitles() {
            downloadOfficialProjects(wikiClient, titles);
            downloadTeaserProjects(wikiClient, titles);
            downloadOLNProjects(wikiClient, titles);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // Check db first
                titles = lndb.lnDao().getAll();
                if (titles.size() == 0 && forceDownload) {
                    // Connection check
                    Utils.checkConnection(SonakoReaderApp.getContext());

                    downloadAll();
                    titles = lndb.lnDao().getAll();
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                this.exception = e;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Toast.makeText(
                    SonakoReaderApp.getContext(),
                    values[0],
                    Toast.LENGTH_SHORT
            ).show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (exception != null) {
                // An error occurred
                Context context = SonakoReaderApp.getContext();
                Toast.makeText(
                        context,
                        context.getString(R.string.error_occurred) + " "
                                + exception.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
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
        private WikiClient wikiClient;
        private Exception exception;

        public Update(LightNovelsDatabase lndb) {
            this.lndb = lndb;
            mHandler = new Handler();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(
                    SonakoReaderApp.getContext(),
                    R.string.start_downloading,
                    Toast.LENGTH_SHORT
            ).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // Connection check
                Utils.checkConnection(SonakoReaderApp.getContext());

                // Real start
                this.wikiClient = new WikiClient(Config.API_ENDPOINT, Config.USER_AGENT);
                titles = new ArrayList<>();
                Context context = SonakoReaderApp.getContext();
                publishProgress(context.getString(R.string.downloading_ln_list));
                downloadOfficialProjects(wikiClient, titles);
                downloadTeaserProjects(wikiClient, titles);
                downloadOLNProjects(wikiClient, titles);

                publishProgress(context.getString(R.string.downloading_tags));
                downloadStatusAndCategories(wikiClient, titles);

                publishProgress(context.getString(R.string.ordering_ln_list));
                orderTitlesAlphabetically(titles);

                publishProgress(context.getString(R.string.caching_ln_list));
                cacheLNDB(lndb, titles);
            } catch (Exception e) {
                exception = e;
                Log.e(TAG, e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            mHandler.post(() -> Toast.makeText(
                    SonakoReaderApp.getContext(),
                    values[0],
                    Toast.LENGTH_SHORT
            ).show());
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Context context = SonakoReaderApp.getContext();
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
    private static void downloadOfficialProjects(WikiClient wikiClient,
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
    private static void downloadTeaserProjects(WikiClient wikiClient,
                                               List<LightNovel> titles) {
        List<String> teasers = wikiClient.getCategoryMembers(LightNovel.ProjectType.TEASER, "0", "max");
        for (String title: teasers) {
            LightNovel teaser = new LightNovel(title);
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
    private static void downloadOLNProjects(WikiClient wikiClient,
                                            List<LightNovel> titles) {
        List<String> olns = wikiClient.getCategoryMembers(LightNovel.ProjectType.OLN, "0", "max");
        for (String title: olns) {
            LightNovel teaser = new LightNovel(title);
            teaser.setType(LightNovel.ProjectType.OLN);
            titles.add(teaser);
        }
    }

    private static void orderTitlesAlphabetically(List<LightNovel> titles) {
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

    /**
     * Fetch status and categories
     *
     * @param wikiClient worker
     * @param titles     collector
     */
    private static void downloadStatusAndCategories(WikiClient wikiClient,
                                                    List<LightNovel> titles) {
        Map<String, LightNovel> mapLN = new HashMap<>();
        for (LightNovel lightNovel : titles) {
            mapLN.put(lightNovel.getTitle(), lightNovel);
        }
        Map<String, Set<String>> result = wikiClient.getCategoriesOnPages(new ArrayList<>(mapLN.keySet()));
        for (String lntitle : result.keySet()) {
            Set<String> categories = result.get(lntitle);
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

    private static void cacheLNDB(LightNovelsDatabase lndb,
                                  List<LightNovel> titles) {
        // Bulk update type and status db
        lndb.lnDao().insert(titles.toArray(new LightNovel[0]));
    }
}