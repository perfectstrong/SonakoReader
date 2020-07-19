package perfectstrong.sonako.sonakoreader.fragments;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.SonakoReaderApp;
import perfectstrong.sonako.sonakoreader.asyncTask.BiblioAsyncTask;
import perfectstrong.sonako.sonakoreader.asyncTask.HistoryAsyncTask;
import perfectstrong.sonako.sonakoreader.asyncTask.LNDatabaseAsyncTask;
import perfectstrong.sonako.sonakoreader.helper.ExtraFontSupport;
import perfectstrong.sonako.sonakoreader.helper.Utils;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String FRAGMENT_TAG = "SETTINGS_FRAGMENT";
    private static final String TAG = SettingsFragment.class.getSimpleName();
    private ActivityResultLauncher<String> selectedFontGetter = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::handleSelectedFontFile
    );

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
        Preference readingFontPref = findPreference(getString(R.string.key_reading_font));
        if (readingFontPref != null) {
            ExtraFontSupport.CustomFont currentReadingFont = Utils.getCurrentFont();
            readingFontPref.setSummary(
                    currentReadingFont.isDefault()
                            ? getString(R.string.default_reading_font_summary)
                            : currentReadingFont.name
            );
            readingFontPref.setOnPreferenceClickListener(preference -> {
                showReadingFontSelectDialog();
                return true;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference readingFontPref = findPreference(getString(R.string.key_reading_font));
        if (readingFontPref != null) {
            ExtraFontSupport.CustomFont currentReadingFont = Utils.getCurrentFont();
            readingFontPref.setSummary(
                    currentReadingFont.isDefault()
                            ? getString(R.string.default_reading_font_summary)
                            : currentReadingFont.name
            );
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

    private void showReadingFontSelectDialog() {
        FragmentActivity activity = getActivity();
        if (activity == null) return;
        // Build dialog
        File fontDir = new File(ExtraFontSupport.EXTERNAL_FONT_LOCATION);
        if (!fontDir.exists()) {
            if (fontDir.mkdirs())
                Log.d(TAG, "Created font directory");
        }
        // List of all possible fonts
        // The first one represent the choice by theme
        // Then the internal available fonts
        // And lastly the downloaded in local font directory
        List<ExtraFontSupport.CustomFont> customFontList = new ArrayList<>();
        customFontList.add(ExtraFontSupport.CustomFont.DEFAULT); // Default by theme
        customFontList.addAll(ExtraFontSupport.getAvailableInternalChoices());
        File[] externalFonts = fontDir.listFiles();
        if (externalFonts == null) externalFonts = new File[0];
        for (File font : externalFonts) {
            customFontList.add(new ExtraFontSupport.CustomFont(font.getName(), true));
        }
        ExtraFontSupport.CustomFont currentReadingFont = Utils.getCurrentFont();
        int selectedFontIndex = customFontList.indexOf(currentReadingFont);
        new AlertDialog.Builder(activity)
                .setTitle(getString(R.string.reading_font_select_dialog_caution))
                .setSingleChoiceItems(new CustomFontArrayAdapter(activity, customFontList),
                        selectedFontIndex,
                        (dialog, which) -> {
                            ExtraFontSupport.CustomFont chosenFont = customFontList.get(which);
                            PreferenceManager.getDefaultSharedPreferences(this.getActivity())
                                    .edit()
                                    .putString(
                                            getString(R.string.key_reading_font),
                                            chosenFont.getEncodedName()
                                    )
                                    .apply();
                            dialog.dismiss();
                        })
                .setNeutralButton(
                        R.string.reading_font_select_dialog_select_font,
                        (dialog, which) -> selectedFontGetter.launch("font/*")
                )
                .setNegativeButton(
                        R.string.no,
                        null
                )
                .setCancelable(true)
                .show();
    }

    private static class CustomFontArrayAdapter extends ArrayAdapter<ExtraFontSupport.CustomFont> {

        public CustomFontArrayAdapter(@NonNull Context context, @NonNull List<ExtraFontSupport.CustomFont> customFonts) {
            super(context,
                    R.layout.dialog_custom_font_single_choice,
                    R.id.custom_font_name,
                    customFonts);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Context context = parent.getContext();
            if (convertView == null)
                convertView = LayoutInflater.from(context)
                        .inflate(android.R.layout.simple_list_item_1, parent, false);
            ExtraFontSupport.CustomFont customFont = super.getItem(position);
            assert customFont != null;
            TextView textView = (TextView) convertView;
            textView.setText(customFont.name == null ? context.getString(R.string.default_reading_font_summary) : customFont.name);
            if (!customFont.isDefault()) {
                textView.setTypeface(customFont.getTypeFace(context));
            } else
                textView.setTypeface(Typeface.DEFAULT);
            return textView;
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

    @SuppressLint("ApplySharedPref")
    private void handleSelectedFontFile(Uri uri) {
        if (uri == null || uri.getPath() == null || getContext() == null) return;
        String filename = Utils.getFileName(this.getContext(), uri);
        ContentResolver cr = getContext().getContentResolver();
        try (InputStream is = cr.openInputStream(uri);
             // Copy selected font file to library directory
             FileOutputStream fos = new FileOutputStream(new File(ExtraFontSupport.EXTERNAL_FONT_LOCATION + filename))) {
            if (is == null) {
                Log.d(TAG, "handleSelectedFontFile: Unreadable source " + uri.toString());
                return;
            }
            byte[] b = new byte[8];
            int i;
            while ((i = is.read(b)) != -1) {
                fos.write(b, 0, i);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "handleSelectedFontFile: Failed on copying source font " + uri.toString());
            return;
        }
        // Set the preference
        PreferenceManager.getDefaultSharedPreferences(SonakoReaderApp.getContext())
                .edit()
                .putString(
                        getString(R.string.key_reading_font),
                        filename
                )
                .commit();
    }
}
