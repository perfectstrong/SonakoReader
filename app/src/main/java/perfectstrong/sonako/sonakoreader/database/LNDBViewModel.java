package perfectstrong.sonako.sonakoreader.database;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class LNDBViewModel extends AndroidViewModel {

    private LightNovelsDatabase lndb;

    public LNDBViewModel(@NonNull Application application) {
        super(application);
    }

    public void setLndb(LightNovelsDatabase lndb) {
        this.lndb = lndb;
    }

    public LiveData<List<LightNovel>> getLiveFavoritesLNList() {
        return lndb.lnDao().getAllFavoritesLive();
    }

    public void registerFavorite(LightNovel... lightNovels) {
        new FavoriteLNsAsyncTask(lndb, ACTION.REGISTER_FAVORITE).execute(lightNovels);
    }

    public void unregisterFavorite(LightNovel... lightNovels) {
        new FavoriteLNsAsyncTask(lndb, ACTION.UNREGISTER_FAVORITE).execute(lightNovels);
    }

    public LiveData<List<Page>> getLiveHistory() {
        return lndb.historyDAO().getLiveHistory();
    }


    public LiveData<List<LightNovel>> getLiveLNList() {
        return lndb.lnDao().getLiveAll();
    }

    private enum ACTION {
        REGISTER_FAVORITE,
        UNREGISTER_FAVORITE,
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
                case REGISTER_FAVORITE:
                    lndb.lnDao().registerFavorites(lightNovels);
                    break;
                case UNREGISTER_FAVORITE:
                    lndb.lnDao().unregisterFavorites(lightNovels);
                    break;
            }
            return null;
        }
    }
}