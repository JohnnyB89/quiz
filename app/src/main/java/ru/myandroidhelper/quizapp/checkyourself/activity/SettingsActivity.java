package ru.myandroidhelper.quizapp.checkyourself.activity;

import android.os.Bundle;
import android.view.MenuItem;

import ru.myandroidhelper.quizapp.checkyourself.R;
import ru.myandroidhelper.quizapp.checkyourself.fragments.SettingsFragment;
import ru.myandroidhelper.quizapp.checkyourself.utilities.ActivityUtilities;


public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_settings);

        // replace linear layout by preference screen
        getFragmentManager().beginTransaction().replace(R.id.content, new SettingsFragment()).commit();

        initToolbar(true);
        setToolbarTitle(getString(R.string.settings));
        enableUpButton();
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ActivityUtilities.getInstance().invokeNewActivity(SettingsActivity.this, MainActivity.class, true);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityUtilities.getInstance().invokeNewActivity(SettingsActivity.this, MainActivity.class, true);
    }
}