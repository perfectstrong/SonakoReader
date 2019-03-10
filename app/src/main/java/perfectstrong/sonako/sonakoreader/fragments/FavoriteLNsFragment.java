package perfectstrong.sonako.sonakoreader.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabaseClient;
import perfectstrong.sonako.sonakoreader.database.LNDBViewModel;


/**
 * A simple {@link Fragment} subclass.
 */
public class FavoriteLNsFragment extends Fragment implements LNFilterable {

    private FavoriteLNsAdapter mAdapter;

    public FavoriteLNsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_favorites_ln, container, false);
        RecyclerView recyclerView = rootview.findViewById(R.id.FavoriteLNsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // View model
        LNDBViewModel viewModel = ViewModelProviders.of(this)
                .get(LNDBViewModel.class);
        viewModel.setLndb(LightNovelsDatabaseClient.getInstance(this.getContext()));

        // Adapter
        mAdapter = new FavoriteLNsAdapter(
                this.getContext(),
                viewModel
        );
        recyclerView.setAdapter(mAdapter);

        // Observer
        viewModel.getLiveFavoritesLNList().observe(FavoriteLNsFragment.this, mAdapter::setDatalist);
        viewModel.initLoadFavorites(mAdapter);

        return rootview;
    }

    @Override
    public void filterLNList(String keyword, String type, String status, String[] genres) {
        mAdapter.filterLNList(keyword, type, status, genres);
    }

    @Override
    public void showAll() {
        mAdapter.showAll();
    }
}
