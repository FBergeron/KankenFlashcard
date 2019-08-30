package jp.kyoto.nlp.kanken;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ResultsHistoryListViewAdapter extends BaseAdapter {

    private class ViewHolder {
        private TextView textViewDate;
        private TextView textViewReadingRights;
        private TextView textViewReadingWrongs;
        private TextView textViewWritingRights;
        private TextView textViewWritingWrongs;
        private TextView textViewTotalRights;
        private TextView textViewTotalWrongs;

        public ViewHolder(View view) {
            textViewDate = view.findViewById(R.id.textViewDate);
            textViewReadingRights = view.findViewById(R.id.textViewReadingRights);
            textViewReadingWrongs = view.findViewById(R.id.textViewReadingWrongs);
            textViewWritingRights = view.findViewById(R.id.textViewWritingRights);
            textViewWritingWrongs = view.findViewById(R.id.textViewWritingWrongs);
            textViewTotalRights = view.findViewById(R.id.textViewTotalRights);
            textViewTotalWrongs = view.findViewById(R.id.textViewTotalWrongs);
        }

        public void bind(ResultsHistoryItem item) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            textViewDate.setText(formatter.format(item.getDate()));
            textViewReadingRights.setText(item.getReadingRights() + "");
            textViewReadingWrongs.setText(item.getReadingWrongs() + "");
            textViewWritingRights.setText(item.getWritingRights() + "");
            textViewWritingWrongs.setText(item.getWritingWrongs() + "");
            textViewTotalRights.setText(item.getTotalRights() + "");
            textViewTotalWrongs.setText(item.getTotalWrongs() + "");
        }

    }

    private Context context;
    private LayoutInflater inflater;
    private List<ResultsHistoryItem> items;

    public ResultsHistoryListViewAdapter(Context context, LayoutInflater inflater) {
        this.context = context;
        this.inflater = inflater;
        items = new ArrayList<>();
    }

    public void setItems(List<ResultsHistoryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ResultsHistoryItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.view_results_history_item, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        ResultsHistoryItem item = items.get(position);
        holder.bind(item);
        return convertView;
    }
}
