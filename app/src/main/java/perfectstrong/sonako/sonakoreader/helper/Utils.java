package perfectstrong.sonako.sonakoreader.helper;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.SonakoReaderApp;
import perfectstrong.sonako.sonakoreader.activity.MainActivity;
import perfectstrong.sonako.sonakoreader.activity.PageReadingActivity;
import perfectstrong.sonako.sonakoreader.database.CachePage;
import perfectstrong.sonako.sonakoreader.service.PageDownloadService;

@SuppressWarnings("WeakerAccess")
public class Utils {

    private static final List<String> NAMESPACE_PREFIXES = Arrays.asList("Blog:", "Blog_talk:", "Board:", "Board_Thread:", "Category:", "Category_talk:", "File:", "File_talk:", "Help:", "Help_talk:", "Image:", "Media:", "MediaWiki:", "MediaWiki_talk:", "Message_Wall:", "Message_Wall_Greeting:", "Module:", "Module_talk:", "Project:", "Project_talk:", "Related_Videos:", "Special:", "Talk:", "Template:", "Template_talk:", "Thread:", "Topic:", "User:", "User_talk:", "User_blog:", "User_blog_comment:", "Phương tiện:", "Đặc biệt:", "Thảo luận:", "Thành viên:", "Thảo luận Thành viên:", "Sonako Light Novel Wiki:", "Thảo luận Sonako Light Novel Wiki:", "Tập tin:", "Thảo luận Tập tin:", "MediaWiki:", "Thảo luận MediaWiki:", "Bản mẫu:", "Thảo luận Bản mẫu:", "Trợ giúp:", "Thảo luận Trợ giúp:", "Thể loại:", "Thảo luận Thể loại:", "Diễn đàn:", "Thảo luận Diễn đàn:", "Blog thành viên:", "Bình luận blog thành viên:", "Blog:", "Thảo luận Blog:", "Mô đun:", "Thảo luận Mô đun:", "Tường tin nhắn:", "Luồng:", "Thông điệp Tường tin nhắn:", "Bảng:", "Luồng bảng:", "Vấn đề:");

    @SuppressWarnings("SameReturnValue")
    public static String getSaveDir() {
//        return PreferenceManager.getDefaultSharedPreferences(SonakoReaderApp.getContext())
//                .getString("SAVE_LOCATION", Config.DEFAULT_SAVE_LOCATION);
        return Config.DEFAULT_SAVE_LOCATION;
    }

    public static String getSaveDirForTag(String tag) {
        return getSaveDir() + sanitize(tag) + "/";
    }

    public static String getCurrentSkin() {
        Context context = SonakoReaderApp.getContext();
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.key_pref_skin),
                        context.getResources().getString(R.string.default_skin)
                );
    }

    public static String getCurrentFont() {
        Context context = SonakoReaderApp.getContext();
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(
                        context.getString(R.string.key_reading_font),
                        null
                );
    }

    public static String getFilepath(String title, String tag) {
        return "file://"
                + Utils.getSaveDirForTag(tag)
                + Utils.sanitize(title)
                + ".html";
    }

    public static String decode(String encodedString) {
        return Uri.decode(encodedString);
    }

    public static final String REPLACEMENT_CHAR = " ";

    /**
     * @param tag retrieved from wikia
     * @return valid path for directory
     */
    public static String sanitize(String tag) {
        if (tag == null) return "";
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

    public static File getCachedTextFile(CachePage page) {
        return getTextFile(page.getTitle(), page.getTag());
    }

    public static boolean isNotCached(String title, String tag) {
        if (tag == null) return true;
        return !getTextFile(title, tag).exists();
    }

    public static File getTextFile(String title, String tag) {
        return new File(
                getSaveDirForTag(tag),
                sanitize(title) + ".html"
        );
    }

    public static boolean isMainpage(String href) {
        for (String s : NAMESPACE_PREFIXES) {
            if (href.startsWith(s)) {
                return false;
            }
        }
        return true;
    }

    private static final String[] AUTO_DOWNLOAD_CHOICES = {"Luôn tự động tải nếu chưa có trong bộ nhớ"};

    /**
     * Open page reader or download if not cached yet
     *
     * @param title   of new page to open
     * @param tag     tag of series
     * @param action  See {@link PageDownloadService.ACTION}
     * @param context where to execute
     */
    public static void openOrDownload(String title,
                                      String tag,
                                      PageDownloadService.ACTION action,
                                      Context context) {
        if (title == null) {
            Toast.makeText(
                    context,
                    R.string.undefined_title,
                    Toast.LENGTH_LONG
            )
                    .show();
            return;
        }
        if (action == null) {
            // Read or first download
            if (tag == null) {
                // Undefined tag
                // We try to check in save dir
                tag = getCachedTagFor(title);
                // If tag is still null, no cached has been saved
            }
            if (isNotCached(title, tag)) {
                // If auto download, no need to create dialog
                if (PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(
                                context.getString(R.string.key_pref_download_noncached_pages),
                                context.getResources().getBoolean(R.bool.default_download_noncached_pages)
                        ))
                    startDownloadTask(context, title, tag, null);
                else {
                    // Request sent from internal activity
                    // Demand before downloading
                    final boolean[] autoDownload = {
                            context.getResources().getBoolean(R.bool.default_download_noncached_pages)
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    String finalTag = tag;
                    builder.setTitle(context.getString(R.string.wanna_download))
                            .setMultiChoiceItems(
                                    AUTO_DOWNLOAD_CHOICES,
                                    null,
                                    (dialog, which, isChecked) -> autoDownload[0] = which == 0 && isChecked
                            )
                            .setPositiveButton(
                                    context.getString(R.string.ok),
                                    (dialog, which) -> {
                                        if (autoDownload[0]) {
                                            PreferenceManager
                                                    .getDefaultSharedPreferences(context)
                                                    .edit()
                                                    .putBoolean(
                                                            context.getString(R.string.key_pref_download_noncached_pages),
                                                            true)
                                                    .apply();
                                        }
                                        startDownloadTask(context, title, finalTag, null);
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
        } else
            // Special action
            startDownloadTask(context, title, tag, action);
    }

    public static String getCachedTagFor(String title) {
        File saveDir = new File(getSaveDir());
        // Get all subfolders
        File[] dirs = saveDir.listFiles(File::isDirectory);
        if (dirs != null && dirs.length > 0) {
            for (File dir : dirs) {
                File[] htmlFiles = dir.listFiles(pathname -> pathname.getName().endsWith(".html") && pathname.getName().contains(sanitize(title)));
                if (Objects.requireNonNull(htmlFiles).length > 0)
                    return dir.getName();
            }
        }
        return null;
    }

    /**
     * Simply open reading activity
     *
     * @param context where to execute
     * @param title   of new page
     * @param tag     tag of series
     */
    public static void startReadingActivity(Context context,
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
    public static void startDownloadTask(Context context,
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
     * @return <tt>true</tt> if is not allowed to download over cellular connection
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

    public static boolean isSkinHoly(Context context) {
        return context.getString(R.string.skin_holy_value)
                .equals(
                        PreferenceManager.getDefaultSharedPreferences(context)
                                .getString(
                                        context.getString(R.string.key_pref_skin),
                                        context.getString(R.string.default_skin)
                                )
                );
    }

    public static void updateTheme(Context context) {
        if (Utils.isSkinHoly(context)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    public static void goHome(Context context) {
        Intent i = new Intent(context, MainActivity.class);
        context.startActivity(i);
    }

    public static void restartApp(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void viewExternalLink(Context context, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    /**
     * slide the view from none to display
     *
     * @param view      regular view
     * @param animation id
     */
    public static void slideIn(View view, int animation) {
        if (view.getVisibility() == View.VISIBLE) return;
        Animation animIn = AnimationUtils.loadAnimation(view.getContext(), animation);
        animIn.setFillBefore(true);
        animIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(animIn);
    }

    /**
     * Slide the view from its current position to none
     *
     * @param view      regular view
     * @param animation id
     */
    public static void slideOut(View view, int animation) {
        if (view.getVisibility() == View.GONE) return;
        Animation animOut = AnimationUtils.loadAnimation(view.getContext(), animation);
        animOut.setFillBefore(true);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(animOut);
    }

    public static void hideImagesFromGallery(Context context) {
        File appFolder = new File(getSaveDir());
        if (!appFolder.exists())
            //noinspection ResultOfMethodCallIgnored
            appFolder.mkdirs();
        File noMediaFile = new File((getSaveDir() + ".nomedia"));
        if (!noMediaFile.exists()) {
            try {
                if (noMediaFile.createNewFile()) {
                    Toast.makeText(
                            context,
                            R.string.has_hidden_img,
                            Toast.LENGTH_SHORT
                    )
                            .show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(
                        context,
                        R.string.cannot_hidden_img,
                        Toast.LENGTH_SHORT
                )
                        .show();
            }
        }
    }

    /**
     * @return unique id which will not be repeated before 68 years
     */
    public static int getUniqueNotificationId() {
        return (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
    }

    /**
     * @param context where to read
     * @param uri     not null
     * @return <tt>null</tt> if failed
     */
    public static String getFileName(Context context, Uri uri) {
        String result;
        //if uri is content
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    //local filesystem
                    int index = cursor.getColumnIndex("_data");
                    if (index == -1)
                        //google drive
                        index = cursor.getColumnIndex("_display_name");
                    result = cursor.getString(index);
                    if (result != null)
                        uri = Uri.parse(result);
                    else
                        return null;
                }
            }
        }
        result = uri.getPath();
        if (result == null) return null;
        //get filename + ext of path
        int cut = result.lastIndexOf('/');
        if (cut != -1)
            result = result.substring(cut + 1);
        return result;
    }
}