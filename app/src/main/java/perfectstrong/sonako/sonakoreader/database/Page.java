package perfectstrong.sonako.sonakoreader.database;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.TypeConverters;

@Entity(
        primaryKeys = {"title", "tag"}
)
@TypeConverters(TimestampConverter.class)
public class Page extends Item {
    @NonNull
    private Date lastRead;

    public Page(@NonNull String title, @NonNull String tag, @NonNull Date lastRead) {
        this.title = title;
        this.tag = tag;
        this.lastRead = lastRead;
    }

    @NonNull
    public Date getLastRead() {
        return lastRead;
    }

    public void setLastRead(@NonNull Date lastRead) {
        this.lastRead = lastRead;
    }
}
