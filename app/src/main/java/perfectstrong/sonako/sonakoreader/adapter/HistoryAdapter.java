package perfectstrong.sonako.sonakoreader.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import perfectstrong.sonako.sonakoreader.PageReadingActivity;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.database.Page;
import perfectstrong.sonako.sonakoreader.helper.Config;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryEntryViewHolder> {

    private List<Page> _plist = new ArrayList<>();
    private List<Page> plist = new ArrayList<>();
    private final Context context;
    private final SimpleDateFormat format = new SimpleDateFormat("HH:mm EEEE dd/MM/yy", new Locale("vi"));
    private boolean onFilter = false;

    public HistoryAdapter(Context context) {
        this.context = context;
    }

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

    @Override
    public int getItemCount() {
        return plist.size();
    }

    public void setDatalist(List<Page> pages) {
        _plist.clear();
        _plist.addAll(pages);
        if (!onFilter)
            loadByChunk(_plist);
    }

    private void loadByChunk(List<Page> list) {
        plist.clear();
        notifyDataSetChanged();
        int currentIndex = 0;
        int chunksize = 25;
        if (list.size() > 0)
            while (currentIndex < list.size()) {
                plist.addAll(list.subList(
                        currentIndex,
                        Math.min(currentIndex + chunksize, list.size()))
                );
                currentIndex += chunksize;
                notifyDataSetChanged();
            }
    }

    public void filterPages(String keyword, int daysLimit) {
        onFilter = true;
        if (daysLimit < 0) daysLimit = Integer.MAX_VALUE;
        List<Page> filteredPages = new ArrayList<>();
        Date date = new Date(); // now
        long msNow = date.getTime();
        for (Page page : _plist) {
            if (!page.getTitle().contains(keyword)) continue;
            if (TimeUnit.MILLISECONDS.toDays(msNow - page.getLastRead().getTime())
                    > daysLimit)
                continue;
            filteredPages.add(page);
        }
        show(filteredPages);
    }

    public void showAll() {
        onFilter = false;
        show(_plist);
    }

    private void show(List<Page> list) {
        loadByChunk(list);
    }

    class HistoryEntryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View view;
        private Page page;

        HistoryEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            view.setOnClickListener(this);
        }

        void initAt(int pos) {
            page = plist.get(pos);
            // Title
            ((TextView) view.findViewById(R.id.page_title)).setText(page.getTitle());
            // Last read
            ((TextView) view.findViewById(R.id.page_last_read))
                    .setText(context.getString(R.string.last_read) +
                            format.format(page.getLastRead())
                    );
        }

        @Override
        public void onClick(View v) {
            if (page == null) return;
            // Open page reader
            Intent intent = new Intent(context, PageReadingActivity.class);
            intent.putExtra(Config.EXTRA_TITLE, page.getTitle());
            intent.putExtra(Config.EXTRA_TAG, page.getTag());
            context.startActivity(intent);
        }
    }
}
