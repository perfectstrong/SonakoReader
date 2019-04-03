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
import perfectstrong.sonako.sonakoreader.database.Page;
import perfectstrong.sonako.sonakoreader.helper.Utils;

public class HistoryAdapter extends SonakoListAdapter<Page, HistoryAdapter.HistoryEntryViewHolder> {

    @NonNull
    @Override
    public HistoryEntryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.page_basic_view, viewGroup, false);
        return new HistoryEntryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryEntryViewHolder historyEntryViewHolder, int i) {
        historyEntryViewHolder.initAt(i);
    }

    public void filterPages(String keyword, int daysLimit) {
        onFilter = true;
        if (daysLimit < 0) daysLimit = Integer.MAX_VALUE;
        List<Page> filteredPages = new ArrayList<>();
        Date date = new Date(); // now
        long msNow = date.getTime();
        for (Page page : _itemsList) {
            if (!page.getTitle().contains(keyword)) continue;
            if (TimeUnit.MILLISECONDS.toDays(msNow - page.getLastRead().getTime())
                    > daysLimit)
                continue;
            filteredPages.add(page);
        }
        show(filteredPages);
    }

    class HistoryEntryViewHolder extends SonakoListAdapter.ItemViewHolder {

        HistoryEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            view.setOnClickListener(this);
        }

        @Override
        void initAt(int pos) {
            setItem(pos);
            // Title
            ((TextView) view.findViewById(R.id.page_title)).setText(item.getTitle());
            // Last read
            ((TextView) view.findViewById(R.id.page_last_read))
                    .setText(Utils.FORMATTER.format(((Page) item).getLastRead()));
        }
    }
}
