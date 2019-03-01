package perfectstrong.sonako.sonakoreader.fragments;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.database.LightNovel;

public class LNTitlesAdapter extends RecyclerView.Adapter<LNTitlesAdapter.LNTitleViewHolder> {
    private List<LightNovel> lnList = new ArrayList<>();

    LNTitlesAdapter() {
    }

    void setDatalist(List<LightNovel> titles) {
        lnList.addAll(titles);
        notifyDataSetChanged();
    }

    class LNTitleViewHolder extends RecyclerView.ViewHolder {
        View view;

        LNTitleViewHolder(View v) {
            super(v);
            view = v;
        }

        void initAt(int position) {
            LightNovel ln = lnList.get(position);
            // Title
            ((TextView) view.findViewById(R.id.lnTitle)).setText(ln.getTitle());
            // Genres
            List<String> genres = ln.getGenres();
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
            switch (ln.getStatus()) {
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
            // Type
            switch (ln.getType()) {
                case LightNovel.ProjectType.TEASER:
                case LightNovel.ProjectType.OLN:
                    ((TextView) view.findViewById(R.id.lnType)).setText(ln.getType());
                    break;
                default:
                    view.findViewById(R.id.lnType).setVisibility(View.GONE);
            }
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
    public void onBindViewHolder(@NonNull LNTitleViewHolder lnTitleViewHolder, int position) {
        lnTitleViewHolder.initAt(position);
    }

    @Override
    public int getItemCount() {
        return lnList.size();
    }
}
