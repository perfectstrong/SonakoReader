package perfectstrong.sonako.sonakoreader.database;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import perfectstrong.sonako.sonakoreader.asyncTask.FavoriteLNsAsyncTask;

public class LNDBViewModel extends AndroidViewModel {

    private LightNovelsDatabase lndb;

    public LNDBViewModel(@NonNull Application application) {
        super(application);
        lndb = LightNovelsDatabaseClient.getInstance();
    }

    public void setLndb(LightNovelsDatabase lndb) {
        this.lndb = lndb;
    }

    public LiveData<List<LightNovel>> getLiveFavoritesLNList() {
        return lndb.lnDao().getAllFavoritesLive();
    }

    public void registerFavorite(LightNovel... lightNovels) {
        new FavoriteLNsAsyncTask.Register(lndb).execute(lightNovels);
    }

    public void unregisterFavorite(LightNovel... lightNovels) {
        new FavoriteLNsAsyncTask.Unregister(lndb).execute(lightNovels);
    }

    public LiveData<List<Page>> getLiveHistory() {
        return lndb.historyDAO().getLiveHistory();
    }

    public LiveData<List<LightNovel>> getLiveLNList() {
        return lndb.lnDao().getLiveAll();
    }

    public LiveData<List<CachePage>> getLiveCaches() {
        return lndb.biblioDAO().getLiveCaches();
    }

}