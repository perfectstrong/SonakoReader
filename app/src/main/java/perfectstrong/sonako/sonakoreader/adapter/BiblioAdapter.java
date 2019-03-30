package perfectstrong.sonako.sonakoreader.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.database.CachePage;
import perfectstrong.sonako.sonakoreader.helper.Utils;

public class BiblioAdapter extends RecyclerView.Adapter<BiblioAdapter.CachePageViewHolder> {
    private List<CachePage> _clist = new ArrayList<>();
    private List<CachePage> clist = new ArrayList<>();
    private boolean onFilter = false;

    public BiblioAdapter() {
    }

    public void setDataList(List<CachePage> cachePages) {
        _clist.clear();
        _clist.addAll(cachePages);
        if (!onFilter)
            show(_clist);
    }

    private void show(List<CachePage> list) {
        loadByChunk(list);
    }

    private void loadByChunk(List<CachePage> newList) {
        clist.clear();
        notifyDataSetChanged();
        int currentIndex = 0;
        int chunksize = 25;
        if (newList.size() > 0)
            while (currentIndex < newList.size()) {
                clist.addAll(newList.subList(
                        currentIndex,
                        Math.min(currentIndex + chunksize, newList.size()))
                );
                currentIndex += chunksize;
                notifyDataSetChanged();
            }
    }

    public void filterPages(String keyword, int dateLimit) {
        // TODO filter pages
    }

    public void showAll() {
        onFilter = false;
        show(_clist);
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

    @Override
    public void onBindViewHolder(@NonNull CachePageViewHolder holder, int position) {
        holder.initAt(position);
    }

    @Override
    public int getItemCount() {
        return clist.size();
    }

    class CachePageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final View view;
        private CachePage cache;

        CachePageViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            itemView.setOnClickListener(this);
        }

        void initAt(int position) {
            cache = clist.get(position);
            // Title
            ((TextView) view.findViewById(R.id.biblio_item_page_title)).setText(cache.getTitle());
            // Tag
            ((TextView) view.findViewById(R.id.biblio_item_page_tag)).setText(cache.getTag());
            // Last cached
            ((TextView) view.findViewById(R.id.biblio_item_page_last_cached)).setText(Utils.FORMATTER.format(cache.getLastCached()));
        }

        @Override
        public void onClick(View v) {
            if (cache == null) return;
            // Open page reader
            Utils.openOrDownload(
                    cache.getTitle(),
                    cache.getTag(),
                    null,
                    v.getContext()
            );
        }
    }
}
