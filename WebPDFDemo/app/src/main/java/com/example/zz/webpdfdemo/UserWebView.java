package com.example.zz.webpdfdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * Created by 上官若枫 on 2016/12/23.
 */

public class UserWebView extends WebView {
    public UserWebView(Context context) {
        super(context);
    }

    public UserWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UserWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int getPageHeight() {
        return computeVerticalScrollRange();
    }

    public int getPageWidth() {
        return computeHorizontalScrollRange();
    }

}
