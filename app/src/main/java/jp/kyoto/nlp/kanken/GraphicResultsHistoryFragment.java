package jp.kyoto.nlp.kanken;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

}

