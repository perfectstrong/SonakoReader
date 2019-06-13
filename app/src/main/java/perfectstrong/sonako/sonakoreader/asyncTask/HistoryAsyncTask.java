package perfectstrong.sonako.sonakoreader.asyncTask;

import android.os.AsyncTask;

import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabase;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabaseClient;
import perfectstrong.sonako.sonakoreader.database.Page;

public class HistoryAsyncTask {

    public static class Register extends AsyncTask<Page, Void, Void> {
        private final LightNovelsDatabase lndb;

        public Register() {
            this.lndb = LightNovelsDatabaseClient.getInstance();
        }

        @Override
        protected Void doInBackground(Page... pages) {
            lndb.historyDAO().insert(pages);
            return null;
        }
    }

    public static class Clear extends AsyncTask<Void, Void, Void> {
        private final LightNovelsDatabase lndb;

        public Clear() {
            this.lndb = LightNovelsDatabaseClient.getInstance();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            lndb.historyDAO().clear();
            return null;
        }
    }

    public static class LookUp extends AsyncTask<Void, Void, Page> {
        private final OnLookUpCallback callback;
        private String title;

        public LookUp(String title,
                      OnLookUpCallback callback) {
            this.title = title;
            this.callback = callback;
        }

        @Override
        protected Page doInBackground(Void... voids) {
            return LightNovelsDatabaseClient.getInstance().historyDAO().getPage(title);
        }

        @Override
        protected void onPostExecute(Page page) {
            this.callback.execute(page);
        }

        public interface OnLookUpCallback {
            void execute(Page page);
        }
    }
}