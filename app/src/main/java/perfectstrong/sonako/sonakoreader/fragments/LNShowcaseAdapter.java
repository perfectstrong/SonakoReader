package perfectstrong.sonako.sonakoreader.fragments;

import android.content.Context;

import perfectstrong.sonako.sonakoreader.database.LNDBViewModel;

public class LNShowcaseAdapter extends LNListAdapter {
    LNShowcaseAdapter(Context context, LNDBViewModel viewModel) {
        super(context, viewModel);
    }

    boolean haveEmptyLNList() {
        return lnList == null || lnList.isEmpty();
    }
}
