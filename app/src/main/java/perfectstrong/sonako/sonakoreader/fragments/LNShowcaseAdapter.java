package perfectstrong.sonako.sonakoreader.fragments;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.List;

import perfectstrong.sonako.sonakoreader.PageReadingActivity;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.asyncTask.FavoriteLNsAsyncTask;
import perfectstrong.sonako.sonakoreader.database.LightNovel;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabaseClient;
import perfectstrong.sonako.sonakoreader.helper.Config;

public class LNShowcaseAdapter extends LNListAdapter<LNShowcaseAdapter.LNTitleViewHolder> {
    private Context context;
    LNShowcaseAdapter(Context context) {
        this.context = context;
    }

    class LNTitleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener, PopupMenu.OnMenuItemClickListener {
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
            ((TextView) view.findViewById(R.id.lnTitle)).setText(lightNovel.getTitle());
            // Type
            switch (lightNovel.getType()) {
                case LightNovel.ProjectType.TEASER:
                case LightNovel.ProjectType.OLN:
                    ((TextView) view.findViewById(R.id.lnType)).setText(lightNovel.getType());
                    view.findViewById(R.id.lnType).setVisibility(View.VISIBLE);
                    break;
                default:
                    view.findViewById(R.id.lnType).setVisibility(View.GONE);
                    break;
            }
            // Genres
            List<String> genres = lightNovel.getGenres();
            if (genres.size() > 0) {
                StringBuilder genresStr = new StringBuilder(genres.get(0));
                for (int i = 1; i < genres.size(); i++) {
                    genresStr.append(", ").append(genres.get(i));
                }
                ((TextView) view.findViewById(R.id.lnCategories)).setText("Thể loại: " + genresStr);
            } else {
                view.findViewById(R.id.lnCategories).setVisibility(View.GONE);
            }
            // Status
            switch (lightNovel.getStatus()) {
                case LightNovel.ProjectStatus.ACTIVE:
                    ((TextView) view.findViewById(R.id.lnStatus)).setText("Tình trạng: Liên tục cập nhật");
                    break;
                case LightNovel.ProjectStatus.IDLE:
                    ((TextView) view.findViewById(R.id.lnStatus)).setText("Tình trạng: Không cập nhật trong 3 tháng qua");
                    break;
                case LightNovel.ProjectStatus.STALLED:
                    ((TextView) view.findViewById(R.id.lnStatus)).setText("Tình trạng: Không cập nhật trong 6 tháng qua");
                    break;
                case LightNovel.ProjectStatus.COMPLETED:
                    ((TextView) view.findViewById(R.id.lnStatus)).setText("Tình trạng: Hoàn thành");
                    break;
                case LightNovel.ProjectStatus.INACTIVE:
                default:
                    ((TextView) view.findViewById(R.id.lnStatus)).setText("Tình trạng: Không cập nhật trong 1 năm qua");
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            if (lightNovel == null) return;
            // Open page reader for lightnovel
            Intent intent = new Intent(context, PageReadingActivity.class);
            intent.putExtra(Config.EXTRA_TITLE, lightNovel.getTitle());
            intent.putExtra(Config.EXTRA_TAG, lightNovel.getTag());
            context.startActivity(intent);
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
                    new FavoriteLNsAsyncTask(
                            LightNovelsDatabaseClient.getInstance(context),
                            lightNovel,
                            FavoriteLNsAsyncTask.ACTION.REGISTER
                    ).execute();
                    break;
                case R.id.ln_title_context_menu_unregister_favorite:
                    new FavoriteLNsAsyncTask(
                            LightNovelsDatabaseClient.getInstance(context),
                            lightNovel,
                            FavoriteLNsAsyncTask.ACTION.UNREGISTER
                    ).execute();
                case R.id.ln_title_context_menu_download_main_page:
                    break;
                case R.id.ln_title_context_menu_download_all_links:
                    break;
            }
            return true;
        }
    }

    @NonNull
    @Override
    public LNTitleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from((viewGroup.getContext()))
                .inflate(R.layout.ln_title_basic_view, viewGroup, false);
        return new LNTitleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int pos) {
        if (viewHolder instanceof  LNTitleViewHolder)
            ((LNTitleViewHolder) viewHolder).initAt(pos);
    }
}