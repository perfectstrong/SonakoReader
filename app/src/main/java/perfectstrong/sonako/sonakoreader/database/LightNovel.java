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
    @Ignore
    private List<String> genres;

    public LightNovel(@NonNull String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LightNovel that = (LightNovel) o;
        return title.equals(that.getTitle());
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
        public static final String[] CHOICES = {"Bất kỳ", "Chính thức", "Teaser", "OLN"};
        public static final String OFFICIAL = "Official";
        public static final String TEASER = "Teaser";
        public static final String OLN = "Original Light Novel";
        public static final List<String> ALL = Arrays.asList(OFFICIAL, TEASER, OLN);
    }

    public static class ProjectStatus {
        public static final String[] CHOICES = {"Bất kỳ", "Active", "Idle", "Stalled", "Completed", "Inactive"};
        public static final String ACTIVE = "Active Projects";
        public static final String IDLE = "Idle Projects";
        public static final String STALLED = "Stalled Projects";
        public static final String INACTIVE = "Inactive Projects";
        public static final String COMPLETED = "Hoàn thành";
        public static final List<String> ALL = Arrays.asList(ACTIVE, IDLE, STALLED, INACTIVE, COMPLETED);
    }

    @SuppressWarnings("unused")
    public static class ProjectGenre {
        public static final List<String> ALL = Arrays.asList("Action", "Adult", "Adventure", "Comedy", "Drama", "Ecchi", "Fantasy", "Game", "Gender Bender", "Harem", "Historical", "Horror", "Martial Arts", "Mature", "Mecha", "Mystery", "Psychological", "Romance", "School Life", "Sci-fi", "Seinen", "Shotocon", "Shoujo", "Shounen", "Slice of life", "Supernatural", "Tragedy", "Yuri", "Magic", "Chinese", "Lolicon", "Sports", "Military", "Post-Apocalypse", "Reincarnation", "Transported", "Warfare", "Demons", "Josei", "Funny");
    }

    public static class ExceptionTag {
        public static final List<String> ALL = Arrays.asList("MF Bunko J", "Event Xuân 2015", "Licensed", "Host Project", "Event 4K");
    }
}
