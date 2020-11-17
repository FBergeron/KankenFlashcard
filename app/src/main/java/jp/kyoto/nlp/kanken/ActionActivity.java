package jp.kyoto.nlp.kanken;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class ActionActivity extends BaseActionActivity {

    protected void onStart() {
        super.onStart();
    }

    protected void initActionBar() {
        super.initActionBar();
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
        return super.onOptionsItemSelected(item);
    }

    private Menu actionBarMenu;

    private final static String tag = "ActionActivity";

    private KankenApplication appl = KankenApplication.getInstance();

}
