package perfectstrong.sonako.sonakoreader.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.database.CachePage;
import perfectstrong.sonako.sonakoreader.helper.Utils;

public class BiblioAdapter extends SonakoListAdapter<CachePage, BiblioAdapter.CachePageViewHolder> {

    public void filterPages(String titleKeyword,
                            String tagKeyword,
                            int daysLimit) {
        onFilter = true;
        if (daysLimit < 0) daysLimit = Integer.MAX_VALUE;
        List<CachePage> filteredPages = new ArrayList<>();
        Date date = new Date(); // now
        long msNow = date.getTime();
        for (CachePage page : _itemsList) {
            if (!page.getTitle().toLowerCase().contains(titleKeyword.toLowerCase())) continue;
            if (!page.getTitle().toLowerCase().contains(tagKeyword.toLowerCase())) continue;
            if (TimeUnit.MILLISECONDS.toDays(msNow - page.getLastCached().getTime())
                    > daysLimit)
                continue;
            filteredPages.add(page);
        }
        show(filteredPages);
    }

    @NonNull
    @Override
    public CachePageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cache_basic_view,
                        parent,
                        false);
        return new CachePageViewHolder(v);
    }

    class CachePageViewHolder extends SonakoListAdapter<CachePage, BiblioAdapter.CachePageViewHolder>.ItemViewHolder {

        CachePageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void initAt(int position) {
            setItem(position);
            // Title
            ((TextView) view.findViewById(R.id.biblio_item_page_title)).setText(item.getTitle());
            // Tag
            ((TextView) view.findViewById(R.id.biblio_item_page_tag)).setText(item.getTag());
            // Last cached
            ((TextView) view.findViewById(R.id.biblio_item_page_last_cached)).setText(Utils.FORMATTER.format(item.getLastCached()));
        }
    }
}
