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
import perfectstrong.sonako.sonakoreader.asyncTask.LNDatabaseAsyncTask;
import perfectstrong.sonako.sonakoreader.database.LNDBViewModel;

/**
 * List of all LNs (including teaser, OLN)
 */
public class LNShowcaseFragment extends LNFilterableFragment {

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
        adapter = new LNListAdapter(viewModel);

        // Observer
        viewModel.getLiveLNList().observe(
                this,
                titles -> {
                    adapter.setDatalist(titles);
                    this.updateView(this.getView());
                }
        );
    }

    private void updateView(View view) {
        if (view == null) return;
        if (adapter.getItemCount() == 0) {
            view.findViewById(R.id.NoDatabaseGroup).setVisibility(View.VISIBLE);
            view.findViewById(R.id.NoDatabaseButton)
                    .setOnClickListener(v -> forceDownload());
            view.findViewById(R.id.RecyclerView).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.NoDatabaseGroup).setVisibility(View.GONE);
            view.findViewById(R.id.RecyclerView).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_lnshowcase, container, false);
        updateView(rootView);
        RecyclerView recyclerView = rootView.findViewById(R.id.RecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        return rootView;
    }

    private void forceDownload() {
        new LNDatabaseAsyncTask.LoadCacheOrDownload(
                true
        ).execute();
    }
}
