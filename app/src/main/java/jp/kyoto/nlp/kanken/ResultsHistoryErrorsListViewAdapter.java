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

public class ResultsHistoryErrorsListViewAdapter extends BaseAdapter {

    private class ViewHolder {
        private TextView textViewDate;
        private TextView textViewProblem;
        private TextView textViewUserAnswer;
        private TextView textViewRightAnswer;

        public ViewHolder(View view) {
            textViewDate = view.findViewById(R.id.textViewDate);
            textViewProblem = view.findViewById(R.id.textViewProblem);
            textViewUserAnswer = view.findViewById(R.id.textViewUserAnswer);
            textViewRightAnswer = view.findViewById(R.id.textViewRightAnswer);
        }

        public void bind(ResultsHistoryErrorItem item) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            if (item.getUserAnswer() == null) {
                textViewDate.setText(formatter.format(item.getDate()));
                textViewProblem.setText(null);
                textViewUserAnswer.setText(null);
                textViewRightAnswer.setText(null);
            }
            else {
                textViewDate.setText(null);
                textViewProblem.setText(item.getProblem());
                textViewUserAnswer.setText(item.getUserAnswer());
                textViewRightAnswer.setText(item.getRightAnswer());
            }
        }

    }

    private Context context;
    private LayoutInflater inflater;
    private List<ResultsHistoryErrorItem> items;

    public ResultsHistoryErrorsListViewAdapter(Context context, LayoutInflater inflater) {
        this.context = context;
        this.inflater = inflater;
        items = new ArrayList<>();
    }

    public void setItems(List<ResultsHistoryErrorItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ResultsHistoryErrorItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.view_results_history_error_item, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        ResultsHistoryErrorItem item = items.get(position);
        holder.bind(item);
        return convertView;
    }
}

