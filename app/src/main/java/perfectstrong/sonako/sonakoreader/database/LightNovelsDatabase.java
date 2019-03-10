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

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE LightNovel ADD COLUMN isFavorite INTEGER NOT NULL default 0");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE LightNovel ADD COLUMN genres TEXT default ''");
            database.execSQL("DROP TABLE Categorization");
        }
    };
}

