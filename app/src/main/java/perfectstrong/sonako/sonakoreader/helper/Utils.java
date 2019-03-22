package perfectstrong.sonako.sonakoreader.helper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;
import perfectstrong.sonako.sonakoreader.PageReadingActivity;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.SonakoReaderApp;
import perfectstrong.sonako.sonakoreader.service.PageDownloadService;

@SuppressWarnings("WeakerAccess")
public class Utils {

    @SuppressWarnings("WeakerAccess")
    public static String getSaveDir() {
        return PreferenceManager.getDefaultSharedPreferences(SonakoReaderApp.getContext())
                .getString("SAVE_LOCATION", Config.DEFAULT_SAVE_LOCATION);
    }

    public static String getSavDirForTag(String tag) {
        return getSaveDir() + sanitize(tag) + "/";
    }

    public static String getCurrentSkin() {
        Context context = SonakoReaderApp.getContext();
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.key_pref_skins),
                        context.getResources().getString(R.string.default_skin)
                );
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
                        .getBoolean(
                                context.getString(R.string.key_pref_download_noncached_pages),
                                context.getResources().getBoolean(R.bool.default_download_noncached_pages)
                        )) {
                    // If auto download, no need to create dialog
                    startDownloadTask(context, title, tag, null);
                } else {
                    final boolean[] autodownload = {
                            context.getResources().getBoolean(R.bool.default_download_noncached_pages)
                    };
                    // Demand before downloading
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(context.getString(R.string.wanna_download))
                            .setMultiChoiceItems(
                                    AUTO_DOWNLOAD_CHOICES,
                                    null,
                                    (dialog, which, isChecked) -> autodownload[0] = which == 0 && isChecked
                            )
                            .setPositiveButton(
                                    context.getString(R.string.ok),
                                    (dialog, which) -> {
                                        if (autodownload[0]) {
                                            PreferenceManager
                                                    .getDefaultSharedPreferences(context)
                                                    .edit()
                                                    .putBoolean(
                                                            context.getString(R.string.key_pref_download_noncached_pages),
                                                            true)
                                                    .apply();
                                        }
                                        startDownloadTask(context, title, tag, null);
                                    }
                            )
                            .setNegativeButton(
                                    context.getString(R.string.no),
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

    /**
     * @param context to get service
     * @return first boolean indicates wifi, second boolean indicates mobile
     */
    public static boolean[] getNetworkConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return new boolean[]{haveConnectedWifi, haveConnectedMobile};
    }

    /**
     * @param context <tt>null</tt> to reference to application context
     * @return <tt>true</tt> if is allowed to download over cellular connection
     */
    public static boolean isNotAuthorizedDownloadingOverCellularConnection(Context context) {
        return !Objects.requireNonNull(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(
                        context.getString(R.string.key_pref_download_when),
                        context.getString(R.string.default_download_when)
                ))
                .equals(context.getString(R.string.download_when_cellular_value));
    }

    /**
     * @throws ConnectException when no internet or only allowed to used Wifi
     */
    public static void checkConnection() throws ConnectException {
        Context context = SonakoReaderApp.getContext();
        boolean[] connections = Utils.getNetworkConnection(context);
        if (!connections[0] && !connections[1]) {
            throw new ConnectException(context.getString(R.string.no_internet));
        } else if (connections[1]
                && Utils.isNotAuthorizedDownloadingOverCellularConnection(context)) {
            throw new ConnectException(context.getString(R.string.not_authorized_cellular));
        }
    }

    /**
     * @param href direct link
     * @return <tt>true</tt> if link contains <tt>wikia.nocookie</tt>
     */
    public static boolean isInternalImage(@NonNull String href) {
        return href.contains("wikia.nocookie");
    }

    /**
     * @return <tt>true</tt> if user allows in settings
     */
    public static boolean isAllowedToDownloadExternalImages() {
        Context context = SonakoReaderApp.getContext();
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(
                        context.getString(
                                R.string.key_pref_download_external_images),
                        context.getResources().getBoolean(
                                R.bool.default_download_external_images)
                );
    }

    /**
     * @return <tt>true</tt> if allowed to download original wikia image
     */
    public static boolean isAllowedToDownloadOriginalImages() {
        Context context = SonakoReaderApp.getContext();
        return context.getString(R.string.download_wikia_images_original_value)
                .equals(
                        PreferenceManager.getDefaultSharedPreferences(context)
                                .getString(
                                        context.getString(R.string.key_pref_download_wikia_images),
                                        context.getString(R.string.default_download_wikia_images)
                                )
                );
    }

    /**
     * @return 2 integers: first describes horizontal size, second describes vertical size
     */
    public static Point getScreenSize() {
        Context context = SonakoReaderApp.getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static int getPreferredSize() {
        Context context = SonakoReaderApp.getContext();
        if (context.getString(R.string.download_wikia_images_not_over_1024px_value)
                .equals(
                        PreferenceManager.getDefaultSharedPreferences(context)
                                .getString(
                                        context.getString(R.string.key_pref_download_wikia_images),
                                        context.getString(R.string.default_download_wikia_images)
                                )
                ))
            return Integer.parseInt(context.getString(R.string.download_wikia_images_not_over_1024px_value));
        else {
            Point size = getScreenSize();
            return Math.max(size.x, size.y);
        }
    }

    public static boolean isWebpPreferred() {
        Context context = SonakoReaderApp.getContext();
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(
                        context.getString(R.string.key_pref_download_webp),
                        context.getResources().getBoolean(R.bool.default_download_webp)
                );
    }
}
