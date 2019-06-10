package perfectstrong.sonako.sonakoreader.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BiblioDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CachePage... cachePages);

    @Query("SELECT * FROM CachePage ORDER BY tag ASC, title ASC")
    LiveData<List<CachePage>> getLiveCaches();

    @Query("DELETE FROM CachePage WHERE tag = :tag")
    void clearTag(String tag);

    @Delete
    void clearChapters(CachePage... page);

    @Query("DELETE FROM CachePage")
    void clearAll();
}
