package jp.kyoto.nlp.kanken;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ErrorsHistoryListViewAdapter extends BaseAdapter {

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

        public void bind(ErrorsHistoryItem item) {
            if (item.isShowDate()) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                textViewDate.setText(formatter.format(item.getDate()));
            }
            else
                textViewDate.setText(null);
            textViewProblem.setText(item.getProblem());
            textViewUserAnswer.setText(item.getUserAnswer());
            textViewRightAnswer.setText(item.getRightAnswer());
        }

    }

    private Context context;
    private LayoutInflater inflater;
    private List<ErrorsHistoryItem> items;

    public ErrorsHistoryListViewAdapter(Context context, LayoutInflater inflater) {
        this.context = context;
        this.inflater = inflater;
        items = new ArrayList<>();
    }

    public void setItems(List<ErrorsHistoryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ErrorsHistoryItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.view_errors_history_item, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        ErrorsHistoryItem item = items.get(position);
        holder.bind(item);
        return convertView;
    }
}

