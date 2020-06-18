package perfectstrong.sonako.sonakoreader.adapter;

import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.asyncTask.AsyncMassLinkDownloader;
import perfectstrong.sonako.sonakoreader.database.LNDBViewModel;
import perfectstrong.sonako.sonakoreader.database.LightNovel;
import perfectstrong.sonako.sonakoreader.helper.Utils;
import perfectstrong.sonako.sonakoreader.service.PageDownloadService;

/**
 * Generic adapter to show light novel list
 */
public class LNListAdapter extends SonakoListAdapter<LightNovel, LNListAdapter.LNTitleViewHolder> {
    private final LNDBViewModel viewModel;

    public LNListAdapter(LNDBViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void filterLNList(String keyword,
                             String type,
                             String status,
                             String[] genres) {
        onFilter = true;
        List<LightNovel> filteredList = new ArrayList<>();
        for (LightNovel ln : _itemsList) {
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

    @NonNull
    @Override
    public LNTitleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from((viewGroup.getContext()))
                .inflate(R.layout.ln_title_basic_view, viewGroup, false);
        return new LNTitleViewHolder(v);
    }

    class LNTitleViewHolder extends SonakoListAdapter<LightNovel, LNListAdapter.LNTitleViewHolder>.ItemViewHolder implements View.OnCreateContextMenuListener, PopupMenu.OnMenuItemClickListener {

        LNTitleViewHolder(View v) {
            super(v);
            v.setOnCreateContextMenuListener(this);
            v.findViewById(R.id.ln_fav_icon).setOnClickListener(v1 -> {
                if (!item.isFavorite())
                    viewModel.registerFavorite(item);
                else
                    viewModel.unregisterFavorite(item);
            });
        }

        @Override
        void decorateView() {
            // Title
            ((TextView) view.findViewById(R.id.ln_title)).setText(item.getTitle());
            // Type
            switch (item.getType()) {
                case LightNovel.ProjectType.OFFICIAL:
                case LightNovel.ProjectType.TEASER:
                case LightNovel.ProjectType.OLN:
                    ((TextView) view.findViewById(R.id.ln_type)).setText(item.getType());
                    view.findViewById(R.id.ln_type).setVisibility(View.VISIBLE);
                    break;
                default:
                    view.findViewById(R.id.ln_type).setVisibility(View.GONE);
                    break;
            }
            // Genres
            List<String> genres = item.getGenres();
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
            switch (item.getStatus()) {
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
            // Fav icon
            ((ImageView) view.findViewById(R.id.ln_fav_icon)).setImageResource(
                    item.isFavorite() ? android.R.drawable.btn_star_big_on
                            : android.R.drawable.btn_star_big_off
            );
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            PopupMenu popupMenu;
            popupMenu = new PopupMenu(v.getContext(), v, Gravity.END);
            popupMenu.getMenuInflater().inflate(R.menu.ln_title_context_menu, popupMenu.getMenu());
            if (item.isFavorite()) {
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
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.ln_title_context_menu_register_favorite:
                    viewModel.registerFavorite(item);
                    break;
                case R.id.ln_title_context_menu_unregister_favorite:
                    viewModel.unregisterFavorite(item);
                    break;
                case R.id.ln_title_context_menu_refresh_text:
                    Utils.openOrDownload(
                            item.getTitle(),
                            item.getTag(),
                            PageDownloadService.ACTION.REFRESH_TEXT,
                            itemView.getContext()
                    );
                    break;
                case R.id.ln_title_context_menu_refresh_missing_images:
                    Utils.openOrDownload(
                            item.getTitle(),
                            item.getTag(),
                            PageDownloadService.ACTION.REFRESH_MISSING_IMAGES,
                            itemView.getContext()
                    );
                    break;
                case R.id.ln_title_context_menu_download_all_chapters:
                    new AsyncMassLinkDownloader(
                            item.getTitle(),
                            item.getTag(),
                            itemView.getContext()
                    ).execute();
                    break;
            }
            return true;
        }
    }
}
