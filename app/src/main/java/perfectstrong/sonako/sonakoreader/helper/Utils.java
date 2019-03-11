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
import perfectstrong.sonako.sonakoreader.service.PageDownloadService;

public class Utils {
    public static String getSavDirForTag(String tag) {
        return Config.SAVE_LOCATION + sanitize(tag) + "/";
    }

    public static String getFilepath(String title, String tag) {
        return "file://"
                + Utils.getSavDirForTag(tag)
                + Utils.sanitize(title)
                + ".html";
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
        return !new File(
                getSavDirForTag(tag),
                sanitize(title) + ".html"
        ).exists();
    }

    public static boolean isMainpage(String href) {
        return !href.startsWith("/wiki/Blog:")
                || href.startsWith("/wiki/Blog_talk:")
                || href.startsWith("/wiki/Board:")
                || href.startsWith("/wiki/Board_Thread:")
                || href.startsWith("/wiki/Category:")
                || href.startsWith("/wiki/Category_talk:")
                || href.startsWith("/wiki/File:")
                || href.startsWith("/wiki/File_talk:")
                || href.startsWith("/wiki/Help:")
                || href.startsWith("/wiki/Help_talk:")
                || href.startsWith("/wiki/Image:")
                || href.startsWith("/wiki/Media:")
                || href.startsWith("/wiki/MediaWiki:")
                || href.startsWith("/wiki/MediaWiki_talk:")
                || href.startsWith("/wiki/Message_Wall:")
                || href.startsWith("/wiki/Message_Wall_Greeting:")
                || href.startsWith("/wiki/Module:")
                || href.startsWith("/wiki/Module_talk:")
                || href.startsWith("/wiki/Project:")
                || href.startsWith("/wiki/Project_talk:")
                || href.startsWith("/wiki/Related_Videos:")
                || href.startsWith("/wiki/Special:")
                || href.startsWith("/wiki/Talk:")
                || href.startsWith("/wiki/Template:")
                || href.startsWith("/wiki/Template_talk:")
                || href.startsWith("/wiki/Thread:")
                || href.startsWith("/wiki/Topic:")
                || href.startsWith("/wiki/User:")
                || href.startsWith("/wiki/User_talk:")
                || href.startsWith("/wiki/User_blog:")
                || href.startsWith("/wiki/User_blog_comment:");
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
}
