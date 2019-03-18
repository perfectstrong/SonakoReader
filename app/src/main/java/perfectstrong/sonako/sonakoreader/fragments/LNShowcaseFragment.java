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
import perfectstrong.sonako.sonakoreader.asyncTask.LNDatabaseAsyncTask;
import perfectstrong.sonako.sonakoreader.database.LNDBViewModel;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabaseClient;


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
        // View model
        LNDBViewModel viewModel = ViewModelProviders.of(this)
                .get(LNDBViewModel.class);
        viewModel.setLndb(LightNovelsDatabaseClient.getInstance(this.getContext()));

        // Adapter
        mAdapter = new LNShowcaseAdapter(
                this.getContext(),
                viewModel
        );

        // Observer
        viewModel.getLiveLNList().observe(
                this,
                titles -> {
                    mAdapter.setDatalist(titles);
                    this.updateView();
                }
        );
    }

    private void updateView() {
        View view = this.getView();
        if (view == null) return;
        if (mAdapter.haveEmptyLNList()) {
            view.findViewById(R.id.LNTitlesNoDatabaseGroup).setVisibility(View.VISIBLE);
            view.findViewById(R.id.LNTitlesRecyclerView).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.LNTitlesNoDatabaseGroup).setVisibility(View.GONE);
            view.findViewById(R.id.LNTitlesRecyclerView).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_lnshowcase, container, false);
        RecyclerView recyclerView = rootView.findViewById(R.id.LNTitlesRecyclerView);
        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // Button to force download
        rootView.findViewById(R.id.LNTitlesNoDatabaseButton)
                .setOnClickListener(this::forceDownload);

        // fetch initial data
        new LNDatabaseAsyncTask.LoadCacheOrDownload(
                LightNovelsDatabaseClient.getInstance(getContext()),
                mAdapter,
                false,
                this::updateView
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

    private void forceDownload(View v) {
        new LNDatabaseAsyncTask.LoadCacheOrDownload(
                LightNovelsDatabaseClient.getInstance(getContext()),
                mAdapter,
                true,
                null
        ).execute();
    }
}
