package perfectstrong.sonako.sonakoreader.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public abstract class LightNovelDAO {

    @Query("SELECT * FROM lightnovel ORDER BY title ASC")
    public abstract List<LightNovel> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

    @Query("DELETE FROM lightnovel")
    public abstract void clear();

    @Query("SELECT * FROM lightnovel ORDER BY title ASC")
    public abstract LiveData<List<LightNovel>> getLiveAll();
}
