package perfectstrong.sonako.sonakoreader.adapter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import perfectstrong.sonako.sonakoreader.fragments.SonakoFragment;

public class MainActivityPagerAdapter extends FragmentPagerAdapter {

    private final List<SonakoFragment> fragmentList = new ArrayList<>();
    private final List<String> fragmentTitles = new ArrayList<>();

    public MainActivityPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    @NonNull
    public SonakoFragment getItem(int i) {
        return fragmentList.get(i);
    }

    @Override
    @NonNull
    public String getPageTitle(int position) {
        return fragmentTitles.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    public void addFragment(SonakoFragment fragment, String fragmentTitle) {
        fragmentList.add(fragment);
        fragmentTitles.add(fragmentTitle);
    }
}
