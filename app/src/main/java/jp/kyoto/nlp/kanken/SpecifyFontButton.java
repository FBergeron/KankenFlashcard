package jp.kyoto.nlp.kanken;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

@SuppressLint("AppCompatCustomView")
public class SpecifyFontButton extends Button {
    public SpecifyFontButton(Context context) {
        super(context);
        setFont();
    }

    public SpecifyFontButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont();
    }

    public SpecifyFontButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFont();
    }

    public SpecifyFontButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
