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
import perfectstrong.sonako.sonakoreader.database.LNDBViewModel;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabaseClient;


/**
 * History of reading
 */
public class HistoryFragment extends Fragment {

    private HistoryAdapter mAdapter;

    public HistoryFragment() {
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
        mAdapter = new HistoryAdapter(this.getContext());

        // Observer
        viewModel.getLiveHistory().observe(
                HistoryFragment.this,
                mAdapter::setDatalist
        );
        viewModel.initLoadHistory(mAdapter);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        RecyclerView recyclerView = rootView.findViewById(R.id.HistoryRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);

        return rootView;
    }
}
