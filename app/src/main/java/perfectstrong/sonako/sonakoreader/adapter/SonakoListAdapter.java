package perfectstrong.sonako.sonakoreader.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import perfectstrong.sonako.sonakoreader.database.Item;
import perfectstrong.sonako.sonakoreader.helper.Utils;

/**
 * Common adapter for with filterable list
 *
 * @param <I>  containing title and tag
 * @param <VH> view holder of item
 */
@SuppressWarnings("WeakerAccess")
public abstract class SonakoListAdapter<I extends Item, VH extends SonakoListAdapter<?, ?>.ItemViewHolder> extends RecyclerView.Adapter<VH> {
    final List<I> _itemsList = new ArrayList<>();
    final List<I> itemsList = new ArrayList<>();
    boolean onFilter = false;

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    public void setDatalist(List<I> pages) {
        _itemsList.clear();
        _itemsList.addAll(pages);
        if (!onFilter)
            show(_itemsList);
        else
            updateItemsList();
    }

    /**
     * Update filtered list
     */
    private void updateItemsList() {
        // Remove removed items
        for (I i : itemsList) {
            if (!_itemsList.contains(i)) {
                itemsList.remove(i);
            }
        }
        notifyDataSetChanged();
        // Update existing items
        for (I i : _itemsList) {
            int idx = itemsList.indexOf(i);
            if (idx > -1) {
                itemsList.set(idx, i);
                notifyItemChanged(idx);
            }
        }
    }

    public void showAll() {
        onFilter = false;
        show(_itemsList);
    }

    void show(List<I> list) {
        loadByChunk(list);
    }

    private void loadByChunk(List<I> list) {
        itemsList.clear();
        notifyDataSetChanged();
        int currentIndex = 0;
        int chunksize = 25;
        if (list.size() > 0)
            while (currentIndex < list.size()) {
                itemsList.addAll(list.subList(
                        currentIndex,
                        Math.min(currentIndex + chunksize, list.size()))
                );
                currentIndex += chunksize;
                notifyDataSetChanged();
            }
    }

    @NonNull
    @Override
    public abstract VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.initAt(position);
    }

    public abstract class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected I item;

        protected ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView.setOnClickListener(this);
        }

        /**
         * @param pos position in {@link #itemsList}
         */
        protected void initAt(int pos) {
            item = itemsList.get(pos);
            decorateView();
        }

        protected abstract void decorateView();

        @Override
        public void onClick(View v) {
            // Open page reader for lightnovel
            Utils.openOrDownload(
                    item.getTitle(),
                    item.getTag(),
                    null,
                    v.getContext()
            );
        }
    }
}
