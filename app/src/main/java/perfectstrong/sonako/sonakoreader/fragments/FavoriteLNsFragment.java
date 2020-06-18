package perfectstrong.sonako.sonakoreader.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

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
        LNDBViewModel viewModel = new ViewModelProvider(this).get(LNDBViewModel.class);

        // Adapter
        adapter = new LNListAdapter(viewModel);
        // Observer
        viewModel.getLiveFavoritesLNList().observe(FavoriteLNsFragment.this, adapter::setDatalist);
    }

    @Override
    protected void updateView(View rootView) {
        // Do nothing
    }
}
