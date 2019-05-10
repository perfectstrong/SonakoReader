package perfectstrong.sonako.sonakoreader.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.adapter.LNListAdapter;
import perfectstrong.sonako.sonakoreader.database.LightNovel;

public abstract class LNFilterableFragment extends SonakoFragment {
    LNListAdapter adapter;

    @Override
    public LNListAdapter getAdapter() {
        return adapter;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if (v != null
                && v.findViewById(R.id.lnfilterable_show_all) != null) {
            final View showAllBtn = v.findViewById(R.id.lnfilterable_show_all);
            showAllBtn.setOnClickListener(v1 -> {
                adapter.showAll();
                hideShowAllBtn();
            });
        }
        return v;
    }

    @Override
    public void showFilterDialog() {
        FragmentActivity activity = getActivity();
        if (activity == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        AlertDialog alertDialog = builder.setTitle(R.string.filter).create();
        View view = View.inflate(activity, R.layout.ln_title_filter_dialog, null);
        alertDialog.setView(view);

        // Hint for genres
        ArrayAdapter<String> genresHintAdapter = new ArrayAdapter<>(
                activity,
                android.R.layout.simple_dropdown_item_1line,
                LightNovel.ProjectGenre.ALL
        );
        MultiAutoCompleteTextView genresTextView = view.findViewById(R.id.genres_selection);
        assert genresTextView != null;
        genresTextView.setAdapter(genresHintAdapter);
        genresTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        // Hint for type
        ArrayAdapter<String> typesAdapter = new ArrayAdapter<>(
                activity,
                android.R.layout.simple_spinner_item,
                LightNovel.ProjectType.CHOICES
        );
        typesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner typesSpinner = view.findViewById(R.id.type_selection);
        typesSpinner.setAdapter(typesAdapter);

        // Hint for status
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                activity,
                android.R.layout.simple_spinner_item,
                LightNovel.ProjectStatus.CHOICES
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner statusSpinner = view.findViewById(R.id.status_selection);
        statusSpinner.setAdapter(statusAdapter);

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
                    String type = typesSpinner.getSelectedItem().toString();
                    String status = statusSpinner.getSelectedItem().toString();
                    String[] genres = genresTextView.getText().toString().split("\\s*,\\s*");
                    if (genres.length == 1 && genres[0].equals("")) genres = new String[]{};
                    adapter.filterLNList(
                            keyword,
                            type,
                            status,
                            genres
                    );
                    showShowAllBtn();
                }
        );
        alertDialog.setButton(
                Dialog.BUTTON_NEUTRAL,
                getString(R.string.filter_reset),
                (dialog, which) -> {
                    adapter.showAll();
                    hideShowAllBtn();
                }
        );

        // Show
        alertDialog.show();
    }
}
