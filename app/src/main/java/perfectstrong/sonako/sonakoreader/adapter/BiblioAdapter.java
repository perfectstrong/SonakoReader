package perfectstrong.sonako.sonakoreader.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.database.CachePage;
import perfectstrong.sonako.sonakoreader.helper.Utils;

public class BiblioAdapter extends SonakoListAdapter<CachePage, BiblioAdapter.CachePageViewHolder> {

    public void filterPages(String keyword, int dateLimit) {
        // TODO filter pages
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
