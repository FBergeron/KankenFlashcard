package jp.kyoto.nlp.kanken;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

public abstract class BaseActionActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionBar();
    }

    protected void onStart() {
        super.onStart();
    }

    protected void initActionBar() {
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    protected ActionBar actionBar;

    private final static String tag = "BaseActionActivity";

}

