package perfectstrong.sonako.sonakoreader.database;

import android.support.annotation.NonNull;

public class Page {
    @NonNull
    private String title;
    private String tag;

    public Page(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
