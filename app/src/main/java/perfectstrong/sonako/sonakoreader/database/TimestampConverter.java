package perfectstrong.sonako.sonakoreader.database;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

@SuppressWarnings("WeakerAccess")
public class TimestampConverter {
    @TypeConverter
    public Date fromTimestamp(Long value) {
        return new Date(value);
    }

    @TypeConverter
    public Long fromDate(Date value) {
        return value.getTime();
    }
}
