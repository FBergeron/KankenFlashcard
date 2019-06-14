package jp.kyoto.nlp.kanken;

import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CustomWebViewClient extends WebViewClient {
    public interface OnPageFinshedListener {
        void onLoadUrl(WebView view, String url);
    }

    private OnPageFinshedListener onPageFinshedListener;

    public void setOnPageFinshedListener(OnPageFinshedListener listener) {
        onPageFinshedListener = listener;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (onPageFinshedListener != null) {
            onPageFinshedListener.onLoadUrl(view, url);
        }
    }
}
