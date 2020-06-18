package perfectstrong.sonako.sonakoreader.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.asyncTask.BiblioAsyncTask;
import perfectstrong.sonako.sonakoreader.component.biblio.BiblioExpandableAdapter;
import perfectstrong.sonako.sonakoreader.component.biblio.LNTag;
import perfectstrong.sonako.sonakoreader.database.CachePage;
import perfectstrong.sonako.sonakoreader.database.LNDBViewModel;

public class BiblioFragment extends SonakoFragment {

    private BiblioExpandableAdapter adapter;
    private List<LNTag> itemsList;
    private boolean onFilter = false;

    @Override
    public BiblioExpandableAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LNDBViewModel viewModel = new ViewModelProvider(this).get(LNDBViewModel.class);

        // Observer
        viewModel.getLiveCaches().observe(
                BiblioFragment.this,
                cachedPages -> {
                    this.updateBackData(cachedPages);
                    this.updateView(this.getView());
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if (v != null
                && v.findViewById(R.id.lnfilterable_show_all) != null) {
            final View showAllBtn = v.findViewById(R.id.lnfilterable_show_all);
            showAllBtn.setOnClickListener(v1 -> {
                showAll();
                hideShowAllBtn();
            });
        }
        return v;
    }


    private void updateBackData(List<CachePage> cachedPages) {
        itemsList = BiblioExpandableAdapter.regroupCaches(cachedPages);
        if (!onFilter) {
            show(itemsList);
        }
    }

    @Override
    protected void updateView(View view) {
        if (view == null) return;
        if (adapter == null || adapter.getItemCount() == 0) {
            view.findViewById(R.id.NoDatabaseGroup).setVisibility(View.VISIBLE);
            view.findViewById(R.id.NoDatabaseButton)
                    .setOnClickListener(v -> forceRefresh());
            if (recyclerView != null)
                recyclerView.setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.NoDatabaseGroup).setVisibility(View.GONE);
            view.findViewById(R.id.RecyclerView).setVisibility(View.VISIBLE);
            if (!onFilter && recyclerView != null)
                recyclerView.setAdapter(adapter);
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.lndatabase;
    }

    private void forceRefresh() {
        new BiblioAsyncTask.ScanSaveDirectory().execute();
    }

    @Override
    public void showFilterDialog() {
        FragmentActivity activity = this.getActivity();
        if (activity == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        AlertDialog alertDialog = builder.setTitle(R.string.filter).create();
        View view = View.inflate(activity, R.layout.cache_filter_dialog, null);
        alertDialog.setView(view);

        // Set default
        Spinner sp = view.findViewById(R.id.date_limit);
        if (sp != null) {
            sp.setSelection(3);
        }

        // Action
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setButton(
                Dialog.BUTTON_NEGATIVE,
                getString(R.string.no),
                (Message) null
        );
        alertDialog.setButton(
                Dialog.BUTTON_POSITIVE,
                getString(R.string.ok),
                (dialog, which) -> {
                    // Filter
                    String titleKeyword = ((TextView) view.findViewById(R.id.title_keyword_selection))
                            .getText().toString().trim();
                    assert sp != null;
                    int daysLimit = getResources()
                            .getIntArray(R.array.date_limit_values)[sp.getSelectedItemPosition()];
                    filterPages(titleKeyword, daysLimit);
                    showShowAllBtn();
                }
        );
        alertDialog.setButton(
                Dialog.BUTTON_NEUTRAL,
                getString(R.string.filter_reset),
                (dialog, which) -> {
                    hideShowAllBtn();
                    showAll();
                }
        );

        // Show
        alertDialog.show();
    }

    private void showAll() {
        onFilter = false;
        show(itemsList);
    }

    private void filterPages(String titleKeyword,
                             int daysLimit) {
        onFilter = true;
        if (daysLimit < 0) daysLimit = Integer.MAX_VALUE;
        Date date = new Date(); // now
        long msNow = date.getTime();
        List<LNTag> filteredTag = new ArrayList<>();
        for (LNTag lnTag : itemsList) {
            String tag = lnTag.getTag();
            List<CachePage> filteredCached = new ArrayList<>();
            for (CachePage cachePage : lnTag.getCachePages()) {
                if (!cachePage.getTitle().toLowerCase().contains(titleKeyword.toLowerCase()))
                    continue;
                if (TimeUnit.MILLISECONDS.toDays(msNow - cachePage.getLastCached().getTime())
                        > daysLimit) {
                    continue;
                }
                filteredCached.add(cachePage);
            }
            if (filteredCached.size() > 0)
                filteredTag.add(new LNTag(tag, filteredCached));
        }
        show(filteredTag);
    }

    private void show(List<LNTag> filteredTags) {
        if (adapter != null) {
            adapter = new BiblioExpandableAdapter(filteredTags, adapter.getToggleState());
        } else
            adapter = new BiblioExpandableAdapter(filteredTags);
        if (recyclerView != null) {
            LinearLayoutManager l = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (l != null) {
                int oldVisibleItemPosition = l.findFirstVisibleItemPosition();
                recyclerView.setAdapter(adapter);
                l.scrollToPosition(oldVisibleItemPosition);
            } else
                recyclerView.setAdapter(adapter);
        }
    }
}