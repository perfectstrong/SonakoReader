package perfectstrong.sonako.sonakoreader.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import perfectstrong.sonako.sonakoreader.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
