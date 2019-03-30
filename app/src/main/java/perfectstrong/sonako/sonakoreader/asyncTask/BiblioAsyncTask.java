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
    public static class ScanSaveDirectory extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onProgressUpdate(Integer... values) {
            Toast.makeText(
                    SonakoReaderApp.getContext(),
                    values[0],
                    Toast.LENGTH_SHORT
            ).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            publishProgress(R.string.start_scanning);
            LightNovelsDatabase lndb = LightNovelsDatabaseClient.getInstance();

            File saveDir = new File(Utils.getSaveDir());
            // Get all subfolders
            File[] dirs = saveDir.listFiles(File::isDirectory);
            List<CachePage> cachePages = new ArrayList<>();
            for (File dir : dirs) {
                String tag = dir.getName();
                File[] htmlFiles = dir.listFiles(pathname -> pathname.getName().endsWith(".html"));
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
            }

            // Clear database
            publishProgress(R.string.clear_biblio);
            lndb.biblioDAO().clearAll();

            // Insert to database
            publishProgress(R.string.update_biblio);
            lndb.biblioDAO().insert(cachePages.toArray(new CachePage[0]));

            return null;
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
}
