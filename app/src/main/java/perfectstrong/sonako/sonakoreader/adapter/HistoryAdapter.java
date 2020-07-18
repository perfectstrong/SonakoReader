package perfectstrong.sonako.sonakoreader.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.database.Page;

public class HistoryAdapter extends SonakoListAdapter<Page, HistoryAdapter.HistoryEntryViewHolder> {

    @NonNull
    @Override
    public HistoryEntryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.page_basic_view, viewGroup, false);
        return new HistoryEntryViewHolder(v);
    }

    public void filterPages(String keyword, int daysLimit) {
        onFilter = true;
        if (daysLimit < 0) daysLimit = Integer.MAX_VALUE;
        List<Page> filteredPages = new ArrayList<>();
        Date date = new Date(); // now
        long msNow = date.getTime();
        for (Page page : _itemsList) {
            if (!page.getTitle().toLowerCase().contains(keyword.toLowerCase())) continue;
            if (TimeUnit.MILLISECONDS.toDays(msNow - page.getLastRead().getTime())
                    > daysLimit)
                continue;
            filteredPages.add(page);
        }
        show(filteredPages);
    }

    class HistoryEntryViewHolder extends SonakoListAdapter<Page, HistoryAdapter.HistoryEntryViewHolder>.ItemViewHolder {

        protected HistoryEntryViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        protected void decorateView() {
            // Title
            ((TextView) itemView.findViewById(R.id.page_title)).setText(item.getTitle());
            // Last read
            ((TextView) itemView.findViewById(R.id.page_last_read))
                    .setText(new SimpleDateFormat("HH:mm EEEE dd/MM/yy", new Locale("vi")).format(item.getLastRead()));
        }
    }
}
