package perfectstrong.sonako.sonakoreader.helper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import perfectstrong.sonako.sonakoreader.MainActivity;
import perfectstrong.sonako.sonakoreader.PageReadingActivity;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.service.PageDownloadService;

public class Utils {
    public static String getSavDirForTag(String tag) {
        return PreferenceManager.getDefaultSharedPreferences(MainActivity.getContextOfApplication())
                .getString("SAVE_LOCATION", Config.DEFAULT_SAVE_LOCATION)
                + sanitize(tag) + "/";
    }

    public static String getCurrentSkin() {
        return PreferenceManager.getDefaultSharedPreferences(MainActivity.getContextOfApplication())
                .getString("SKIN", Config.DEFAULT_SKIN);
    }

    public static String getFilepath(String title, String tag) {
        return "file://"
                + Utils.getSavDirForTag(tag)
                + Utils.sanitize(title)
                + ".html";
    }

    public static String decode(String encodedString) {
        return Uri.decode(encodedString);
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

    public static boolean isNotCached(String title, String tag) {
        return !getTextFile(title, tag).exists();
    }

    public static File getTextFile(String title, String tag) {
        return new File(
                getSavDirForTag(tag),
                sanitize(title) + ".html"
        );
    }

    public static boolean isMainpage(String href) {
        return !(href.startsWith("Blog:")
                || href.startsWith("Blog_talk:")
                || href.startsWith("Board:")
                || href.startsWith("Board_Thread:")
                || href.startsWith("Category:")
                || href.startsWith("Category_talk:")
                || href.startsWith("File:")
                || href.startsWith("File_talk:")
                || href.startsWith("Help:")
                || href.startsWith("Help_talk:")
                || href.startsWith("Image:")
                || href.startsWith("Media:")
                || href.startsWith("MediaWiki:")
                || href.startsWith("MediaWiki_talk:")
                || href.startsWith("Message_Wall:")
                || href.startsWith("Message_Wall_Greeting:")
                || href.startsWith("Module:")
                || href.startsWith("Module_talk:")
                || href.startsWith("Project:")
                || href.startsWith("Project_talk:")
                || href.startsWith("Related_Videos:")
                || href.startsWith("Special:")
                || href.startsWith("Talk:")
                || href.startsWith("Template:")
                || href.startsWith("Template_talk:")
                || href.startsWith("Thread:")
                || href.startsWith("Topic:")
                || href.startsWith("User:")
                || href.startsWith("User_talk:")
                || href.startsWith("User_blog:")
                || href.startsWith("User_blog_comment:"));
    }

    private static final String[] AUTO_DOWNLOAD_CHOICES = {"Luôn tự động tải nếu chưa có trong bộ nhớ"};

    /**
     * Open page reader or download if not cached yet
     *
     * @param context activity
     * @param title   of new page to open
     * @param tag     tag of series
     * @param action  See {@link PageDownloadService.ACTION}
     */
    public static void openOrDownload(Context context,
                                      String title,
                                      String tag,
                                      PageDownloadService.ACTION action) {
        if (action == null)
            // Read or first download
            if (isNotCached(title, tag)) {
                if (PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(Config.PREF_AUTO_DOWNLOAD, false)) {
                    // If auto download, no need to create dialog
                    startDownloadTask(context, title, tag, null);
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
                                        startDownloadTask(context, title, tag, null);
                                    }
                            )
                            .setNegativeButton(
                                    context.getString(R.string.download_no),
                                    null
                            )
                            .show();
                }
            } else
                startReadingActivity(context, title, tag);
        else
            // Special action
            startDownloadTask(context, title, tag, action);
    }

    /**
     * Simply open reading activity
     *
     * @param context where to execute
     * @param title   of new page
     * @param tag     tag of series
     */
    private static void startReadingActivity(Context context,
                                             String title,
                                             String tag) {
        Intent i = new Intent(context, PageReadingActivity.class);
        i.putExtra(Config.EXTRA_TITLE, title);
        i.putExtra(Config.EXTRA_TAG, tag);
        context.startActivity(i);
    }

    /**
     * Demand download
     *
     * @param context where to execute
     * @param title   of new page
     * @param tag     tag of series
     * @param action  see {@link PageDownloadService.ACTION}
     */
    private static void startDownloadTask(Context context,
                                          String title,
                                          String tag,
                                          PageDownloadService.ACTION action) {
        Intent i = new Intent(context, PageDownloadService.class);
        i.putExtra(Config.EXTRA_TITLE, title);
        i.putExtra(Config.EXTRA_TAG, tag);
        i.putExtra(Config.EXTRA_ACTION, action);
        context.startService(i);
    }

    public static void massDownload(Context context,
                                    List<String> selectedLinks,
                                    String tag,
                                    PageDownloadService.ACTION action) {
        if (!selectedLinks.isEmpty()) {
            for (String title : selectedLinks) {
                startDownloadTask(context, title, tag, action);
            }
        }
    }
}
