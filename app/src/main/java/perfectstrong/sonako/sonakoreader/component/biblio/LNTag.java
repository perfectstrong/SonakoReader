package perfectstrong.sonako.sonakoreader.component.biblio;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;
import java.util.Objects;

import perfectstrong.sonako.sonakoreader.database.CachePage;

public class LNTag extends ExpandableGroup<CachePage> {
    private final List<CachePage> cachePages;
    private final String tag;

    public LNTag(String title, List<CachePage> items) {
        super(title, items);
        tag = title;
        cachePages = items;
    }

    public String getTag() {
        return tag;
    }

    public List<CachePage> getCachePages() {
        return cachePages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LNTag lnTag = (LNTag) o;
        return tag.equals(lnTag.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag);
    }
}
