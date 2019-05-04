package perfectstrong.sonako.sonakoreader.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.asyncTask.BiblioAsyncTask;
import perfectstrong.sonako.sonakoreader.asyncTask.HistoryAsyncTask;
import perfectstrong.sonako.sonakoreader.asyncTask.LNDatabaseAsyncTask;
import perfectstrong.sonako.sonakoreader.helper.Utils;

public class SettingsFragment extends PreferenceFragmentCompat {

    public static final String FRAGMENT_TAG = "SETTINGS_FRAGMENT";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        if ((getString(R.string.key_interface_options)).equals(rootKey)) {
            Preference prefSkin = findPreference(getString(R.string.key_pref_skin));
            if (prefSkin != null) {
                prefSkin.setSummary(Utils.getCurrentSkin());
                // Manually handle this
                prefSkin.setOnPreferenceClickListener(preference -> {
                    showSkinSelectDialog();
                    return true;
                });
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    private void showSkinSelectDialog() {
        FragmentActivity activity = getActivity();
        if (activity == null) return;
        // Find current skin
        int[] currentPos = {-1};
        String currentSkin = Utils.getCurrentSkin();
        String[] skins = getResources().getStringArray(R.array.pref_skin_values);
        for (int i = 0; i < skins.length; i++) {
            if (skins[i].equals(currentSkin)) {
                currentPos[0] = i;
                break;
            }
        }
        // Build dialog
        new AlertDialog.Builder(activity)
                .setTitle(R.string.theme_changing_caution)
                .setSingleChoiceItems(
                        R.array.pref_skin_values,
                        currentPos[0],
                        null
                )
                .setPositiveButton(
                        R.string.ok,
                        (dialog, which) -> {
                            ListView lw = ((AlertDialog) dialog).getListView();
                            which = lw.getCheckedItemPosition();
                            if (which != currentPos[0]) {
                                PreferenceManager.getDefaultSharedPreferences(this.getActivity())
                                        .edit()
                                        .putString(
                                                getString(R.string.key_pref_skin),
                                                skins[which]
                                        )
                                        .commit();
                                Utils.updateTheme(getActivity());
                                Utils.restartApp(getActivity());
                                getActivity().finish();
                            }
                        }
                )
                .setNegativeButton(
                        R.string.no,
                        null
                )
                .setCancelable(true)
                .show();
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
