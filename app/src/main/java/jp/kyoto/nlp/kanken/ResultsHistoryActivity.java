package jp.kyoto.nlp.kanken;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

public class ResultsHistoryActivity extends ActionActivity {

    @Override
    public void onBackPressed() {
        doLeaveResultsHistory();
    }

    public void leaveResultsHistory(android.view.View view) {
        doLeaveResultsHistory();
    }

    public void showErrorView(android.view.View view) {
        fragmentErrorView.setVisibility(View.VISIBLE);
        fragmentGraphicView.setVisibility(View.GONE);
        fragmentTextView.setVisibility(View.GONE);

        SharedPreferences sharedPref = getSharedPreferences(Util.PREFS_GENERAL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Util.PREF_KEY_HISTORY_VIEW, "Errors");
        editor.apply();
    }

    public void showTextView(android.view.View view) {
        fragmentErrorView.setVisibility(View.GONE);
        fragmentGraphicView.setVisibility(View.GONE);
        fragmentTextView.setVisibility(View.VISIBLE);

        SharedPreferences sharedPref = getSharedPreferences(Util.PREFS_GENERAL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Util.PREF_KEY_HISTORY_VIEW, "Text");
        editor.apply();
    }

    public void showGraphicView(android.view.View view) {
        fragmentErrorView.setVisibility(View.GONE);
        fragmentGraphicView.setVisibility(View.VISIBLE);
        fragmentTextView.setVisibility(View.GONE);

        SharedPreferences sharedPref = getSharedPreferences(Util.PREFS_GENERAL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Util.PREF_KEY_HISTORY_VIEW, "Graphic");
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_history);

        fragmentErrorView = findViewById(R.id.fragmentErrorResultsHistory);
        fragmentGraphicView = findViewById(R.id.fragmentGraphicResultsHistory);
        fragmentTextView = findViewById(R.id.fragmentTextResultsHistory);

        SharedPreferences sharedPref = getSharedPreferences(Util.PREFS_GENERAL, Context.MODE_PRIVATE);
        String prefView = sharedPref.getString(Util.PREF_KEY_HISTORY_VIEW, "Text");

        if ("Text".equals(prefView)) {
            fragmentErrorView.setVisibility(View.GONE);
            fragmentGraphicView.setVisibility(View.GONE);
            fragmentTextView.setVisibility(View.VISIBLE);

            RadioGroup radioGroupHistoryViewType = findViewById(R.id.radioGroupHistoryViewType);
            radioGroupHistoryViewType.check(R.id.buttonTextView);
        }
        else if ("Graphic".equals(prefView)) {
            fragmentErrorView.setVisibility(View.GONE);
            fragmentGraphicView.setVisibility(View.VISIBLE);
            fragmentTextView.setVisibility(View.GONE);

            RadioGroup radioGroupHistoryViewType = findViewById(R.id.radioGroupHistoryViewType);
            radioGroupHistoryViewType.check(R.id.buttonGraphicView);
        }
        else if ("Errors".equals(prefView)) {
            fragmentErrorView.setVisibility(View.VISIBLE);
            fragmentGraphicView.setVisibility(View.GONE);
            fragmentTextView.setVisibility(View.GONE);

            RadioGroup radioGroupHistoryViewType = findViewById(R.id.radioGroupHistoryViewType);
            radioGroupHistoryViewType.check(R.id.buttonErrorView);
        }
    }

    private void doLeaveResultsHistory() {
        finish();
    }

    private ResultsHistoryListViewAdapter listViewAdapter;

    private View fragmentErrorView;
    private View fragmentGraphicView;
    private View fragmentTextView;

    private ProgressDialog progressDialog;

    private KankenApplication appl = KankenApplication.getInstance();

    private static final String tag = "ResultsHistoryActivity";

}
