package perfectstrong.sonako.sonakoreader;

import android.os.Bundle;

import perfectstrong.sonako.sonakoreader.fragments.SettingsFragment;

public class SettingsActivity extends SonakoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
