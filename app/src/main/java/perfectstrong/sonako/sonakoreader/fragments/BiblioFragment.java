package perfectstrong.sonako.sonakoreader.fragments;

import android.annotation.SuppressLint;
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
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.adapter.BiblioAdapter;
import perfectstrong.sonako.sonakoreader.asyncTask.BiblioAsyncTask;
import perfectstrong.sonako.sonakoreader.database.LNDBViewModel;

public class BiblioFragment extends SonakoFragment {

    private BiblioAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LNDBViewModel viewModel = ViewModelProviders.of(this).get(LNDBViewModel.class);

        // Adapter
        adapter = new BiblioAdapter();

        // Observer
        viewModel.getLiveCaches().observe(
                BiblioFragment.this,
                cachedPages -> {
                    adapter.setDatalist(cachedPages);
                    this.updateView(this.getView());
                }
        );
    }

    private void updateView(View view) {
        if (view == null) return;
        if (adapter.getItemCount() == 0) {
            view.findViewById(R.id.NoDatabaseGroup).setVisibility(View.VISIBLE);
            view.findViewById(R.id.NoDatabaseButton)
                    .setOnClickListener(v -> forceRefresh());
            view.findViewById(R.id.RecyclerView).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.NoDatabaseGroup).setVisibility(View.GONE);
            view.findViewById(R.id.RecyclerView).setVisibility(View.VISIBLE);
        }
    }

    private void forceRefresh() {
        new BiblioAsyncTask.ScanSaveDirectory().execute();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_biblio, container, false);
        updateView(rootView);
        RecyclerView recyclerView = rootView.findViewById(R.id.RecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        return rootView;
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
                    String tagKeyword = ((TextView) view.findViewById(R.id.tag_keyword_selection))
                            .getText().toString().trim();
                    @SuppressLint("CutPasteId") int daysLimit = getResources()
                            .getIntArray(R.array.date_limit_values)[
                            ((Spinner) view.findViewById(R.id.date_limit))
                                    .getSelectedItemPosition()
                            ];
                    adapter.filterPages(titleKeyword, tagKeyword, daysLimit);
                }
        );
        alertDialog.setButton(
                Dialog.BUTTON_NEUTRAL,
                getString(R.string.filter_reset),
                (dialog, which) -> adapter.showAll()
        );

        // Show
        alertDialog.show();
    }
}
