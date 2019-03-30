package perfectstrong.sonako.sonakoreader.adapter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.asyncTask.AsyncMassLinkDownloader;
import perfectstrong.sonako.sonakoreader.database.LNDBViewModel;
import perfectstrong.sonako.sonakoreader.database.LightNovel;
import perfectstrong.sonako.sonakoreader.helper.Utils;
import perfectstrong.sonako.sonakoreader.service.PageDownloadService;

/**
 * Generic adapter to show light novel list
 */
public abstract class LNListAdapter extends RecyclerView.Adapter<LNListAdapter.LNTitleViewHolder> {
    private List<LightNovel> _lnList = new ArrayList<>();
    List<LightNovel> lnList = new ArrayList<>();
    protected WeakReference<Context> context;
    private LNDBViewModel viewModel;
    private boolean onFilter = false;

    LNListAdapter(Context context, LNDBViewModel viewModel) {
        this.context = new WeakReference<>(context);
        this.viewModel = viewModel;
    }

    public void setDatalist(List<LightNovel> titles) {
        _lnList.clear();
        _lnList.addAll(titles);
        if (!onFilter)
            loadByChunk(_lnList);
    }

    private void loadByChunk(List<LightNovel> list) {
        lnList.clear();
        notifyDataSetChanged();
        int currentIndex = 0;
        int chunksize = 25;
        if (list.size() > 0)
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

    public void filterLNList(String keyword,
                             String type,
                             String status,
                             String[] genres) {
        onFilter = true;
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
        show(filteredList);
    }

    private void show(List<LightNovel> filteredList) {
        loadByChunk(filteredList);
    }

    public void showAll() {
        onFilter = false;
        show(_lnList);
    }

    @NonNull
    @Override
    public LNTitleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from((viewGroup.getContext()))
                .inflate(R.layout.ln_title_basic_view, viewGroup, false);
        return new LNTitleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LNTitleViewHolder viewHolder, int i) {
        viewHolder.initAt(i);
    }

    class LNTitleViewHolder extends ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener, PopupMenu.OnMenuItemClickListener {
        View view;
        private LightNovel lightNovel;

        LNTitleViewHolder(View v) {
            super(v);
            view = v;
            v.setOnClickListener(this);
            v.setOnCreateContextMenuListener(this);
        }

        void initAt(int position) {
            lightNovel = lnList.get(position);
            // Title
            ((TextView) view.findViewById(R.id.ln_title)).setText(lightNovel.getTitle());
            // Type
            switch (lightNovel.getType()) {
                case LightNovel.ProjectType.OFFICIAL:
                case LightNovel.ProjectType.TEASER:
                case LightNovel.ProjectType.OLN:
                    ((TextView) view.findViewById(R.id.ln_type)).setText(lightNovel.getType());
                    view.findViewById(R.id.ln_type).setVisibility(View.VISIBLE);
                    break;
                default:
                    view.findViewById(R.id.ln_type).setVisibility(View.GONE);
                    break;
            }
            // Genres
            List<String> genres = lightNovel.getGenres();
            if (genres != null && genres.size() > 0) {
                StringBuilder genresStr = new StringBuilder(genres.get(0));
                for (int i = 1; i < genres.size(); i++) {
                    genresStr.append(", ").append(genres.get(i));
                }
                String _genres = genresStr.toString();
                ((TextView) view.findViewById(R.id.ln_categories)).setText(_genres);
                view.findViewById(R.id.ln_categories).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.ln_categories).setVisibility(View.GONE);
            }
            // Status
            switch (lightNovel.getStatus()) {
                case LightNovel.ProjectStatus.ACTIVE:
                    ((TextView) view.findViewById(R.id.ln_status)).setText(R.string.ln_status_active);
                    break;
                case LightNovel.ProjectStatus.IDLE:
                    ((TextView) view.findViewById(R.id.ln_status)).setText(R.string.ln_status_idle);
                    break;
                case LightNovel.ProjectStatus.STALLED:
                    ((TextView) view.findViewById(R.id.ln_status)).setText(R.string.ln_status_stalled);
                    break;
                case LightNovel.ProjectStatus.COMPLETED:
                    ((TextView) view.findViewById(R.id.ln_status)).setText(R.string.ln_status_completed);
                    break;
                case LightNovel.ProjectStatus.INACTIVE:
                default:
                    ((TextView) view.findViewById(R.id.ln_status)).setText(R.string.ln_status_inactive);
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            if (lightNovel == null) return;
            // Open page reader for lightnovel
            Utils.openOrDownload(
                    context.get(),
                    lightNovel.getTitle(),
                    lightNovel.getTag(),
                    null
            );
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            PopupMenu popupMenu;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                popupMenu = new PopupMenu(v.getContext(), v, Gravity.END);
            } else {
                popupMenu = new PopupMenu(v.getContext(), v);
            }
            popupMenu.getMenuInflater().inflate(R.menu.ln_title_context_menu, popupMenu.getMenu());
            if (lightNovel.isFavorite()) {
                popupMenu.getMenu()
                        .findItem(R.id.ln_title_context_menu_register_favorite)
                        .setVisible(false);
            } else {
                popupMenu.getMenu()
                        .findItem(R.id.ln_title_context_menu_unregister_favorite)
                        .setVisible(false);
            }
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.ln_title_context_menu_register_favorite:
                    viewModel.registerFavorite(lightNovel);
                    break;
                case R.id.ln_title_context_menu_unregister_favorite:
                    viewModel.unregisterFavorite(lightNovel);
                    break;
                case R.id.ln_title_context_menu_refresh_text:
                    Utils.openOrDownload(
                            context.get(),
                            lightNovel.getTitle(),
                            lightNovel.getTag(),
                            PageDownloadService.ACTION.REFRESH_TEXT
                    );
                    break;
                case R.id.ln_title_context_menu_refresh_missing_images:
                    Utils.openOrDownload(
                            context.get(),
                            lightNovel.getTitle(),
                            lightNovel.getTag(),
                            PageDownloadService.ACTION.REFRESH_MISSING_IMAGES
                    );
                case R.id.ln_title_context_menu_download_all_chapters:
                    new AsyncMassLinkDownloader(
                            context.get(),
                            lightNovel.getTitle(),
                            lightNovel.getTag()
                    ).execute();
            }
            return true;
        }
    }
}
