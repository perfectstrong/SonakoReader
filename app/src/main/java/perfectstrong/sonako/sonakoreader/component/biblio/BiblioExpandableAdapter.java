package perfectstrong.sonako.sonakoreader.component.biblio;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.database.CachePage;
import perfectstrong.sonako.sonakoreader.helper.Utils;

public class BiblioExpandableAdapter extends ExpandableRecyclerViewAdapter<BiblioExpandableAdapter.LNTagViewHolder, BiblioExpandableAdapter.CachePageViewHolder> {

    public BiblioExpandableAdapter(@NonNull List<LNTag> lnTags) {
        super(lnTags);
    }

    public static List<LNTag> regroupCaches(List<CachePage> cachedPages) {
        Map<String, List<CachePage>> tagMap = new HashMap<>();
        for (CachePage cachedPage : cachedPages) {
            String tag = cachedPage.getTag();
            if (tagMap.containsKey(tag))
                //noinspection ConstantConditions
                tagMap.get(tag).add(cachedPage);
            else {
                List<CachePage> list = new ArrayList<>();
                list.add(cachedPage);
                tagMap.put(tag, list);
            }
        }
        List<LNTag> tags = new ArrayList<>();
        for (String tag : tagMap.keySet()) {
            tags.add(new LNTag(tag, tagMap.get(tag)));
        }
        return tags;
    }

    @Override
    public LNTagViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.biblio_ln_tag_basic_view,
                        parent,
                        false);
        return new LNTagViewHolder(view);
    }

    @NonNull
    @Override
    public CachePageViewHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
        View view = LayoutInflater.from(childViewGroup.getContext())
                .inflate(R.layout.biblio_chapter_basic_view,
                        childViewGroup,
                        false);
        return new CachePageViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(CachePageViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        holder.bind(((LNTag) group).getCachePages().get(childIndex));
    }

    @Override
    public void onBindGroupViewHolder(LNTagViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.bind((LNTag) group);
    }

    class LNTagViewHolder extends GroupViewHolder {

        View view;
        private ImageView arrow;

        /**
         * Default constructor.
         *
         * @param itemView The {@link View} being hosted in this ViewHolder
         */
        LNTagViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            arrow = view.findViewById(R.id.biblio_item_show_childs);
        }

        void bind(LNTag tag) {
            ((TextView) view.findViewById(R.id.biblio_item_tag)).setText(tag.getTag());
        }

        @Override
        public void expand() {
            animateExpand();
        }

        @Override
        public void collapse() {
            animateCollapse();
        }

        private void animateExpand() {
            RotateAnimation rotate =
                    new RotateAnimation(
                            360,
                            180,
                            android.view.animation.Animation.RELATIVE_TO_SELF,
                            0.5f,
                            android.view.animation.Animation.RELATIVE_TO_SELF,
                            0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            arrow.setAnimation(rotate);
        }

        private void animateCollapse() {
            RotateAnimation rotate =
                    new RotateAnimation(
                            180,
                            360,
                            android.view.animation.Animation.RELATIVE_TO_SELF,
                            0.5f,
                            android.view.animation.Animation.RELATIVE_TO_SELF,
                            0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            arrow.setAnimation(rotate);
        }
    }

    class CachePageViewHolder extends ChildViewHolder implements View.OnClickListener {

        View view;
        CachePage item;

        /**
         * Default constructor.
         *
         * @param itemView The {@link View} being hosted in this ViewHolder
         */
        CachePageViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            view.setOnClickListener(this);
        }

        void bind(CachePage cachePage) {
            item = cachePage;
            ((TextView) view.findViewById(R.id.biblio_item_page_title)).setText(item.getTitle());
        }

        @Override
        public void onClick(View v) {
            Utils.openOrDownload(
                    item.getTitle(),
                    item.getTag(),
                    null,
                    v.getContext()
            );
        }
    }
}
