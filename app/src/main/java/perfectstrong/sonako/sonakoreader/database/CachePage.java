package perfectstrong.sonako.sonakoreader.database;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.TypeConverters;

import java.util.Date;
import java.util.Objects;

import perfectstrong.sonako.sonakoreader.helper.Utils;

@SuppressWarnings("unused")
@Entity(
        primaryKeys = {"title"}
)
@TypeConverters(TimestampConverter.class)
public class CachePage extends Item implements Parcelable {
    @NonNull
    private Date lastCached;

    public CachePage(@NonNull String title, @NonNull String tag, @NonNull Date lastCached) {
        this.title = Utils.sanitize(Utils.decode(title));
        this.tag = Utils.sanitize(tag);
        this.lastCached = lastCached;
    }

    protected CachePage(Parcel in) {
        title = Objects.requireNonNull(in.readString());
        tag = in.readString();
        lastCached = new Date(in.readLong());
    }

    public static final Creator<CachePage> CREATOR = new Creator<CachePage>() {
        @Override
        public CachePage createFromParcel(Parcel in) {
            return new CachePage(in);
        }

        @Override
        public CachePage[] newArray(int size) {
            return new CachePage[size];
        }
    };

    @NonNull
    public Date getLastCached() {
        return lastCached;
    }

    public void setLastCached(@NonNull Date lastCached) {
        this.lastCached = lastCached;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(tag);
        dest.writeLong(lastCached.getTime());
    }
}
