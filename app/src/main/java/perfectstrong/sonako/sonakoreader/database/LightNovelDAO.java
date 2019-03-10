package perfectstrong.sonako.sonakoreader.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public abstract class LightNovelDAO {

    @Query("SELECT * FROM lightnovel")
    public abstract List<LightNovel> getAll();

    @Insert
    public abstract void insert(LightNovel... lightNovels);

    @Update
    public abstract void update(LightNovel... lightNovels);

    @Transaction
    public void registerFavorites(LightNovel... lightNovels) {
        for (LightNovel ln : lightNovels) {
            ln.setFavorite(true);
        }
        update(lightNovels);
    }

    @Transaction
    public void unregisterFavorites(LightNovel... lightNovels) {
        for (LightNovel ln : lightNovels) {
            ln.setFavorite(false);
        }
        update(lightNovels);
    }

    @Query("SELECT * FROM lightnovel WHERE isFavorite = 1")
    public abstract LiveData<List<LightNovel>> getAllFavoritesLive();

    @Query("SELECT * FROM lightnovel WHERE isFavorite = 1")
    public abstract List<LightNovel> getAllFavorites();
}
