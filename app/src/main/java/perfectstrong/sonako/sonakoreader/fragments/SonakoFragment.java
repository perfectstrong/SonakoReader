package perfectstrong.sonako.sonakoreader.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.helper.Utils;

public abstract class SonakoFragment extends Fragment {

    RecyclerView recyclerView;

    public SonakoFragment() {
        // Required empty public constructor
    }

    public abstract void showFilterDialog();

    protected abstract void updateView(View rootView);

    int getLayout() {
        return R.layout.ln_recycler_list;
    }

    protected abstract RecyclerView.Adapter getAdapter();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(getLayout(), container, false);
        updateView(rootView);
        recyclerView = rootView.findViewById(R.id.RecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(getAdapter());
        return rootView;
    }

    void showShowAllBtn() {
        if (this.getView() != null
                && this.getView().findViewById(R.id.lnfilterable_show_all) != null)
            Utils.slideIn(this.getView().findViewById(R.id.lnfilterable_show_all), R.anim.top_down);
    }

    void hideShowAllBtn() {
        if (this.getView() != null
                && this.getView().findViewById(R.id.lnfilterable_show_all) != null)
            Utils.slideOut(this.getView().findViewById(R.id.lnfilterable_show_all), R.anim.top_up);
    }
}
