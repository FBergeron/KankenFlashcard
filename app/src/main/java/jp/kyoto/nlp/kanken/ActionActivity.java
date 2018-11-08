package jp.kyoto.nlp.kanken;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class ActionActivity extends BaseActionActivity {

    protected void onStart() {
        super.onStart();
        appl.playBackgroundMusic();
    }

    protected void initActionBar() {
        super.initActionBar();

        appl.initializePlaybackController();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        actionBarMenu = menu;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.action_bar_writing_problem_menu, menu);

        SharedPreferences sharedPref = getSharedPreferences(Util.PREFS_GENERAL, Context.MODE_PRIVATE);
        boolean isMusicEnabled = sharedPref.getBoolean("MusicEnabled", true);
        appl.setBackgroundMusicEnabled(isMusicEnabled);
        actionBarMenu.getItem(0).setIcon(ResourcesCompat.getDrawable(getResources(), (isMusicEnabled ? R.drawable.music_on : R.drawable.music_off), null));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menuToggleMusic) {
            SharedPreferences sharedPref = getSharedPreferences(Util.PREFS_GENERAL, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            if (appl.isBackgroundMusicEnabled()) {
                actionBarMenu.getItem(0).setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.music_off, null));
                editor.putBoolean("MusicEnabled", false);
                // Use a separate thread to prevent slowing the GUI.
                actionBarMenu.getItem(0).setEnabled(false);
                new ToggleMusicTask().execute(Boolean.FALSE);
            }
            else {
                actionBarMenu.getItem(0).setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.music_on, null));
                editor.putBoolean("MusicEnabled", true);
                // Use a separate thread to prevent slowing the GUI.
                new ToggleMusicTask().execute(Boolean.TRUE);
            }
            editor.apply();
        }
        return super.onOptionsItemSelected(item);
    }

    private class ToggleMusicTask extends AsyncTask {

        protected Object doInBackground(Object... objs) {
            Boolean isMusicEnabled = (Boolean)objs[0];
            appl.setBackgroundMusicEnabled(isMusicEnabled.booleanValue());
            return isMusicEnabled;
        }

        protected void onPostExecute(final Object obj) {
            actionBarMenu.getItem(0).setEnabled(true);
        }

    }

    private Menu actionBarMenu;

    private final static String tag = "ActionActivity";

    private KankenApplication appl = KankenApplication.getInstance();

}
