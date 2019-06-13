package perfectstrong.sonako.sonakoreader.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import perfectstrong.sonako.sonakoreader.BuildConfig;
import perfectstrong.sonako.sonakoreader.SonakoReaderApp;

/**
 * Singleton class to access database
 */
public class LightNovelsDatabaseClient {
    private static final String DB_NAME = LightNovelsDatabase.class.getSimpleName() + ".db";
    private static volatile LightNovelsDatabase instance;
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Page" +
                    " ADD COLUMN currentReadingPosition REAL DEFAULT 0.0 NOT NULL");
        }
    };

    private static synchronized LightNovelsDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static LightNovelsDatabase create(final Context context) {
        RoomDatabase.Builder<LightNovelsDatabase> builder = Room.databaseBuilder(
                context,
                LightNovelsDatabase.class,
                DB_NAME
        )
                .addMigrations(MIGRATION_1_2);
        if (BuildConfig.DEBUG) {
            return builder
                    .fallbackToDestructiveMigration()
                    .build();
        } else
            return builder.build();
    }

    public static LightNovelsDatabase getInstance() {
        return getInstance(SonakoReaderApp.getContext());
    }
}
