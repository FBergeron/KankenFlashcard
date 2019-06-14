package jp.kyoto.nlp.kanken;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.CheckBox;

@SuppressLint("AppCompatCustomView")
public class SpecifyFontCheckBox extends CheckBox {
    public SpecifyFontCheckBox(Context context) {
        super(context);
        setFont();
    }

    public SpecifyFontCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont();
    }

    public SpecifyFontCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
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
