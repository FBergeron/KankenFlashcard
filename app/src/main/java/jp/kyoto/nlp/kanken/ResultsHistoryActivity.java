package jp.kyoto.nlp.kanken;

import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
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

        fragmentError.updateErrors();
    }

    public void showTextView(android.view.View view) {
        fragmentErrorView.setVisibility(View.GONE);
        fragmentGraphicView.setVisibility(View.GONE);
        fragmentTextView.setVisibility(View.VISIBLE);

        SharedPreferences sharedPref = getSharedPreferences(Util.PREFS_GENERAL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Util.PREF_KEY_HISTORY_VIEW, "Text");
        editor.apply();

        fragmentText.initResultsHistory();
    }

    public void showGraphicView(android.view.View view) {
        fragmentErrorView.setVisibility(View.GONE);
        fragmentGraphicView.setVisibility(View.VISIBLE);
        fragmentTextView.setVisibility(View.GONE);

        SharedPreferences sharedPref = getSharedPreferences(Util.PREFS_GENERAL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Util.PREF_KEY_HISTORY_VIEW, "Graphic");
        editor.apply();

        fragmentGraphic.updateHistoryChart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_history);

        fragmentErrorView = findViewById(R.id.fragmentErrorResultsHistory);
        fragmentGraphicView = findViewById(R.id.fragmentGraphicResultsHistory);
        fragmentTextView = findViewById(R.id.fragmentTextResultsHistory);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentText = (TextResultsHistoryFragment)fragmentManager.findFragmentById(R.id.fragmentTextResultsHistory);
        fragmentGraphic = (GraphicResultsHistoryFragment)fragmentManager.findFragmentById(R.id.fragmentGraphicResultsHistory);
        fragmentError = (ErrorsHistoryFragment)fragmentManager.findFragmentById(R.id.fragmentErrorResultsHistory);

        SharedPreferences sharedPref = getSharedPreferences(Util.PREFS_GENERAL, Context.MODE_PRIVATE);
        String prefView = sharedPref.getString(Util.PREF_KEY_HISTORY_VIEW, "Text");

        if ("Text".equals(prefView)) {
            fragmentErrorView.setVisibility(View.GONE);
            fragmentGraphicView.setVisibility(View.GONE);
            fragmentTextView.setVisibility(View.VISIBLE);

            RadioGroup radioGroupHistoryViewType = findViewById(R.id.radioGroupHistoryViewType);
            radioGroupHistoryViewType.check(R.id.buttonTextView);

            fragmentText.initResultsHistory();
        }
        else if ("Graphic".equals(prefView)) {
            fragmentErrorView.setVisibility(View.GONE);
            fragmentGraphicView.setVisibility(View.VISIBLE);
            fragmentTextView.setVisibility(View.GONE);

            RadioGroup radioGroupHistoryViewType = findViewById(R.id.radioGroupHistoryViewType);
            radioGroupHistoryViewType.check(R.id.buttonGraphicView);

            fragmentGraphic.updateHistoryChart();
        }
        else if ("Errors".equals(prefView)) {
            fragmentErrorView.setVisibility(View.VISIBLE);
            fragmentGraphicView.setVisibility(View.GONE);
            fragmentTextView.setVisibility(View.GONE);

            RadioGroup radioGroupHistoryViewType = findViewById(R.id.radioGroupHistoryViewType);
            radioGroupHistoryViewType.check(R.id.buttonErrorView);

            fragmentError.updateErrors();
        }
    }

    private void doLeaveResultsHistory() {
        finish();
    }

    private ResultsHistoryListViewAdapter listViewAdapter;

    private View fragmentErrorView;
    private View fragmentGraphicView;
    private View fragmentTextView;

    private TextResultsHistoryFragment fragmentText;
    private GraphicResultsHistoryFragment fragmentGraphic;
    private ErrorsHistoryFragment fragmentError;

    private ProgressDialog progressDialog;

    private KankenApplication appl = KankenApplication.getInstance();

    private static final String tag = "ResultsHistoryActivity";

}
