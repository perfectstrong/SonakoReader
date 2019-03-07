package perfectstrong.sonako.sonakoreader.helper;

import android.os.Environment;
import android.preference.PreferenceManager;

import java.net.MalformedURLException;
import java.net.URL;

import perfectstrong.sonako.sonakoreader.MainActivity;

public class Config {
    public static final String SERVER_NAME = "sonako.wikia.com";
    public static final String API_ENDPOINT = "https://" + SERVER_NAME + "/api.php";
    public static final String OFFICIAL_PROJECTS_LIST = "Danh_sách_các_project";
    private static final String APP_PREFIX = "perfectstrong.sonako.sonakoreader";
    private static final String APP_NAME = "Sonako Reader";
    public static final String EXTRA_TITLE = APP_PREFIX + ".title";
    public static final String EXTRA_TAG = APP_PREFIX + ".tag";
    public static final String ASSETS_DIR = "file:///android_asset/";
    public static final String DEFAULT_SKIN = ASSETS_DIR + "mercury.css";
    public static final String DEFAULT_SAVE_LOCATION = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + APP_NAME + "/";
    public static String SAVE_LOCATION = PreferenceManager.getDefaultSharedPreferences(
            MainActivity.getContextOfApplication()).getString("SAVE_LOCATION", DEFAULT_SAVE_LOCATION);
    public static String SKIN = PreferenceManager.getDefaultSharedPreferences(
            MainActivity.getContextOfApplication()).getString("SKIN", DEFAULT_SKIN);

    public static String getSaveLocationForTag(String tag) {
        return SAVE_LOCATION + sanitize(tag) + "/";
    }

    private static final String REPLACEMENT_CHAR = "_";

    public static String sanitize(String tag) {
        return tag.replaceAll("[|?*<\":>+\\[\\]/']", REPLACEMENT_CHAR);
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
}
