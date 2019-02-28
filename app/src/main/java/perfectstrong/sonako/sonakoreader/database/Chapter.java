package perfectstrong.sonako.sonakoreader.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(
        foreignKeys = @ForeignKey(
                entity = LightNovel.class,
                parentColumns = "title",
                childColumns = "lnTitle"
        ),
        indices = {@Index("lnTitle")}
)
public class Chapter {
    /**
     * Title of wiki page
     */
    @PrimaryKey
    @NonNull
    private String id;
    /**
     * Empty indicates unidentifiable
     */
    private String volume = "";
    @NonNull
    private String lnTitle;
    private String shortname = "";
    private boolean isFulltext = false;
    private String content = "";

    public Chapter(String id, String lnTitle) {
        this.id = id;
        this.lnTitle = lnTitle;
    }

    public String getId() {
        return id;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getLnTitle() {
        return lnTitle;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public boolean isFulltext() {
        return isFulltext;
    }

    public void setFulltext(boolean fulltext) {
        isFulltext = fulltext;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
