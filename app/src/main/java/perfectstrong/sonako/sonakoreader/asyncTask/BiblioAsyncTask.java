package perfectstrong.sonako.sonakoreader.asyncTask;

import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.SonakoReaderApp;
import perfectstrong.sonako.sonakoreader.database.CachePage;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabase;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabaseClient;
import perfectstrong.sonako.sonakoreader.helper.Utils;

public class BiblioAsyncTask {
    /**
     * Tag name of ln
     */
    public static class ScanSaveDirectory extends AsyncTask<String, Integer, Void> {

        @Override
        protected void onProgressUpdate(Integer... values) {
            Toast.makeText(
                    SonakoReaderApp.getContext(),
                    values[0],
                    Toast.LENGTH_SHORT
            ).show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            publishProgress(R.string.start_scanning);
            LightNovelsDatabase lndb = LightNovelsDatabaseClient.getInstance();

            if (strings.length == 0) {
                // Total scan and re-update
                File saveDir = new File(Utils.getSaveDir());
                // Get all subfolders
                File[] dirs = saveDir.listFiles(File::isDirectory);
                if (dirs == null || dirs.length == 0) {
                    publishProgress(R.string.biblio_empty);
                    return null;
                }
                List<CachePage> allCachePages = new ArrayList<>();
                for (File dir : dirs) {
                    allCachePages.addAll(scanTag(dir));
                }

                // Clear database
                publishProgress(R.string.clear_biblio);
                lndb.biblioDAO().clearAll();

                // Insert to database
                publishProgress(R.string.update_biblio);
                lndb.biblioDAO().insert(allCachePages.toArray(new CachePage[0]));
            } else {
                for (String tag : strings) {
                    File tagDir = new File(Utils.getSaveDirForTag(tag));
                    List<CachePage> caches = scanTag(tagDir);
                    if (!caches.isEmpty()) {
                        lndb.biblioDAO().clearTag(tag);
                        lndb.biblioDAO().insert(caches.toArray(new CachePage[0]));
                    }
                }
                publishProgress(R.string.rescan_ln_done);
            }
            return null;
        }

        List<CachePage> scanTag(File dir) {
            if (!dir.exists() || !dir.isDirectory())
                return new ArrayList<>();
            String tag = dir.getName();
            File[] htmlFiles = dir.listFiles(pathname -> pathname.getName().endsWith(".html"));
            List<CachePage> cachePages = new ArrayList<>();
            // Suppose each html is cache page
            for (File html : htmlFiles) {
                String name = html.getName();
                String title = name.substring(0, name.lastIndexOf(".html"));
                cachePages.add(new CachePage(
                        title,
                        tag,
                        new Date(html.lastModified())
                ));
            }
            return cachePages;
        }
    }

    public static class Register extends AsyncTask<CachePage, Void, Void> {

        @Override
        protected Void doInBackground(CachePage... cachePages) {
            LightNovelsDatabaseClient.getInstance()
                    .biblioDAO()
                    .insert(cachePages);
            return null;
        }
    }

    public static class Update extends ScanSaveDirectory {

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(
                    SonakoReaderApp.getContext(),
                    R.string.update_completed,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public static class Clear extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            LightNovelsDatabaseClient.getInstance()
                    .biblioDAO()
                    .clearAll();
            return null;
        }
    }
}
