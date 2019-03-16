package perfectstrong.sonako.sonakoreader;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;
import perfectstrong.sonako.sonakoreader.database.LightNovel;
import perfectstrong.sonako.sonakoreader.fragments.FavoriteLNsFragment;
import perfectstrong.sonako.sonakoreader.fragments.HistoryFragment;
import perfectstrong.sonako.sonakoreader.fragments.LNFilterable;
import perfectstrong.sonako.sonakoreader.fragments.LNShowcaseFragment;
import perfectstrong.sonako.sonakoreader.fragments.PageDownloadFragment;

public class MainActivity extends AppCompatActivity {

    private static final String LN_SHOW_CASE = "Danh sách";
    private static final String FAVORITE_LNs = "Yêu thích";
    private static final String HISTORY = "Lịch sử";
    private static final String DOWNLOAD_ACTIVITIES = "Download";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setContentView(R.layout.main_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);

        ViewPager viewPager = findViewById(R.id.viewpager);
        MainActivityPagerAdapter adapter = new MainActivityPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new LNShowcaseFragment(), LN_SHOW_CASE);
        adapter.addFragment(new FavoriteLNsFragment(), FAVORITE_LNs);
        adapter.addFragment(new HistoryFragment(), HISTORY);
        adapter.addFragment(PageDownloadFragment.getInstance(), DOWNLOAD_ACTIVITIES);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(l -> {
            switch (adapter.getPageTitle(viewPager.getCurrentItem())) {
                case LN_SHOW_CASE:
                case FAVORITE_LNs:
                    showLNTitleFilterDialog((LNFilterable) adapter.getItem(viewPager.getCurrentItem()));
                    break;
                case DOWNLOAD_ACTIVITIES:
                    break;
                case HISTORY:
                    break;
                default:
                    break;
            }
        });

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLNTitleFilterDialog(LNFilterable fragment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        AlertDialog alertDialog = builder.setTitle(R.string.filter).create();
        View view = View.inflate(this, R.layout.ln_title_filter_dialog, null);
        alertDialog.setView(view);

        // Hint for genres
        ArrayAdapter<String> genresHintAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                LightNovel.ProjectGenre.ALL
        );
        MultiAutoCompleteTextView genresTextView = view.findViewById(R.id.genres_selection);
        assert genresTextView != null;
        genresTextView.setAdapter(genresHintAdapter);
        genresTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        // Hint for type
        ArrayAdapter<String> typesAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                LightNovel.ProjectType.CHOICES
        );
        typesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner typesSpinner = view.findViewById(R.id.type_selection);
        typesSpinner.setAdapter(typesAdapter);

        // Hint for status
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                LightNovel.ProjectStatus.CHOICES
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner statusSpinner = view.findViewById(R.id.status_selection);
        statusSpinner.setAdapter(statusAdapter);

        // Action
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setButton(
                Dialog.BUTTON_NEGATIVE,
                getString(R.string.filter_no),
                (Message) null
        );
        alertDialog.setButton(
                Dialog.BUTTON_POSITIVE,
                getString(R.string.filter_ok),
                (dialog, which) -> {
                    // Filter
                    String keyword = ((TextView) view.findViewById(R.id.keyword_selection))
                            .getText().toString();
                    String type = typesSpinner.getSelectedItem().toString();
                    String status = statusSpinner.getSelectedItem().toString();
                    String[] genres = genresTextView.getText().toString().split("\\s*,\\s*");
                    if (genres.length == 1 && genres[0].equals("")) genres = new String[]{};
                    fragment.filterLNList(
                            keyword,
                            type,
                            status,
                            genres
                    );
                }
        );
        alertDialog.setButton(
                Dialog.BUTTON_NEUTRAL,
                getString(R.string.filter_reset),
                (dialog, which) -> fragment.showAll()
        );

        // Show
        alertDialog.show();
    }
}
