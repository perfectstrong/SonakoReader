package perfectstrong.sonako.sonakoreader;

import android.os.Bundle;

import perfectstrong.sonako.sonakoreader.helper.PageLoader;

/**
 * Read and manipulate a lightnovel main page
 */
public class LNMainpageReadingActivity extends PageReadingActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new PageLoader(this, title).execute();
    }
}
