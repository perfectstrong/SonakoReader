package perfectstrong.sonako.sonakoreader;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;

import perfectstrong.sonako.sonakoreader.database.LightNovel;
import perfectstrong.sonako.sonakoreader.fragments.FavoriteLNsFragment;
import perfectstrong.sonako.sonakoreader.fragments.HistoryFragment;
import perfectstrong.sonako.sonakoreader.fragments.LNFilterable;
import perfectstrong.sonako.sonakoreader.fragments.LNShowcaseFragment;
import perfectstrong.sonako.sonakoreader.fragments.RecentActivitiesFragment;

public class MainActivity extends AppCompatActivity {

    private static final String LN_SHOW_CASE = "Danh sách";
    private static final String FAVORITE_LNs = "Yêu thích";
    private static final String RECENT_ACTIVITIES = "Hoạt động";
    private static final String HISTORY = "Lịch sử";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = findViewById(R.id.viewpager);
        MainActivityPagerAdapter adapter = new MainActivityPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new LNShowcaseFragment(), LN_SHOW_CASE);
        adapter.addFragment(new FavoriteLNsFragment(), FAVORITE_LNs);
        adapter.addFragment(new RecentActivitiesFragment(), RECENT_ACTIVITIES);
        adapter.addFragment(new HistoryFragment(), HISTORY);
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
                case RECENT_ACTIVITIES:
                    break;
                case HISTORY:
                    break;
                default:
                    break;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
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
                LightNovel.ProjectType.ALL
        );
        typesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner typesSpinner = view.findViewById(R.id.type_selection);
        typesSpinner.setAdapter(typesAdapter);

        // Hint for status
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                LightNovel.ProjectStatus.ALL
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
