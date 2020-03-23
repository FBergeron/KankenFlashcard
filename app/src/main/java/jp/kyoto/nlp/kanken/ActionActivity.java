package jp.kyoto.nlp.kanken;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.core.content.res.ResourcesCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class ActionActivity extends BaseActionActivity {

    protected void onStart() {
        super.onStart();
//        appl.playBackgroundMusic();
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
        boolean isMusicEnabled = sharedPref.getBoolean(Util.PREF_KEY_MUSIC_ENABLED, true);
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
                editor.putBoolean(Util.PREF_KEY_MUSIC_ENABLED, false);
                // Use a separate thread to prevent slowing the GUI.
                actionBarMenu.getItem(0).setEnabled(false);
                new ToggleMusicTask().execute(Boolean.FALSE);
            }
            else {
                actionBarMenu.getItem(0).setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.music_on, null));
                editor.putBoolean(Util.PREF_KEY_MUSIC_ENABLED, true);
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
