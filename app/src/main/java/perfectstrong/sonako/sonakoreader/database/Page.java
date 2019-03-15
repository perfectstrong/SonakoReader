package perfectstrong.sonako.sonakoreader.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.annotation.NonNull;

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
