package perfectstrong.sonako.sonakoreader.helper;

import android.os.Environment;

import java.io.File;

import perfectstrong.sonako.sonakoreader.BuildConfig;

@SuppressWarnings("WeakerAccess")
public class Config {
    public static final String SERVER_NAME = "sonako.fandom.com";
    public static final String WEBSITE = "https://" + SERVER_NAME;
    public static final String API_ENDPOINT = WEBSITE + "/api.php";
    public static final String OFFICIAL_PROJECTS_LIST = "Danh sách các project";
    public static final String APP_PREFIX = BuildConfig.APPLICATION_ID;
    public static final String APP_NAME = "Sonako Reader";
    public static final String EXTRA_ACTION = APP_PREFIX + ".action";
    public static final String EXTRA_TITLE = APP_PREFIX + ".title";
    public static final String EXTRA_TAG = APP_PREFIX + ".tag";
    public static final String EXTRA_ID = APP_PREFIX + ".notification.id";
    public static final String SKIN_BASE = "common";
    public static final String DEFAULT_SAVE_LOCATION = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + APP_NAME + File.separator;
    public static final String USER_AGENT = String.format(
            "%s %s on Android OS",
            BuildConfig.APPLICATION_ID,
            BuildConfig.VERSION_NAME
    );
    public static final String CHANNEL_ID = APP_PREFIX + ".downloader";
}
