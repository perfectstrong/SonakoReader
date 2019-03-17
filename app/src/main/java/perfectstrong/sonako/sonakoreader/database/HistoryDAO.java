package perfectstrong.sonako.sonakoreader.database;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface HistoryDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Page... pages);

    @Query("SELECT * FROM Page ORDER BY lastRead DESC")
    LiveData<List<Page>> getLiveHistory();

    @Query("SELECT * FROM Page ORDER BY lastRead DESC")
    List<Page> getHistory();

    @Query("DELETE FROM Page")
    void clear();
}
