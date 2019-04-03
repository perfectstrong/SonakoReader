package perfectstrong.sonako.sonakoreader.database;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.TypeConverters;
import perfectstrong.sonako.sonakoreader.helper.Utils;

@SuppressWarnings("unused")
@Entity(
        primaryKeys = {"title"}
)
@TypeConverters(TimestampConverter.class)
public class CachePage extends Item {
    @NonNull
    private Date lastCached;

    public CachePage(@NonNull String title, @NonNull String tag, @NonNull Date lastCached) {
        this.title = Utils.sanitize(Utils.decode(title));
        this.tag = Utils.sanitize(tag);
        this.lastCached = lastCached;
    }

    @NonNull
    public Date getLastCached() {
        return lastCached;
    }

    public void setLastCached(@NonNull Date lastCached) {
        this.lastCached = lastCached;
    }
}
