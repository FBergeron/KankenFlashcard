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

        initResultsHistory();
        //initTest();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    List<String> xAxisValues = new ArrayList<>(Arrays.asList("Jan", "Feb", "March", "April", "May", "June","July", "August", "September", "October", "November", "December"));


    private void initTest() {
        int size = 12;
        List<BarEntry> incomeEntries = getIncomeEntries(size);
        List<BarEntry> expenseEntries = getExpenseEntries(size);
        datasets = new ArrayList<>();
        BarDataSet set1, set2;

        set1 = new BarDataSet(incomeEntries, "Income");
        set1.setColor(Color.rgb(65, 168, 121));
        set1.setValueTextColor(Color.rgb(55, 70, 73));
        set1.setValueTextSize(10f);

        set2 = new BarDataSet(expenseEntries, "Expense");
        set2.setColors(Color.rgb(241, 107, 72));
        set2.setValueTextColor(Color.rgb(55, 70, 73));
        set2.setValueTextSize(10f);

        datasets.add(set1);
        datasets.add(set2);

        BarData data = new BarData(datasets);
        chart.setData(data);
        chart.getAxisLeft().setAxisMinimum(0);

        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setAxisMinimum(0);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.setMaxVisibleValueCount(10);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);

        Legend l = chart.getLegend();
        l.setWordWrapEnabled(true);
        l.setTextSize(14);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.CIRCLE);

        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMaximum(getExpenseEntries(size).size());

        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisValues));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.setTypeface(Typeface.DEFAULT);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);

        setBarWidth(data, size);
        chart.invalidate();
    }

private List<BarEntry> getExpenseEntries(int size) {
    ArrayList<BarEntry> expenseEntries = new ArrayList<>();

    expenseEntries.add(new BarEntry(1,1710));
    expenseEntries.add(new BarEntry(2,2480));
    expenseEntries.add(new BarEntry(3,242));
    expenseEntries.add(new BarEntry(4,2409));
    expenseEntries.add(new BarEntry(5,8100));
    expenseEntries.add(new BarEntry(6,1200));
    expenseEntries.add(new BarEntry(7,6570));
    expenseEntries.add(new BarEntry(8,5455));
    expenseEntries.add(new BarEntry(9,15000));
    expenseEntries.add(new BarEntry(10,11340));
    expenseEntries.add(new BarEntry(11,9100));
    expenseEntries.add(new BarEntry(12,6300));
    return expenseEntries.subList(0, size);
}

private List<BarEntry> getIncomeEntries(int size) {
    ArrayList<BarEntry> incomeEntries = new ArrayList<>();

    incomeEntries.add(new BarEntry(1, 11300));
    incomeEntries.add(new BarEntry(2, 1390));
    incomeEntries.add(new BarEntry(3, 1190));
    incomeEntries.add(new BarEntry(4, 7200));
    incomeEntries.add(new BarEntry(5, 4790));
    incomeEntries.add(new BarEntry(6, 4500));
    incomeEntries.add(new BarEntry(7, 8000));
    incomeEntries.add(new BarEntry(8, 7034));
    incomeEntries.add(new BarEntry(9, 4307));
    incomeEntries.add(new BarEntry(10, 8762));
    incomeEntries.add(new BarEntry(11, 4355));
    incomeEntries.add(new BarEntry(12, 6000));
    return incomeEntries.subList(0, size);
}


    private void initResultsHistory() {
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
            JSONObject jsonResultsHistory = (JSONObject)obj;
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

                    for (int i = 0; i < jsonResults.length(); i++) {
                        try {
                            JSONObject result = (JSONObject)jsonResults.get(i);
                            Integer readingRights = new Integer(0);
                            Integer readingWrongs = new Integer(0);
                            Integer writingRights = new Integer(0);
                            Integer writingWrongs = new Integer(0);

                            String strDateKey = null;
                            for (Iterator<String> keys = result.keys(); keys.hasNext(); ) {
                                strDateKey = keys.next();
                                JSONObject dailyResult = (JSONObject)result.get(strDateKey);
                                if (dailyResult.has("reading")) {
                                    JSONObject readingData = (JSONObject)dailyResult.get("reading");
                                    readingRights = (Integer)readingData.get("rights");
                                    readingWrongs = (Integer)readingData.get("wrongs");
                                }
                                if (dailyResult.has("writing")) {
                                    JSONObject writingData = (JSONObject)dailyResult.get("writing");
                                    writingRights = (Integer)writingData.get("rights");
                                    writingWrongs = (Integer)writingData.get("wrongs");
                                }
                            }

                            System.out.println("i="+i+" strDateKey="+strDateKey+" r="+(readingRights.intValue()+writingRights.intValue())+
                                " w="+(readingWrongs.intValue()+writingWrongs.intValue()));
                            if (strDateKey != null) {
                                //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                                //try {
                                //    Date date = formatter.parse(strDateKey);
                                    dateLabels.add(strDateKey);
                                    rightEntries.add(new BarEntry(i, readingRights.intValue() + writingRights.intValue()));
                                    wrongEntries.add(new BarEntry(i, readingWrongs.intValue() + writingWrongs.intValue()));
                                // }
                                // catch(ParseException e) {
                                //     e.printStackTrace();
                                // }
                            }
                        }
                        catch(JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    ValueFormatter formatter = new ValueFormatter() {
                        @Override
                        public String getAxisLabel(float value, AxisBase axis) {
                            int val = (int)value;
                            System.out.println("getAxisLabel value="+value+" val="+val+" axis="+axis);
                            return (value < 0 || value >= dateLabels.size()  ? "" : dateLabels.get(val));
                        }
                    };

                    BarDataSet rightDataset = new BarDataSet(rightEntries, "Rights");
                    int rightDatasetColor = ContextCompat.getColor(getContext(), R.color.colorAccent);
                    rightDataset.setColor(rightDatasetColor);
                    // //setValueTextColor

                    BarDataSet wrongDataset = new BarDataSet(wrongEntries, "Wrongs");

                    datasets = new ArrayList<IBarDataSet>();
                    datasets.add(rightDataset);
                    datasets.add(wrongDataset);

                    XAxis xAxis = chart.getXAxis();
                    xAxis.setGranularity(1f);
                    xAxis.setCenterAxisLabels(true);
                    xAxis.setValueFormatter(formatter);
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setLabelRotationAngle(-45);

                    float groupSpace = 0.06f;
                    float barSpace = 0.02f;
                    float barWidth = 0.45f;

                    BarData barData = new BarData(datasets);
                    barData.setBarWidth(barWidth);
                    chart.setDescription(null);
                    chart.setData(barData);
                    chart.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));

                    // Legend legend = chart.getLegend();



                    chart.getXAxis().setAxisMinimum(0);
                    chart.getXAxis().setAxisMaximum(chart.getBarData().getGroupWidth(groupSpace, barSpace) * 10);

                    chart.groupBars(0f, groupSpace, barSpace);
                    //chart.setVisibleXRangeMaximum(5);
                    chart.setFitBars(true);
                    chart.invalidate();

                }
            }
        }

        private Exception exception;

    }

    private void setBarWidth(BarData barData, int size) {
        if (datasets.size() > 1) {
            float barSpace = 0.02f;
            float groupSpace = 0.3f;
            defaultBarWidth = (1 - groupSpace) / datasets.size() - barSpace;
            if (defaultBarWidth >= 0)
                barData.setBarWidth(defaultBarWidth);
            else
                Toast.makeText(getContext(), "Default Barwdith " + defaultBarWidth, Toast.LENGTH_SHORT).show();
            int groupCount = getExpenseEntries(size).size();
            System.out.println("groupCount="+groupCount);
            if (groupCount != -1) {
                chart.getXAxis().setAxisMinimum(0);
                chart.getXAxis().setAxisMaximum(0 + chart.getBarData().getGroupWidth(groupSpace, barSpace) * groupCount);
                chart.getXAxis().setCenterAxisLabels(true);
            }
            else
                Toast.makeText(getContext(), "no of bar groups is " + groupCount, Toast.LENGTH_SHORT).show();

            chart.groupBars(0, groupSpace, barSpace); // perform the "explicit" grouping
            chart.invalidate();
        }
    }

    private BarChart chart;
    private List<IBarDataSet> datasets;
    private float defaultBarWidth = -1;

    private ProgressDialog progressDialog;

    private KankenApplication appl = KankenApplication.getInstance();

    private static final String getResultHistoryReqPath = "/cgi-bin/get_results_history.cgi";

    private static final String tag = "GraphicResultsHistoryFragment";

}

