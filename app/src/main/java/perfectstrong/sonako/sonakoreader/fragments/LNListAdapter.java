package perfectstrong.sonako.sonakoreader.fragments;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import perfectstrong.sonako.sonakoreader.database.LightNovel;

public abstract class LNListAdapter<L extends ViewHolder> extends RecyclerView.Adapter<ViewHolder> {
    private List<LightNovel> _lnList = new ArrayList<>();
    protected List<LightNovel> lnList = new ArrayList<>();

    public void setDatalist(List<LightNovel> titles) {
        _lnList.addAll(titles);
        loadByChunk(_lnList);
    }

    private void loadByChunk(List<LightNovel> list) {
        lnList.clear();
        notifyDataSetChanged();
        int currentIndex = 0;
        int chunksize = 25;
        while (currentIndex < list.size()) {
            lnList.addAll(list.subList(
                    currentIndex,
                    Math.min(currentIndex + chunksize, list.size()))
            );
            currentIndex += chunksize;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return lnList.size();
    }

    void filterLNList(String keyword,
                             String type,
                             String status,
                             String[] genres) {
        List<LightNovel> filteredList = new ArrayList<>();
        for (LightNovel ln : _lnList) {
            boolean ok = true;
            // Keyword
            if (!keyword.equals("") && !ln.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                continue;
            // Type
            switch (type) {
                case "Chính thức":
                    if (!ln.getType().equals(LightNovel.ProjectType.OFFICIAL))
                        ok = false;
                    break;
                case "Teaser":
                    if (!ln.getType().equals(LightNovel.ProjectType.TEASER))
                        ok = false;
                    break;
                case "OLN":
                    if (!ln.getType().equals(LightNovel.ProjectType.OLN))
                        ok = false;
                    break;
                default:
                    // Any
                    break;
            }
            if (!ok) continue;
            // Status
            switch (status) {
                case "Active":
                    if (!ln.getStatus().equals(LightNovel.ProjectStatus.ACTIVE))
                        ok = false;
                    break;
                case "Idle":
                    if (!ln.getStatus().equals(LightNovel.ProjectStatus.IDLE))
                        ok = false;
                    break;
                case "Stalled":
                    if (!ln.getStatus().equals(LightNovel.ProjectStatus.STALLED))
                        ok = false;
                    break;
                case "Completed":
                    if (!ln.getStatus().equals(LightNovel.ProjectStatus.COMPLETED))
                        ok = false;
                    break;
                case "Inactive":
                    if (!ln.getStatus().equals(LightNovel.ProjectStatus.INACTIVE))
                        ok = false;
                    break;
                default:
                    // Any
                    break;
            }
            if (!ok) continue;
            // Genres
            if (genres.length > 0) {
                if (!ln.getGenres().containsAll(Arrays.asList(genres))) continue;
            }
            // Acceptance
            filteredList.add(ln);
        }
        // Show
        loadByChunk(filteredList);
    }

    void showAll() {
        loadByChunk(_lnList);
    }
}
