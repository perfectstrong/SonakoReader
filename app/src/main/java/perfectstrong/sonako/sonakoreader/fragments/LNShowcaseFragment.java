package perfectstrong.sonako.sonakoreader.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

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
        LNDBViewModel viewModel = new ViewModelProvider(this).get(LNDBViewModel.class);

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

    @Override
    protected void updateView(View view) {
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
    protected int getLayout() {
        return R.layout.lndatabase;
    }

    private void forceDownload() {
        new LNDatabaseAsyncTask.LoadCacheOrDownload(
                true
        ).execute();
    }
}
