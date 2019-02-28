package perfectstrong.sonako.sonakoreader.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.support.annotation.NonNull;

@Entity(
        primaryKeys = {"lnTitle", "id"},
        foreignKeys = @ForeignKey(
                entity = LightNovel.class,
                parentColumns = "title",
                childColumns = "lnTitle"
        )
)
public class Volume {
    @NonNull
    private String lnTitle;
    @NonNull
    private String id;
    private String title = "";

    public Volume(@NonNull String lnTitle, @NonNull String id) {
        this.lnTitle = lnTitle;
        this.id = id;
    }

    @NonNull
    public String getLnTitle() {
        return lnTitle;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
