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
}
