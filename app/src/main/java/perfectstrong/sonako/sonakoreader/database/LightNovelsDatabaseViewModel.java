package perfectstrong.sonako.sonakoreader.database;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.os.AsyncTask;

import java.util.List;

public class LightNovelsDatabaseViewModel extends ViewModel {

    private final LiveData<List<LightNovel>> liveLNList;

    private LightNovelsDatabase lndb;

    public LightNovelsDatabaseViewModel(LightNovelsDatabase lndb) {
        this.lndb = lndb;
        liveLNList = this.lndb.lnDao().getAllLive();
    }

    public LiveData<List<LightNovel>> getLiveLNList() {
        return liveLNList;
    }

    public void registerFavorite(LightNovel... lightNovels) {
        new FavoriteLNsAsyncTask(lndb, ACTION.REGISTER).execute(lightNovels);
    }

    public void unregisterFavorite(LightNovel... lightNovels) {
        new FavoriteLNsAsyncTask(lndb, ACTION.UNREGISTER).execute(lightNovels);
    }

    private enum ACTION {
        REGISTER,
        UNREGISTER
    }

    private static class FavoriteLNsAsyncTask extends AsyncTask<LightNovel, Void, Void> {

        private final ACTION action;
        private final LightNovelsDatabase lndb;

        private FavoriteLNsAsyncTask(LightNovelsDatabase lndb, ACTION action) {
            this.lndb = lndb;
            this.action = action;
        }

        @Override
        protected Void doInBackground(LightNovel... lightNovels) {
            switch (action) {
                case REGISTER:
                    lndb.lnDao().registerFavorites(lightNovels);
                    break;
                case UNREGISTER:
                    lndb.lnDao().unregisterFavorites(lightNovels);
                    break;
            }
            return null;
        }
    }
}
