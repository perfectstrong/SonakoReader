package perfectstrong.sonako.sonakoreader.database;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.annotation.NonNull;

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

