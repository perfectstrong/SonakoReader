package perfectstrong.sonako.sonakoreader.database;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.List;

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
        new FavoriteLNsAsyncTask(lndb, ACTION.REGISTER).execute(lightNovels);
    }

    public void unregisterFavorite(LightNovel... lightNovels) {
        new FavoriteLNsAsyncTask(lndb, ACTION.UNREGISTER).execute(lightNovels);
    }

    public void initLoadFavorites(LNListAdapter adapter) {
        new FavoriteLNsAsyncTask(
                lndb,
                ACTION.INIT_LOAD_FAVORITES,
                adapter
        ).execute();
    }

    private enum ACTION {
        INIT_LOAD_FAVORITES,
        REGISTER,
        UNREGISTER
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
                case REGISTER:
                    lndb.lnDao().registerFavorites(lightNovels);
                    break;
                case UNREGISTER:
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
                case REGISTER:
                    break;
                case UNREGISTER:
                    break;
            }
        }
    }
}
