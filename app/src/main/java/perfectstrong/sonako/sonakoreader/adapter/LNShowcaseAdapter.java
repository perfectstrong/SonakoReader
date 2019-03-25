package perfectstrong.sonako.sonakoreader.adapter;

import android.content.Context;

import perfectstrong.sonako.sonakoreader.database.LNDBViewModel;

public class LNShowcaseAdapter extends LNListAdapter {
    public LNShowcaseAdapter(Context context, LNDBViewModel viewModel) {
        super(context, viewModel);
    }

    public boolean haveEmptyLNList() {
        return lnList == null || lnList.isEmpty();
    }
}
