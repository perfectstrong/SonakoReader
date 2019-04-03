package perfectstrong.sonako.sonakoreader.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
        version = 1,
        entities = {
                LightNovel.class,
                Page.class,
                CachePage.class
        },
        exportSchema = false
)
public abstract class LightNovelsDatabase extends RoomDatabase {
    public abstract LightNovelDAO lnDao();

    public abstract HistoryDAO historyDAO();

    public abstract BiblioDAO biblioDAO();
}

