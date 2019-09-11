package jp.kyoto.nlp.kanken;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class GraphicResultsHistoryFragment extends Fragment {

    public GraphicResultsHistoryFragment() {
    }

    public static GraphicResultsHistoryFragment newInstance(String param1, String param2) {
        GraphicResultsHistoryFragment fragment = new GraphicResultsHistoryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_graphic_results_history, container, false);

        chart = view.findViewById(R.id.chart);

        radioButtonLastWeek = view.findViewById(R.id.radioButtonLastWeek);
        radioButtonLastMonth = view.findViewById(R.id.radioButtonLastMonth);

        radioGroupPeriod = view.findViewById(R.id.radioGroupPeriod);
        radioGroupPeriod.setOnCheckedChangeListener(
            new RadioGroup.OnCheckedChangeListener() {
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    int selectedId = radioGroupPeriod.getCheckedRadioButtonId();
                    updateHistoryChart();
                }
            }
        );

        updateHistoryChart();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void updateHistoryChart() {
        if (jsonResultsHistory != null)
            rebuildHistoryChart();
        else {
            URL getResultHistoryUrl;
            try {
                getResultHistoryUrl = new URL(appl.getServerBaseUrl() + getResultHistoryReqPath);

                progressDialog = new ProgressDialog(getContext());
                progressDialog.setMessage(getResources().getString(R.string.label_fetching_data));
                progressDialog.setCancelable(false);
                progressDialog.show();

                new FetchResultHistoryTask().execute(getResultHistoryUrl);
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (UnsupportedEncodingException e2) {
                e2.printStackTrace();
            } catch (IOException e3) {
                e3.printStackTrace();
            } catch (JSONException e4) {
                e4.printStackTrace();
            }
        }
    }

    private class FetchResultHistoryTask extends AsyncTask {

        protected Object doInBackground(Object... objs) {
            JSONObject jsonResultHistory = null;
            URL getResultHistoryUrl = (URL) objs[0];
            try {
                HttpURLConnection con = (HttpURLConnection) getResultHistoryUrl.openConnection();
                con.setRequestProperty("Accept", "application/json");
                String cookie = appl.getSessionCookie();
                if (cookie != null)
                    con.setRequestProperty("Cookie", cookie);
                con.setRequestMethod("GET");
                con.connect();

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                jsonResultHistory = jsonResponse;
            } catch (IOException e) {
                e.printStackTrace();
                this.exception = e;
            } catch (JSONException e2) {
                e2.printStackTrace();
                this.exception = e2;
            }

            return jsonResultHistory;
        }

        protected void onPostExecute(final Object obj) {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            if (exception != null || obj == null) {
                if (exception != null)
                    Log.e(tag, "An exception has occurred: " + exception);
                if (obj == null)
                    Log.e(tag, "Cannot retrieve problems.");

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getResources().getString(R.string.error_server_unreachable_title))
                        .setMessage(getResources().getString(R.string.error_server_unreachable_msg))
                        .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setCancelable(true)
                        .show();

                return;
            }

            Log.d(tag, "resultsHistory=" + obj);

            List<ResultsHistoryItem> resultsHistoryItems = new ArrayList<>();
            jsonResultsHistory = (JSONObject)obj;
            rebuildHistoryChart();
        }

        private Exception exception;

    }

    private void rebuildHistoryChart() {
        if (jsonResultsHistory.has("results_history")) {
            JSONArray jsonResults = null;
            try {
                jsonResults = (JSONArray)jsonResultsHistory.get("results_history");
            }
            catch(JSONException e) {
                e.printStackTrace();
            }
            if (jsonResults != null) {
                List<String> dateLabels = new ArrayList<String>();
                List<BarEntry> rightEntries = new ArrayList<BarEntry>();
                List<BarEntry> wrongEntries = new ArrayList<BarEntry>();

                Calendar now = Calendar.getInstance();
                String strDateTemp = dateFormatter.format(now.getTime());

                int maxValueCount = -1;
                Calendar periodStart = Calendar.getInstance();
                if (radioButtonLastWeek.isChecked()) {
                    periodStart.add(Calendar.DAY_OF_MONTH, -7);
                    maxValueCount = 7;
                }
                else if (radioButtonLastMonth.isChecked()) {
                    periodStart.add(Calendar.MONTH, -1);
                    maxValueCount = 31;
                }
                String strDateMin = dateFormatter.format(periodStart.getTime());

                String strDateResult = null;
                JSONObject result = null;
                Integer readingRights = Integer.valueOf(0);
                Integer readingWrongs = Integer.valueOf(0);
                Integer writingRights = Integer.valueOf(0);
                Integer writingWrongs = Integer.valueOf(0);
                int entryIndex = 0;
                int resultIndex = 0;
                while (strDateTemp.compareTo(strDateMin) > 0) {
                    if (jsonResults.length() > resultIndex && (result == null || strDateResult == null || strDateResult.compareTo(strDateTemp) > 0)) {
                        try {
                            result = (JSONObject)jsonResults.get(resultIndex);
                            for (Iterator<String> keys = result.keys(); keys.hasNext(); ) {
                                strDateResult = keys.next();

                                JSONObject dailyResult = (JSONObject)result.get(strDateResult);
                                if (dailyResult.has("reading")) {
                                    JSONObject readingData = (JSONObject)dailyResult.get("reading");
                                    readingRights = (Integer)readingData.get("rights");
                                    readingWrongs = (Integer)readingData.get("wrongs");
                                }
                                else {
                                    readingRights = Integer.valueOf(0);
                                    readingWrongs = Integer.valueOf(0);
                                }
                                if (dailyResult.has("writing")) {
                                    JSONObject writingData = (JSONObject)dailyResult.get("writing");
                                    writingRights = (Integer)writingData.get("rights");
                                    writingWrongs = (Integer)writingData.get("wrongs");
                                }
                                else {
                                    writingRights = Integer.valueOf(0);
                                    writingWrongs = Integer.valueOf(0);
                                }

                                // There always is 1 key.
                                break;
                            }
                            resultIndex++;
                        }
                        catch(JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    dateLabels.add(strDateTemp);
                    if (strDateResult != null && strDateResult.compareTo(strDateTemp) == 0) {
                        rightEntries.add(new BarEntry(entryIndex, readingRights.intValue() + writingRights.intValue()));
                        wrongEntries.add(new BarEntry(entryIndex, readingWrongs.intValue() + writingWrongs.intValue()));
                    }
                    else {
                        rightEntries.add(new BarEntry(entryIndex, 0));
                        wrongEntries.add(new BarEntry(entryIndex, 0));
                    }

                    entryIndex++;
                    now.add(Calendar.DAY_OF_MONTH, -1);
                    strDateTemp = dateFormatter.format(now.getTime());
                }

                ValueFormatter formatter = new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, AxisBase axis) {
                        int val = (int)value;
                        return (value < 0 || value >= dateLabels.size()  ? "" : dateLabels.get(val));
                    }
                };

                String strRightDataSetLabel = getResources().getString(R.string.results_history_graphic_view_legend_rights);
                BarDataSet rightDataset = new BarDataSet(rightEntries, strRightDataSetLabel);
                rightDataset.setDrawValues(false);
                rightDataset.setColor(Color.rgb(255, 0, 0));

                String strWrongDataSetLabel = getResources().getString(R.string.results_history_graphic_view_legend_wrongs);
                BarDataSet wrongDataset = new BarDataSet(wrongEntries, strWrongDataSetLabel);
                wrongDataset.setDrawValues(false);
                wrongDataset.setColor(Color.rgb(0, 136, 255));

                datasets = new ArrayList<IBarDataSet>();
                datasets.add(rightDataset);
                datasets.add(wrongDataset);

                XAxis xAxis = chart.getXAxis();
                xAxis.setGranularity(1f);
                xAxis.setDrawGridLines(false);
                xAxis.setCenterAxisLabels(true);
                xAxis.setValueFormatter(formatter);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setLabelRotationAngle(-45);

                float groupSpace = 0.06f;
                float barSpace = 0.02f;
                float barWidth = 0.45f;

                BarData barData = new BarData(datasets);
                barData.setBarWidth(barWidth);
                chart.getDescription().setEnabled(false);
                chart.setTouchEnabled(false);
                YAxis leftAxis = chart.getAxisLeft();
                leftAxis.setAxisMinimum(0f);
                leftAxis.setGranularity(1f);
                leftAxis.setDrawGridLines(false);
                chart.getAxisRight().setEnabled(false);
                chart.setData(barData);
                chart.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));

                Legend legend = chart.getLegend();
                legend.setWordWrapEnabled(true);
                legend.setTextSize(14);
                legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
                legend.setDrawInside(false);
                legend.setForm(Legend.LegendForm.SQUARE);

                chart.getXAxis().setAxisMinimum(0);

                chart.groupBars(0f, groupSpace, barSpace);
                chart.setFitBars(true);
                chart.setVisibleXRangeMaximum(maxValueCount);
                chart.setVisibleXRangeMinimum(maxValueCount);
                chart.getXAxis().setLabelCount(maxValueCount);
                chart.invalidate();

            }
        }
    }

    private JSONObject jsonResultsHistory = null;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    private BarChart chart;
    private RadioGroup radioGroupPeriod;
    private RadioButton radioButtonLastWeek;
    private RadioButton radioButtonLastMonth;
    private List<IBarDataSet> datasets;

    private ProgressDialog progressDialog;

    private KankenApplication appl = KankenApplication.getInstance();

    private static final String getResultHistoryReqPath = "/cgi-bin/get_results_history.cgi";

    private static final String tag = "GraphicResultsHistoryFragment";

}

