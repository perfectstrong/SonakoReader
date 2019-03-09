package perfectstrong.sonako.sonakoreader.asyncTask;

import android.os.AsyncTask;

import perfectstrong.sonako.sonakoreader.database.LightNovel;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabase;

public class FavoriteLNsAsyncTask extends AsyncTask<Void, Void, Void> {

    private LightNovelsDatabase lndb;
    private LightNovel ln;
    private ACTION action;

    public FavoriteLNsAsyncTask(LightNovelsDatabase lndb, LightNovel ln, ACTION action) {
        this.lndb = lndb;
        this.ln = ln;
        this.action = action;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        switch (action) {
            case REGISTER:
                lndb.lnDao().registerFavorites(ln);
                break;
            case UNREGISTER:
                lndb.lnDao().unregisterFavorites(ln);
                break;
        }
        return null;
    }

    public enum ACTION {
        REGISTER,
        UNREGISTER
    }
}
