package perfectstrong.sonako.sonakoreader.component.biblio;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

import perfectstrong.sonako.sonakoreader.database.CachePage;

public class LNTag extends ExpandableGroup<CachePage> {
    private List<CachePage> cachePages;
    private String tag;

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
}
