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
import perfectstrong.sonako.sonakoreader.adapter.LNListAdapter;
import perfectstrong.sonako.sonakoreader.asyncTask.LNDatabaseAsyncTask;
import perfectstrong.sonako.sonakoreader.database.LNDBViewModel;


/**
 * List of all LNs (including teaser, OLN)
 */
public class LNShowcaseFragment extends Fragment implements LNFilterable {

    private LNListAdapter mAdapter;

    public LNShowcaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // View model
        LNDBViewModel viewModel = ViewModelProviders.of(this)
                .get(LNDBViewModel.class);

        // Adapter
        mAdapter = new LNListAdapter(viewModel);

        // Observer
        viewModel.getLiveLNList().observe(
                this,
                titles -> {
                    mAdapter.setDatalist(titles);
                    this.updateView(this.getView());
                }
        );
    }

    private void updateView(View view) {
        if (view == null) return;
        if (mAdapter.getItemCount() == 0) {
            view.findViewById(R.id.LNTitlesNoDatabaseGroup).setVisibility(View.VISIBLE);
            view.findViewById(R.id.LNTitlesNoDatabaseButton)
                    .setOnClickListener(v -> forceDownload());
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
        updateView(rootView);
        RecyclerView recyclerView = rootView.findViewById(R.id.LNTitlesRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
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

    private void forceDownload() {
        new LNDatabaseAsyncTask.LoadCacheOrDownload(
                true
        ).execute();
    }
}
