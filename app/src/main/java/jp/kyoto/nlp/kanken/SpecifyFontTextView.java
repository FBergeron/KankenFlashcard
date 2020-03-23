package jp.kyoto.nlp.kanken;

import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

public class SpecifyFontTextView extends AppCompatTextView {
    public SpecifyFontTextView(Context context) {
        super(context);
        setFont();
    }

    public SpecifyFontTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setFont();
    }

    public SpecifyFontTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFont();
    }

    private void setFont() {
        Typeface typeface;
        try {
            typeface = Typeface.createFromAsset(getContext().getAssets(), "IwaGTxtProN-Bd.otf");
        }
        catch(RuntimeException rte) {
            typeface = Typeface.createFromAsset(getContext().getAssets(), "gyate-luminescence.otf");
        }
        setTypeface(typeface);
    }
}
