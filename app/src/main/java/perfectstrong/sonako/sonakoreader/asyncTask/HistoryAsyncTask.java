package perfectstrong.sonako.sonakoreader.asyncTask;

import android.os.AsyncTask;

import java.util.List;

import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabase;
import perfectstrong.sonako.sonakoreader.database.Page;
import perfectstrong.sonako.sonakoreader.fragments.HistoryAdapter;

public class HistoryAsyncTask {

    public static class InitLoad extends AsyncTask<Void, Void, List<Page>> {
        private final LightNovelsDatabase lndb;
        private final HistoryAdapter adapter;

        public InitLoad(LightNovelsDatabase lndb,
                        HistoryAdapter adapter) {
            this.lndb = lndb;
            this.adapter = adapter;
        }

        @Override
        protected List<Page> doInBackground(Void... voids) {
            return lndb.historyDAO().getHistory();
        }

        @Override
        protected void onPostExecute(List<Page> pages) {
            adapter.setDatalist(pages);
        }
    }

    public static class Register extends AsyncTask<Page, Void, Void> {
        private final LightNovelsDatabase lndb;

        public Register(LightNovelsDatabase lndb) {
            this.lndb = lndb;
        }

        @Override
        protected Void doInBackground(Page... pages) {
            lndb.historyDAO().insert(pages);
            return null;
        }
    }
}
