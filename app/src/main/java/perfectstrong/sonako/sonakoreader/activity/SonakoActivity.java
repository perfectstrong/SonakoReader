package perfectstrong.sonako.sonakoreader.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.helper.Utils;

public class SonakoActivity extends AppCompatActivity {
    private String skin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (skin == null)
            skin = Utils.getCurrentSkin();
        getDelegate().setLocalNightMode(
                skin.equals(getString(R.string.skin_holy_value)) ?
                        AppCompatDelegate.MODE_NIGHT_NO :
                        AppCompatDelegate.MODE_NIGHT_YES);
        getDelegate().applyDayNight();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!skin.equals(Utils.getCurrentSkin())) {
            // There is a change
            // Toggle
            skin = Utils.getCurrentSkin();
            getDelegate().setLocalNightMode(
                    skin.equals(getString(R.string.skin_holy_value)) ?
                            AppCompatDelegate.MODE_NIGHT_NO :
                            AppCompatDelegate.MODE_NIGHT_YES);
            getDelegate().applyDayNight();
            recreate();
        }
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }
}
