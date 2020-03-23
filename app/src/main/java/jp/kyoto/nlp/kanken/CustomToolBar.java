package jp.kyoto.nlp.kanken;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class CustomToolBar extends Toolbar {
    public CustomToolBar(Context context) {
        this(context, null);
    }

    public CustomToolBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.actionBarStyle);
    }

    public CustomToolBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomToolBar, defStyleAttr, 0);

        View view = LayoutInflater.from(context).inflate(R.layout.view_tool_bar, this);
        ImageView imageView = view.findViewById(R.id.titleImage);
        Drawable titleImage = typedArray.getDrawable(R.styleable.CustomToolBar_title_image);
        imageView.setImageDrawable(titleImage);

    }
}
