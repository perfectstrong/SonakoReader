package perfectstrong.sonako.sonakoreader.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.support.annotation.NonNull;

@Entity(
        primaryKeys = {"lnTitle", "genre"},
        foreignKeys = {
                @ForeignKey(
                        entity = LightNovel.class,
                        parentColumns = "title",
                        childColumns = "lnTitle"
                )
        },
        indices = {
                @Index("lnTitle"),
                @Index("genre")
        }
)
public class Categorization {
    @NonNull
    private String lnTitle;
    @NonNull
    private String genre;

    public Categorization(@NonNull String lnTitle, @NonNull String genre) {
        this.lnTitle = lnTitle;
        this.genre = genre;
    }

    @NonNull
    public String getLnTitle() {
        return lnTitle;
    }

    public void setLnTitle(@NonNull String lnTitle) {
        this.lnTitle = lnTitle;
    }

    @NonNull
    public String getGenre() {
        return genre;
    }

    public void setGenre(@NonNull String genre) {
        this.genre = genre;
    }
}
