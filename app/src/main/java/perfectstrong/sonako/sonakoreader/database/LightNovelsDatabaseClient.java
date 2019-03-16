package perfectstrong.sonako.sonakoreader.database;

import android.content.Context;

import androidx.room.Room;

/**
 * Singleton class to access database
 */
public class LightNovelsDatabaseClient {
    private static final String DB_NAME = LightNovelsDatabase.class.getSimpleName() + ".db";
    private static volatile LightNovelsDatabase instance;

    public static synchronized LightNovelsDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static LightNovelsDatabase create(final Context context) {
        return Room.databaseBuilder(
                context,
                LightNovelsDatabase.class,
                DB_NAME
        )
                .build();
    }

}
