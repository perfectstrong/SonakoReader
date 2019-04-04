package perfectstrong.sonako.sonakoreader.activity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import perfectstrong.sonako.sonakoreader.fragments.SettingsFragment;

public class SettingsActivity extends SonakoActivity implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            // Create the fragment only when the activity is created for the first time.
            // ie. not after orientation changes
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(SettingsFragment.FRAGMENT_TAG);
            if (fragment == null) {
                fragment = new SettingsFragment();
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment, SettingsFragment.FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat caller,
                                           PreferenceScreen preferenceScreen) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.slide_in_left,
                android.R.anim.slide_out_right,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right);
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.getKey());
        fragment.setArguments(args);
        ft.replace(android.R.id.content, fragment, preferenceScreen.getKey());
        ft.addToBackStack(preferenceScreen.getKey());
        ft.commit();
        return true;
    }
}
