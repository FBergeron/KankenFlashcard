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

import java.util.ArrayList;
import java.util.List;

public class SummaryListViewAdapter extends BaseAdapter {

    private class ViewHolder {
        private TextView level;
        private TextView topic;
        private TextView number;
        private TextView answer;
        private TextView rightAnswer;
        private TextView familiarity;
        private TextView statement;
        private ImageView result;
        private String link = "";

        public ViewHolder(View view) {
            answer = view.findViewById(R.id.textViewAnswer);
            rightAnswer = view.findViewById(R.id.textViewRightAnswer);
            statement = view.findViewById(R.id.statement);
            level = view.findViewById(R.id.textViewLevel);
            topic = view.findViewById(R.id.textViewTopic);
            number = view.findViewById(R.id.textViewNumber);
            familiarity = view.findViewById(R.id.textViewFamiliarity);
            result = view.findViewById(R.id.imageViewResult);
            ImageButton imageButton = view.findViewById(R.id.imageButtonReadNews);
            imageButton.setOnClickListener(v -> {
                onClickReadNews(link);
            });
        }

        public void bind(SummaryItem item) {
            level.setText(item.getLevel());
            topic.setText(item.getTopic());
            number.setText(item.getNumber());
            answer.setText(item.getAnswer());
            rightAnswer.setText(item.getRightAnswer());
            familiarity.setText(item.getFamiliarity());
            link = item.getLink();

            int resId = item.isRight() ? R.drawable.icon_maru : R.drawable.icon_batu;
            result.setImageResource(resId);

            statement.setText(Html.fromHtml(item.getStatement().replace("[", "<u><font color=\"red\">").replace("]", "</font></u>")));
        }

    }

    private Context context;
    private LayoutInflater inflater;
    private List<SummaryItem> items;

    public SummaryListViewAdapter(Context context, LayoutInflater inflater) {
        this.context = context;
        this.inflater = inflater;
        items = new ArrayList<>();
    }

    public void setItems(List<SummaryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public SummaryItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.view_summary_item, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        SummaryItem item = items.get(position);
        holder.bind(item);
        return convertView;
    }

    private void onClickReadNews(String link) {
        if (link != null && !link.isEmpty()) {
            Intent httpIntent = new Intent(Intent.ACTION_VIEW);
            httpIntent.setData(Uri.parse(link));
            context.startActivity(httpIntent);
        }
    }
}
