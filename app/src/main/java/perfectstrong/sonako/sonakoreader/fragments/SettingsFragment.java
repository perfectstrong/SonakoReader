package perfectstrong.sonako.sonakoreader.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.activity.SettingsActivity;
import perfectstrong.sonako.sonakoreader.asyncTask.BiblioAsyncTask;
import perfectstrong.sonako.sonakoreader.asyncTask.HistoryAsyncTask;
import perfectstrong.sonako.sonakoreader.asyncTask.LNDatabaseAsyncTask;

public class SettingsFragment extends PreferenceFragmentCompat {

    public static final String FRAGMENT_TAG = "SETTINGS_FRAGMENT";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        if ((getString(R.string.key_interface_options)).equals(rootKey)) {
            Preference prefSkin = findPreference(getString(R.string.key_pref_skin));
            if (prefSkin != null) {
                prefSkin.setOnPreferenceChangeListener((preference, newValue) -> {
                    String str = (String) newValue;
                    //noinspection ConstantConditions
                    ((SettingsActivity) getActivity()).setSkin(str);
                    getActivity().recreate();
                    return true;
                });
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String title = String.valueOf(preference.getTitle());
        Context context = getContext();
        if (context != null)
            if (getString(R.string.title_update_lndb).equals(title)) {
                new LNDatabaseAsyncTask.Update().execute();
                return true;
            } else if (getString(R.string.title_clear_history).equals(title)) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.confirm_delete)
                        .setPositiveButton(
                                R.string.ok,
                                (dialog, which) -> new HistoryAsyncTask.Clear().execute()
                        )
                        .setNegativeButton(
                                R.string.no,
                                null
                        )
                        .show();
                return true;
            } else if (getString(R.string.title_clear_lndb).equals(title)) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.confirm_delete)
                        .setPositiveButton(
                                R.string.ok,
                                (dialog, which) -> new LNDatabaseAsyncTask.Clear().execute()
                        )
                        .setNegativeButton(
                                R.string.no,
                                null
                        )
                        .show();
                return true;
            } else if (getString(R.string.title_update_biblio).equals(title)) {
                new BiblioAsyncTask.Update().execute();
            } else if (getString(R.string.title_clear_biblio).equals(title)) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.confirm_delete)
                        .setPositiveButton(
                                R.string.ok,
                                (dialog, which) -> new BiblioAsyncTask.Clear().execute()
                        )
                        .setNegativeButton(
                                R.string.no,
                                null
                        )
                        .show();
            }
        return super.onPreferenceTreeClick(preference);
    }
}
