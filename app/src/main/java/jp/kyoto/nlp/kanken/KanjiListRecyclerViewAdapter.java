package jp.kyoto.nlp.kanken;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class KanjiListRecyclerViewAdapter extends RecyclerView.Adapter<KanjiListRecyclerViewAdapter.ViewHolder> {

    KanjiListRecyclerViewAdapter(Context context, String[] data) {
        this.inflater = LayoutInflater.from(context);
        this.data = data;
        this.typeface = null;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recycler_view_kanji_input_right_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.kanjiTextView.setText(data[position]);
        if (typeface != null)
            holder.kanjiTextView.setTypeface(typeface);
    }

    @Override
    public int getItemCount() {
        return data.length;
    }

    public void clear() {
        final int size = data.length;
        data = new String[] {};
        notifyItemRangeRemoved(0, size);
    }

    public void setTypeface(Typeface typeface) {
        this.typeface = typeface;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView kanjiTextView;

        ViewHolder(View itemView) {
            super(itemView);
            kanjiTextView = itemView.findViewById(R.id.info_text);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onItemClick(view, getAdapterPosition());
        }
    }

    String getItem(int id) {
        return data[id];
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    private String[] data;
    private Typeface typeface;
    private LayoutInflater inflater;
    private ItemClickListener clickListener;

}
