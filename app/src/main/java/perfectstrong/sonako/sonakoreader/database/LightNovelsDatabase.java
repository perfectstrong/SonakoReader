package perfectstrong.sonako.sonakoreader.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;

@Database(
        version = 3,
        entities = {
                LightNovel.class,
                Page.class
        },
        exportSchema = false
)
public abstract class LightNovelsDatabase extends RoomDatabase {
    public abstract LightNovelDAO lnDao();

    public abstract HistoryDAO historyDAO();
}

