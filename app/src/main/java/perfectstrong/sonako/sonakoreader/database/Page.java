package perfectstrong.sonako.sonakoreader.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import java.util.Date;

@Entity()
@TypeConverters(TimestampConverter.class)
public class Page {
    @PrimaryKey
    @NonNull
    private String title;
    @NonNull
    private String tag;
    @NonNull
    private Date lastRead;

    public Page(@NonNull String title, @NonNull String tag, @NonNull Date lastRead) {
        this.title = title;
        this.tag = tag;
        this.lastRead = lastRead;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getTag() {
        return tag;
    }

    public void setTag(@NonNull String tag) {
        this.tag = tag;
    }

    @NonNull
    public Date getLastRead() {
        return lastRead;
    }

    public void setLastRead(@NonNull Date lastRead) {
        this.lastRead = lastRead;
    }
}
