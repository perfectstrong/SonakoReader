package perfectstrong.sonako.sonakoreader.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(
        version = 1,
        entities = {
                LightNovel.class,
                Volume.class,
                Chapter.class,
                Categorization.class
        },
        exportSchema = false
)
public abstract class LightNovelsDatabase extends RoomDatabase {
    public abstract LightNovelDAO lnDao();

    public abstract VolumnChapterDAO chaperDao();
}

