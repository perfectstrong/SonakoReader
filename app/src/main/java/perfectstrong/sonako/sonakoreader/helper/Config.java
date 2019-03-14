package perfectstrong.sonako.sonakoreader.helper;

import android.os.Environment;

import java.util.Arrays;
import java.util.List;

public class Config {
    public static final String SERVER_NAME = "sonako.wikia.com";
    public static final String WEBSITE = "https://" + SERVER_NAME;
    public static final String API_ENDPOINT = WEBSITE + "/api.php";
    public static final String OFFICIAL_PROJECTS_LIST = "Danh sách các project";
    public static final String APP_PREFIX = "perfectstrong.sonako.sonakoreader";
    public static final String APP_NAME = "Sonako Reader";
    public static final String EXTRA_ACTION = APP_PREFIX + ".action";
    public static final String EXTRA_TITLE = APP_PREFIX + ".title";
    public static final String EXTRA_TAG = APP_PREFIX + ".tag";
    public static final String[] AVAILABLE_SKINS = {"holy", "dark"};
    public static final String DEFAULT_SKIN = AVAILABLE_SKINS[0];
    public static final String SKIN_BASE = "common";
    public static final String DEFAULT_SAVE_LOCATION = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + APP_NAME + "/";
    public static final List<String> SUPPORTED_IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");
    public static final String PREF_AUTO_DOWNLOAD = APP_PREFIX + ".autoDownload";
}
