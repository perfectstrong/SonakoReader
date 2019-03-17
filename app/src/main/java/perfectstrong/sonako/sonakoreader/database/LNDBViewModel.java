package perfectstrong.sonako.sonakoreader.database;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import perfectstrong.sonako.sonakoreader.asyncTask.HistoryAsyncTask;
import perfectstrong.sonako.sonakoreader.fragments.HistoryAdapter;
import perfectstrong.sonako.sonakoreader.fragments.LNListAdapter;

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

    public void initLoadFavorites(LNListAdapter adapter) {
        new FavoriteLNsAsyncTask(
                lndb,
                ACTION.INIT_LOAD_FAVORITES,
                adapter
        ).execute();
    }

    public LiveData<List<Page>> getLiveHistory() {
        return lndb.historyDAO().getLiveHistory();
    }


    public void initLoadHistory(HistoryAdapter mAdapter) {
        new HistoryAsyncTask.InitLoad(
                lndb,
                mAdapter
        ).execute();
    }

    public LiveData<List<LightNovel>> getLiveLNList() {
        return lndb.lnDao().getLiveAll();
    }

    private enum ACTION {
        INIT_LOAD_FAVORITES,
        REGISTER_FAVORITE,
        UNREGISTER_FAVORITE,
    }

    private static class FavoriteLNsAsyncTask extends AsyncTask<LightNovel, Void, Void> {

        private final ACTION action;
        private final LightNovelsDatabase lndb;
        private LNListAdapter adapter;
        private List<LightNovel> data;


        private FavoriteLNsAsyncTask(LightNovelsDatabase lndb, ACTION action) {
            this.lndb = lndb;
            this.action = action;
        }

        private FavoriteLNsAsyncTask(LightNovelsDatabase lndb, ACTION action, LNListAdapter adapter) {
            this(lndb, action);
            this.adapter = adapter;
        }

        @Override
        protected Void doInBackground(LightNovel... lightNovels) {
            switch (action) {
                case INIT_LOAD_FAVORITES:
                    data = lndb.lnDao().getAllFavorites();
                    break;
                case REGISTER_FAVORITE:
                    lndb.lnDao().registerFavorites(lightNovels);
                    break;
                case UNREGISTER_FAVORITE:
                    lndb.lnDao().unregisterFavorites(lightNovels);
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            switch (action) {
                case INIT_LOAD_FAVORITES:
                    adapter.setDatalist(data);
                    break;
                case REGISTER_FAVORITE:
                    break;
                case UNREGISTER_FAVORITE:
                    break;
            }
        }
    }

}
