package perfectstrong.sonako.sonakoreader.component;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import perfectstrong.sonako.sonakoreader.activity.PageReadingActivity;

public class PageReadingWebView extends LollipopFixedWebView {

    private PageReadingActivity.PageReadingWebViewClient client;
    private OnScrollCallback onScrollCallback;

    public PageReadingWebView(Context context) {
        super(context);
    }

    public PageReadingWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PageReadingWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PageReadingWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        onScrollCallback.execute(l, t, oldl, oldt);
    }

    public void setOnScrollCallback(OnScrollCallback onScrollCallback) {
        this.onScrollCallback = onScrollCallback;
    }

    public interface OnScrollCallback {
        void execute(int l, int t, int oldl, int oldt);
    }
}
