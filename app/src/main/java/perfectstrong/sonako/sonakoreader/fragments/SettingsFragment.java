package perfectstrong.sonako.sonakoreader.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.asyncTask.HistoryAsyncTask;
import perfectstrong.sonako.sonakoreader.asyncTask.LNDatabaseAsyncTask;
import perfectstrong.sonako.sonakoreader.database.LightNovelsDatabaseClient;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String title = String.valueOf(preference.getTitle());
        Context context = getContext();
        if (context != null)
            if (getString(R.string.title_update_lndb).equals(title)) {
                new LNDatabaseAsyncTask.Update(
                        LightNovelsDatabaseClient.getInstance(context),
                        context
                )
                        .execute();
                return true;
            } else if (getString(R.string.title_clear_history).equals(title)) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.confirm_delete)
                        .setPositiveButton(
                                R.string.ok,
                                (dialog, which) -> new HistoryAsyncTask.Clear(
                                        LightNovelsDatabaseClient.getInstance(context)
                                ).execute()
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
                                (dialog, which) -> new LNDatabaseAsyncTask.Clear(
                                        LightNovelsDatabaseClient.getInstance(this.getContext())
                                ).execute()
                        )
                        .setNegativeButton(
                                R.string.no,
                                null
                        )
                        .show();
                return true;
            }
        return super.onPreferenceTreeClick(preference);
    }
}
