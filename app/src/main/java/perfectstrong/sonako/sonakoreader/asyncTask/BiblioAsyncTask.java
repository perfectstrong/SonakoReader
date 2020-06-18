package perfectstrong.sonako.sonakoreader.asyncTask;

import android.os.AsyncTask;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.SonakoReaderApp;
import perfectstrong.sonako.sonakoreader.database.BiblioDAO;
import perfectstrong.sonako.sonakoreader.database.CachePage;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabase;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabaseClient;
import perfectstrong.sonako.sonakoreader.helper.Utils;

public class BiblioAsyncTask {
    private static void delete(String lnTag) {
        File lnDir = new File(Utils.getSaveDirForTag(lnTag));
        if (!lnDir.exists() || !lnDir.isDirectory())
            return;
        deleteRecursive(lnDir);
    }

    private static void deleteRecursive(File f) {
        if (f.isDirectory()) {
            for (File child : Objects.requireNonNull(f.listFiles())) {
                deleteRecursive(child);
            }
        }
        //noinspection ResultOfMethodCallIgnored
        f.delete();
    }

    /**
     * Tag name of ln
     */
    public static class ScanSaveDirectory extends StaticMessageUpdateAsyncTask<String, Void> {

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
            for (File html : Objects.requireNonNull(htmlFiles)) {
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

    public static class Clear extends StaticMessageUpdateAsyncTask<Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            publishProgress(R.string.delete_biblio_start);
            File lnsDir = new File(Utils.getSaveDir());
            if (!lnsDir.exists() || !lnsDir.isDirectory())
                return null;
            LightNovelsDatabaseClient.getInstance()
                    .biblioDAO()
                    .clearAll();
            for (File lnTag : Objects.requireNonNull(lnsDir.listFiles((dir, name) -> dir.isDirectory()))) {
                deleteRecursive(lnTag);
            }
            publishProgress(R.string.delete_biblio_end);
            return null;
        }
    }

    public static class DeleteLNTag extends StaticMessageUpdateAsyncTask<String, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            publishProgress(R.string.delete_ln_start);
            for (String lnTag : strings) {
                // Delete on disk
                delete(lnTag);
                // Delete entry
                LightNovelsDatabaseClient.getInstance().biblioDAO().clearTag(lnTag);
            }
            publishProgress(R.string.delete_ln_end);
            return null;
        }
    }

    public static class DeleteCachedPage extends StaticMessageUpdateAsyncTask<CachePage, Void> {
        @Override
        protected Void doInBackground(CachePage... cachePages) {
            publishProgress(R.string.delete_chapter_start);
            BiblioDAO dao = LightNovelsDatabaseClient.getInstance().biblioDAO();
            for (CachePage page : cachePages) {
                // Delete entry
                dao.clearChapters(page);
                // Delete on disk
                File f = Utils.getCachedTextFile(page);
                if (f.exists())
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
            }
            publishProgress(R.string.delete_chapter_end);
            return null;
        }
    }

    public static class DeleteCachedPageWithImages extends StaticMessageUpdateAsyncTask<CachePage, Void> {

        @Override
        protected Void doInBackground(CachePage... cachePages) {
            publishProgress(R.string.delete_chapter_start);
            BiblioDAO dao = LightNovelsDatabaseClient.getInstance().biblioDAO();
            for (CachePage page : cachePages) {
                // Delete entry
                dao.clearChapters(page);
                // Find included images in page
                String pageDir = Utils.getSaveDirForTag(page.getTag());
                for (String imgName : findIncludedImages(page)) {
                    File imgPath = new File(pageDir + imgName);
                    if (imgPath.exists())
                        //noinspection ResultOfMethodCallIgnored
                        imgPath.delete();
                }
                // Delete page text
                File f = Utils.getCachedTextFile(page);
                if (f.exists())
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
            }
            publishProgress(R.string.delete_biblio_end);
            return null;
        }

        List<String> findIncludedImages(CachePage page) {
            File f = Utils.getCachedTextFile(page);
            if (f.exists()) {
                try {
                    Document doc = Jsoup.parse(f, "UTF-8");
                    List<String> imagesLinks = new ArrayList<>();
                    for (Element img : doc.getElementsByTag("img")) {
                        if (img.hasAttr("src")) {
                            imagesLinks.add(img.attr("src"));
                        }
                    }
                    return imagesLinks;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return new ArrayList<>();
        }
    }
}