package perfectstrong.sonako.sonakoreader.asyncTask;

import android.os.AsyncTask;

import perfectstrong.sonako.sonakoreader.database.LightNovel;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabase;

public class FavoriteLNsAsyncTask {

    public static class Register extends AsyncTask<LightNovel, Void, Void> {

        private LightNovelsDatabase lndb;

        public Register(LightNovelsDatabase lndb) {
            this.lndb = lndb;
        }

        @Override
        protected Void doInBackground(LightNovel... lightNovels) {
            lndb.lnDao().registerFavorites(lightNovels);
            return null;
        }
    }

    public static class Unregister extends AsyncTask<LightNovel, Void, Void> {

        private LightNovelsDatabase lndb;

        public Unregister(LightNovelsDatabase lndb) {
            this.lndb = lndb;
        }

        @Override
        protected Void doInBackground(LightNovel... lightNovels) {
            lndb.lnDao().unregisterFavorites(lightNovels);
            return null;
        }
    }
}
