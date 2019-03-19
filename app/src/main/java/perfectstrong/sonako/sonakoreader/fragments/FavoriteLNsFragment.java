package perfectstrong.sonako.sonakoreader.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.database.LNDBViewModel;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabaseClient;


/**
 * Represent all bookmarked LN
 */
public class FavoriteLNsFragment extends Fragment implements LNFilterable {

    private FavoriteLNsAdapter mAdapter;

    public FavoriteLNsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // View model
        LNDBViewModel viewModel = ViewModelProviders.of(this)
                .get(LNDBViewModel.class);
        viewModel.setLndb(LightNovelsDatabaseClient.getInstance(this.getContext()));

        // Adapter
        mAdapter = new FavoriteLNsAdapter(
                this.getContext(),
                viewModel
        );
        // Observer
        viewModel.getLiveFavoritesLNList().observe(FavoriteLNsFragment.this, mAdapter::setDatalist);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_favorites_ln, container, false);
        RecyclerView recyclerView = rootview.findViewById(R.id.FavoriteLNsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
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
