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
import perfectstrong.sonako.sonakoreader.fragments.PageFilterable;

public class MainActivity extends SonakoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setContentView(R.layout.main_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);

        ViewPager viewPager = findViewById(R.id.viewpager);
        MainActivityPagerAdapter adapter = new MainActivityPagerAdapter(getSupportFragmentManager());
        // In order of @array/pref_first_page_values
        adapter.addFragment(new LNShowcaseFragment(), getString(R.string.page_ln_showcase));
        adapter.addFragment(new FavoriteLNsFragment(), getString(R.string.page_ln_favorites));
        adapter.addFragment(new HistoryFragment(), getString(R.string.page_history));
        adapter.addFragment(PageDownloadFragment.getInstance(), getString(R.string.page_download));
        viewPager.setAdapter(adapter);
        // Set first page
        String first = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.key_pref_first_page),
                        getResources().getString(R.string.default_first_page));
        assert first != null;
        viewPager.setCurrentItem(Integer.parseInt(first) - 1);

        TabLayout tabLayout = findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(l -> {
            String s = adapter.getPageTitle(viewPager.getCurrentItem());
            if (getString(R.string.page_ln_showcase).equals(s)
                    || getString(R.string.page_ln_favorites).equals(s)) {
                showLNTitleFilterDialog((LNFilterable) adapter.getItem(viewPager.getCurrentItem()));
            } else if (getString(R.string.page_history).equals(s)) {
                showPageFilterDialog((PageFilterable) adapter.getItem(viewPager.getCurrentItem()));
            }
            // No search on download fragment
        });
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
                getString(R.string.no),
                (Message) null
        );
        alertDialog.setButton(
                Dialog.BUTTON_POSITIVE,
                getString(R.string.ok),
                (dialog, which) -> {
                    // Filter
                    String keyword = ((TextView) view.findViewById(R.id.keyword_selection))
                            .getText().toString().trim();
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

    private void showPageFilterDialog(PageFilterable fragment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        AlertDialog alertDialog = builder.setTitle(R.string.filter).create();
        View view = View.inflate(this, R.layout.page_filter_dialog, null);
        alertDialog.setView(view);

        // Action
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setButton(
                Dialog.BUTTON_NEGATIVE,
                getString(R.string.no),
                (Message) null
        );
        alertDialog.setButton(
                Dialog.BUTTON_POSITIVE,
                getString(R.string.ok),
                (dialog, which) -> {
                    // Filter
                    String keyword = ((TextView) view.findViewById(R.id.keyword_selection))
                            .getText().toString().trim();
                    int daysLimit = getResources()
                            .getIntArray(R.array.history_date_limit_values)[
                            ((Spinner) view.findViewById(R.id.history_date_limit))
                                    .getSelectedItemPosition()
                            ];
                    fragment.filterPages(keyword, daysLimit);
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
