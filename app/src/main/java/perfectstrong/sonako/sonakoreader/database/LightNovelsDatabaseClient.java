package perfectstrong.sonako.sonakoreader.database;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import perfectstrong.sonako.sonakoreader.BuildConfig;
import perfectstrong.sonako.sonakoreader.SonakoReaderApp;

/**
 * Singleton class to access database
 */
public class LightNovelsDatabaseClient {
    private static final String DB_NAME = LightNovelsDatabase.class.getSimpleName() + ".db";
    private static volatile LightNovelsDatabase instance;

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
        );
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
