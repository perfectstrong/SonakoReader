package perfectstrong.sonako.sonakoreader.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.adapter.LNListAdapter;
import perfectstrong.sonako.sonakoreader.database.LNDBViewModel;


/**
 * Represent all bookmarked LN
 */
public class FavoriteLNsFragment extends LNFilterableFragment {

    public FavoriteLNsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // View model
        LNDBViewModel viewModel = ViewModelProviders.of(this)
                .get(LNDBViewModel.class);

        // Adapter
        adapter = new LNListAdapter(viewModel);
        // Observer
        viewModel.getLiveFavoritesLNList().observe(FavoriteLNsFragment.this, adapter::setDatalist);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_favorites_ln, container, false);
        RecyclerView recyclerView = rootview.findViewById(R.id.FavoriteLNsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        return rootview;
    }
}
