package perfectstrong.sonako.sonakoreader.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity(
        primaryKeys = {"title"}
)
@TypeConverters(TimestampConverter.class)
public class Page extends Item {
    @NonNull
    private Date lastRead;
    private float currentReadingPosition;

    public Page(@NonNull String title,
                @NonNull String tag,
                @NonNull Date lastRead,
                float currentReadingPosition) {
        this.title = title;
        this.tag = tag;
        this.lastRead = lastRead;
        this.currentReadingPosition = currentReadingPosition;
    }

    @NonNull
    public Date getLastRead() {
        return lastRead;
    }

    public void setLastRead(@NonNull Date lastRead) {
        this.lastRead = lastRead;
    }

    public float getCurrentReadingPosition() {
        return currentReadingPosition;
    }

    public void setCurrentReadingPosition(float currentReadingPosition) {
        this.currentReadingPosition = currentReadingPosition;
    }
}
