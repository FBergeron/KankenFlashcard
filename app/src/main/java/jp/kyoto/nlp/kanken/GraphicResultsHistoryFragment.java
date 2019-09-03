package jp.kyoto.nlp.kanken;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
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

        // LineChart chart = view.findViewById(R.id.chart);

        // final String[] dateLabels = {
        //         "2010-10-10", "2010-10-12", "2010-10-16", "2010-10-22", "2010-10-23",
        //         "2010-10-24", "2010-10-25", "2010-10-26", "2010-10-27", "2010-10-28"
        // };

        // ValueFormatter formatter = new ValueFormatter() {
        //     @Override
        //     public String getAxisLabel(float value, AxisBase axis) {
        //         return dateLabels[(int) value];
        //     }
        // };

        // List<Entry> entries = new ArrayList<Entry>();
        // entries.add(new Entry(0, 4));
        // entries.add(new Entry(1, 14));
        // entries.add(new Entry(2, 43));
        // entries.add(new Entry(3, 8));
        // entries.add(new Entry(4, 22));
        // entries.add(new Entry(5, 4));
        // entries.add(new Entry(6, 14));
        // entries.add(new Entry(7, 2));
        // entries.add(new Entry(8, 18));
        // entries.add(new Entry(9, 15));

        // List<Entry> entries2 = new ArrayList<Entry>();
        // entries2.add(new Entry(0, 24));
        // entries2.add(new Entry(1, 34));
        // entries2.add(new Entry(2, 3));
        // entries2.add(new Entry(3, 45));
        // entries2.add(new Entry(4, 12));
        // entries2.add(new Entry(5, 7));
        // entries2.add(new Entry(6, 14));
        // entries2.add(new Entry(7, 3));
        // entries2.add(new Entry(8, 10));
        // entries2.add(new Entry(9, 12));

        // LineDataSet dataset = new LineDataSet(entries, "Rights");
        // int datasetColor = ContextCompat.getColor(getContext(), R.color.colorAccent);
        // dataset.setColor(datasetColor);
        // //setValueTextColor

        // LineDataSet dataset2 = new LineDataSet(entries2, "Wrongs");

        // List<ILineDataSet> datasets = new ArrayList<ILineDataSet>();
        // datasets.add(dataset);
        // datasets.add(dataset2);

        // XAxis xAxis = chart.getXAxis();
        // xAxis.setGranularity(1f);
        // xAxis.setValueFormatter(formatter);
        // xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // LineData lineData = new LineData(datasets);
        // chart.setData(lineData);
        // chart.invalidate();

        BarChart chart = view.findViewById(R.id.chart);

        final String[] dateLabels = {
                "2010-10-10", "2010-10-12", "2010-10-16", "2010-10-22", "2010-10-23",
                "2010-10-24", "2010-10-25", "2010-10-26", "2010-10-27", "2010-10-28"
        };

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return dateLabels[(int) value];
            }
        };

        List<BarEntry> entries = new ArrayList<BarEntry>();
        entries.add(new BarEntry(0, 4));
        entries.add(new BarEntry(1, 14));
        entries.add(new BarEntry(2, 43));
        entries.add(new BarEntry(3, 8));
        entries.add(new BarEntry(4, 22));
        entries.add(new BarEntry(5, 4));
        entries.add(new BarEntry(6, 14));
        entries.add(new BarEntry(7, 2));
        entries.add(new BarEntry(8, 18));
        entries.add(new BarEntry(9, 15));

        List<BarEntry> entries2 = new ArrayList<BarEntry>();
        entries2.add(new BarEntry(0, 24));
        entries2.add(new BarEntry(1, 34));
        entries2.add(new BarEntry(2, 3));
        entries2.add(new BarEntry(3, 45));
        entries2.add(new BarEntry(4, 12));
        entries2.add(new BarEntry(5, 7));
        entries2.add(new BarEntry(6, 14));
        entries2.add(new BarEntry(7, 3));
        entries2.add(new BarEntry(8, 10));
        entries2.add(new BarEntry(9, 12));

        BarDataSet dataset = new BarDataSet(entries, "Rights");
        int datasetColor = ContextCompat.getColor(getContext(), R.color.colorAccent);
        dataset.setColor(datasetColor);
        //setValueTextColor

        BarDataSet dataset2 = new BarDataSet(entries2, "Wrongs");

        List<IBarDataSet> datasets = new ArrayList<IBarDataSet>();
        datasets.add(dataset);
        datasets.add(dataset2);

        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        BarData barData = new BarData(datasets);
        chart.setData(barData);
        chart.groupBars(0f, 0.10f, 0.04f);
        chart.setVisibleXRangeMaximum(5);
        chart.invalidate();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

}

