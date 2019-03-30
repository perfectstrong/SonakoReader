package perfectstrong.sonako.sonakoreader.database;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import perfectstrong.sonako.sonakoreader.helper.Utils;

@SuppressWarnings("unused")
@Entity
@TypeConverters(TimestampConverter.class)
public class CachePage {
    @NonNull
    @PrimaryKey
    private String title;
    @NonNull
    private String tag;
    @NonNull
    private Date lastCached;

    public CachePage(@NonNull String title, @NonNull String tag, @NonNull Date lastCached) {
        this.title = Utils.sanitize(Utils.decode(title));
        this.tag = Utils.sanitize(tag);
        this.lastCached = lastCached;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getTag() {
        return tag;
    }

    public void setTag(@NonNull String tag) {
        this.tag = tag;
    }

    @NonNull
    public Date getLastCached() {
        return lastCached;
    }

    public void setLastCached(@NonNull Date lastCached) {
        this.lastCached = lastCached;
    }
}
