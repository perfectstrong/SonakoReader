package perfectstrong.sonako.sonakoreader.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.adapter.BiblioAdapter;
import perfectstrong.sonako.sonakoreader.asyncTask.BiblioAsyncTask;
import perfectstrong.sonako.sonakoreader.database.LNDBViewModel;

public class BiblioFragment extends Fragment implements PageFilterable {

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
                    adapter.setDataList(cachedPages);
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
            view.findViewById(R.id.BiblioRecyclerView).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.NoDatabaseGroup).setVisibility(View.GONE);
            view.findViewById(R.id.BiblioRecyclerView).setVisibility(View.VISIBLE);
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
        RecyclerView recyclerView = rootView.findViewById(R.id.BiblioRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        return rootView;
    }

    @Override
    public void filterPages(String keyword, int dateLimit) {
        adapter.filterPages(keyword, dateLimit);
    }

    @Override
    public void showAll() {
        adapter.showAll();
    }
}
