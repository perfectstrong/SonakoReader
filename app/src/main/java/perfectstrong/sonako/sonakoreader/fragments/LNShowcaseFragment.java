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
import perfectstrong.sonako.sonakoreader.asyncTask.LNShowcaseAsyncLoader;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabaseClient;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabaseViewModel;


/**
 * List of all LNs (including teaser, OLN)
 */
public class LNShowcaseFragment extends Fragment implements LNFilterable {

    private LNShowcaseAdapter mAdapter;

    public LNShowcaseFragment() {
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
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_lnshowcase, container, false);
        RecyclerView recyclerView = rootView.findViewById(R.id.LNTitlesRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // View model
        LightNovelsDatabaseViewModel viewModel = ViewModelProviders.of(this)
                .get(LightNovelsDatabaseViewModel.class);
        viewModel.setLndb(LightNovelsDatabaseClient.getInstance(this.getContext()));

        // Adapter
        mAdapter = new LNShowcaseAdapter(
                this.getContext(),
                viewModel
        );
        recyclerView.setAdapter(mAdapter);

        // fetch data
        new LNShowcaseAsyncLoader(
                LightNovelsDatabaseClient.getInstance(getContext()),
                this,
                mAdapter
        ).execute();

        return rootView;
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
