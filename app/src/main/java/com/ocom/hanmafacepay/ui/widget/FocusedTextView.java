package com.ocom.hanmafacepay.ui.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * 跑马灯效果TextView
 */
public class FocusedTextView extends AppCompatTextView {
    public FocusedTextView(Context context) {
        super(context);
        init();
    }

    public FocusedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FocusedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
