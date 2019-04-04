package perfectstrong.sonako.sonakoreader.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.adapter.MainActivityPagerAdapter;
import perfectstrong.sonako.sonakoreader.fragments.BiblioFragment;
import perfectstrong.sonako.sonakoreader.fragments.FavoriteLNsFragment;
import perfectstrong.sonako.sonakoreader.fragments.HistoryFragment;
import perfectstrong.sonako.sonakoreader.fragments.LNShowcaseFragment;
import perfectstrong.sonako.sonakoreader.fragments.PageDownloadFragment;

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
        adapter.addFragment(new BiblioFragment(), getString(R.string.page_biblio));
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
        fab.setOnClickListener(l -> adapter.getItem(viewPager.getCurrentItem()).showFilterDialog());
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
}
