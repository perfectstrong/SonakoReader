package perfectstrong.sonako.sonakoreader.helper;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import perfectstrong.sonako.sonakoreader.PageReadingActivity;
import perfectstrong.sonako.sonakoreader.R;

public class Utils {
    public static String getSaveLocationForTag(String tag) {
        return Config.SAVE_LOCATION + sanitize(tag) + "/";
    }

    private static final String REPLACEMENT_CHAR = " ";

    public static String sanitize(String tag) {
        return tag.replaceAll("[|?*<\":>+\\[\\]/'_]", REPLACEMENT_CHAR);
    }

    public static String removeSubtrait(String tag) {
        return tag.replaceAll("_", REPLACEMENT_CHAR);
    }

    public static String getFileNameFromURL(String url) {
        if (url == null) {
            return "";
        }
        try {
            URL resource = new URL(url);
            String host = resource.getHost();
            if (host.length() > 0 && url.endsWith(host)) {
                // handle ...example.com
                return "";
            }
        } catch (MalformedURLException e) {
            return "";
        }

        int startIndex = url.lastIndexOf('/') + 1;
        int length = url.length();

        // find end index for ?
        int lastQMPos = url.lastIndexOf('?');
        if (lastQMPos == -1) {
            lastQMPos = length;
        }

        // find end index for #
        int lastHashPos = url.lastIndexOf('#');
        if (lastHashPos == -1) {
            lastHashPos = length;
        }

        // calculate the end index
        int endIndex = Math.min(lastQMPos, lastHashPos);
        return url.substring(startIndex, endIndex);
    }

    public static boolean isCached(String title, String tag) {
        return new File(
                getSaveLocationForTag(tag),
                sanitize(title) + ".html"
        ).exists();
    }

    private static final String[] AUTO_DOWNLOAD_CHOICES = {"Luôn tự động tải nếu chưa có trong bộ nhớ"};

    /**
     * Open page reader or download if not cached yet
     * @param context activity
     * @param title of new page to open
     * @param tag tag of series
     * @param action See {@link PageReadingActivity.ACTION}
     */
    public static void loadCacheOrDownload(Context context,
                                           String title,
                                           String tag,
                                           PageReadingActivity.ACTION action) {
        if (!isCached(title, tag)) {
            if (PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(Config.PREF_AUTO_DOWNLOAD, false)) {
                // If auto download, no need to create dialog
                startDownloadTask(context, title, tag);
            } else {
                final boolean[] autodownload = {false};
                // Demand before downloading
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Trang này chưa được tải. Bạn có muốn tải?")
                        .setMultiChoiceItems(
                                AUTO_DOWNLOAD_CHOICES,
                                null,
                                (dialog, which, isChecked) -> autodownload[0] = which == 0 && isChecked
                        )
                        .setPositiveButton(
                                context.getString(R.string.download_ok),
                                (dialog, which) -> {
                                    if (autodownload[0]) {
                                        PreferenceManager
                                                .getDefaultSharedPreferences(context)
                                                .edit()
                                                .putBoolean(Config.PREF_AUTO_DOWNLOAD, true)
                                                .apply();
                                    }
                                    startDownloadTask(context, title, tag);
                                }
                        )
                        .setNegativeButton(
                                context.getString(R.string.download_no),
                                null
                        )
                        .show();
            }
        } else {
            startReadingActivity(context, title, tag, action);
        }
    }

    /**
     * Simply open reading activity. May trigger downloading
     * @param context where to execute
     * @param title of new page
     * @param tag tag of series
     * @param action see {@link PageReadingActivity.ACTION}
     */
    public static void startReadingActivity(Context context,
                                            String title,
                                            String tag,
                                            PageReadingActivity.ACTION action) {
        Intent i = new Intent(context, PageReadingActivity.class);
        i.putExtra(Config.EXTRA_TITLE, title);
        i.putExtra(Config.EXTRA_TAG, tag);
        i.putExtra(Config.EXTRA_ACTION, action);
        context.startActivity(i);
    }

    private static void startDownloadTask(Context context,
                                          String title,
                                          String tag) {
        // TODO

    }
}
