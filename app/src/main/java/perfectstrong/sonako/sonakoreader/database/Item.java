package perfectstrong.sonako.sonakoreader.database;

import androidx.annotation.NonNull;

public abstract class Item {
    @NonNull
    String title = "";
    String tag;

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
}
