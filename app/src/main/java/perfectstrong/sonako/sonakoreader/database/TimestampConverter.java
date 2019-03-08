package perfectstrong.sonako.sonakoreader.database;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

public class TimestampConverter {
    @TypeConverter
    Date fromTimestamp(Long value) {
        return new Date(value);
    }

    @TypeConverter
    Long fromDate(Date value) {
        return value.getTime();
    }
}
