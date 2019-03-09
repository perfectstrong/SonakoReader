package perfectstrong.sonako.sonakoreader.fragments;

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


/**
 * List of all LNs (including teaser, OLN)
 */
public class LNShowcaseFragment extends Fragment implements LNFilterable {

    private LNTitlesAdapter mAdapter;

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

        // specify an adapter
        mAdapter = new LNTitlesAdapter(this.getContext());
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

    //    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }
//
//    /**
//     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
//     * to the activity and potentially other fragments contained in that
//     * activity.
//     * <p>
//     * See the Android Training lesson <a href=
//     * "http://developer.android.com/training/basics/fragments/communicating.html"
//     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
}
