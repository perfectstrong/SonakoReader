package perfectstrong.sonako.sonakoreader.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface HistoryDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Page... pages);

    @Query("SELECT * FROM Page ORDER BY lastRead DESC")
    LiveData<List<Page>> getLiveHistory();

    @Query("SELECT * FROM Page ORDER BY lastRead DESC")
    List<Page> getHistory();
}
