package perfectstrong.sonako.sonakoreader.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

@Entity()
public class LightNovel {
    @PrimaryKey()
    @NonNull
    private String title;
    private String tag;
    private String type = ProjectType.OFFICIAL;
    private String status = ProjectStatus.ACTIVE;
    private String mainpageText = "";
    @Ignore
    private List<String> genres;

    public LightNovel(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMainpageText() {
        return mainpageText;
    }

    public void setMainpageText(String mainpageText) {
        this.mainpageText = mainpageText;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public static class ProjectType {
        public static final String OFFICIAL = "Official";
        public static final String TEASER = "Teaser";
        public static final String OLN = "Original Light Novel";
    }

    public static class ProjectStatus {
        public static final String ACTIVE = "Active Projects";
        public static final String IDLE = "Idle Projects";
        public static final String STALLED = "Stalled Projects";
        public static final String INACTIVE = "Inactive Projects";
        public static final String COMPLETED = "Hoàn thành";
    }

    public static class ProjectGenre {
        public static final List<String> ALL = Arrays.asList("Action", "Adult", "Adventure", "Comedy", "Drama", "Ecchi", "Fantasy", "Game", "Gender Bender", "Harem", "Historical", "Horror", "Martial Arts", "Mature", "Mecha", "Mystery", "Psychological", "Romance", "School Life", "Sci-fi", "Seinen", "Shotocon", "Shoujo", "Shounen", "Slice of life", "Supernatural", "Tragedy");
        public static final String ACTION = "Action";
        public static final String ADULT = "Adult";
        public static final String ADVENTURE = "Adventure";
        public static final String COMEDY = "Comedy";
        public static final String DRAMA = "Drama";
        public static final String ECCHI = "Ecchi";
        public static final String FANTASY = "Fantasy";
        public static final String GAME = "Game";
        public static final String GENDER_BENDER = "Gender Bender";
        public static final String HAREM = "Harem";
        public static final String HISTORICAL = "Historical";
        public static final String HORROR = "Horror";
        public static final String MARTIAL_ARTS = "Martial Arts";
        public static final String MATURE = "Mature";
        public static final String MECHA = "Mecha";
        public static final String MYSTERY = "Mystery";
        public static final String PSYCHOLOGICAL = "Psychological";
        public static final String ROMANCE = "Romance";
        public static final String SCHOOL_LIFE = "School Life";
        public static final String SCI_FI = "Sci-fi";
        public static final String SEINEN = "Seinen";
        public static final String SHOTOCON = "Shotocon";
        public static final String SHOUJO = "Shoujo";
        public static final String SHOUNEN = "Shounen";
        public static final String SLICE_OF_LIFE = "Slice of life";
        public static final String SUPERNATURAL = "Supernatural";
        public static final String TRAGEDY = "Tragedy";

    }
}
