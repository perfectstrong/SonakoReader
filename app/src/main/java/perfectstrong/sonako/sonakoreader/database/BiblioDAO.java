package perfectstrong.sonako.sonakoreader.database;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface BiblioDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CachePage... cachePages);

    @Update
    void update(CachePage... cachePages);

    @Query("SELECT * FROM CachePage ORDER BY tag ASC, title ASC")
    LiveData<List<CachePage>> getLiveCaches();

    @Delete
    void delete(CachePage... cachePages);

    @Query("DELETE FROM CachePage")
    void clearAll();
}
