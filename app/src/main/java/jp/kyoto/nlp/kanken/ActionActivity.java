package jp.kyoto.nlp.kanken;

import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class ActionActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionBar();
    }

    protected void onStart() {
        super.onStart();
        appl.playBackgroundMusic();
    }

    protected void initActionBar() {
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.app_name));
        actionBar.show();

        appl.initializePlaybackController();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        actionBarMenu = menu;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.action_bar_writing_problem_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menuToggleMusic) {
            if (appl.isBackgroundMusicEnabled()) {
                actionBarMenu.getItem(0).setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.music_off, null));
                // Use a separate thread to prevent slowing the GUI.
                Thread t = new Thread(
                    new Runnable() {
                        public void run() {
                            appl.setBackgroundMusicEnabled(false);
                        }
                    }
                );
                t.start();
            }
            else {
                actionBarMenu.getItem(0).setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.music_on, null));
                // Use a separate thread to prevent slowing the GUI.
                Thread t = new Thread(
                    new Runnable() {
                        public void run() {
                            appl.setBackgroundMusicEnabled(true);
                        }
                    }
                );
                t.start();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private ActionBar actionBar;
    private Menu actionBarMenu;

    private final static String tag = "ActionActivity";

    private KankenApplication appl = KankenApplication.getInstance();

}
