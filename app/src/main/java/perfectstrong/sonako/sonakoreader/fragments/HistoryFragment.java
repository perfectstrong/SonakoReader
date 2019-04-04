package perfectstrong.sonako.sonakoreader.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.adapter.HistoryAdapter;
import perfectstrong.sonako.sonakoreader.database.LNDBViewModel;


/**
 * History of reading
 */
public class HistoryFragment extends SonakoFragment {

    private HistoryAdapter adapter;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void showFilterDialog() {
        FragmentActivity activity = getActivity();
        if (activity == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        AlertDialog alertDialog = builder.setTitle(R.string.filter).create();
        View view = View.inflate(activity, R.layout.page_filter_dialog, null);
        alertDialog.setView(view);

        // Action
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setButton(
                Dialog.BUTTON_NEGATIVE,
                getString(R.string.no),
                (Message) null
        );
        alertDialog.setButton(
                Dialog.BUTTON_POSITIVE,
                getString(R.string.ok),
                (dialog, which) -> {
                    // Filter
                    String keyword = ((TextView) view.findViewById(R.id.keyword_selection))
                            .getText().toString().trim();
                    int daysLimit = getResources()
                            .getIntArray(R.array.date_limit_values)[
                            ((Spinner) view.findViewById(R.id.date_limit))
                                    .getSelectedItemPosition()
                            ];
                    adapter.filterPages(keyword, daysLimit);
                }
        );
        alertDialog.setButton(
                Dialog.BUTTON_NEUTRAL,
                getString(R.string.filter_reset),
                (dialog, which) -> adapter.showAll()
        );

        // Show
        alertDialog.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // View model
        LNDBViewModel viewModel = ViewModelProviders.of(this)
                .get(LNDBViewModel.class);

        // Adapter
        adapter = new HistoryAdapter();

        // Observer
        viewModel.getLiveHistory().observe(
                HistoryFragment.this,
                adapter::setDatalist
        );
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
        recyclerView.setAdapter(adapter);

        return rootView;
    }
}
