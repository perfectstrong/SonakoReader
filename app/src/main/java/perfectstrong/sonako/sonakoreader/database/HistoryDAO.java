package perfectstrong.sonako.sonakoreader.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface HistoryDAO {

    @Query("SELECT * FROM Page ORDER BY lastRead DESC LIMIT :lim")
    List<Page> getRecentHistory(int lim);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Page page);
}
