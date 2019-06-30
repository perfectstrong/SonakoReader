package perfectstrong.sonako.sonakoreader.component;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class PageReadingWebView extends LollipopFixedWebView {

    private OnScrollCallback onScrollCallback;
    private OnTouchCallback onTouchCallback;

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

    public interface OnTouchCallback {
        void execute(MotionEvent event);

    }

    public void setOnTouchCallback(OnTouchCallback onTouchCallback) {
        this.onTouchCallback = onTouchCallback;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        HitTestResult hitResult = getHitTestResult();
        if (HitTestResult.SRC_ANCHOR_TYPE == hitResult.getType()) {
            // An url
            // Do not execute callback
            return super.onTouchEvent(event);
        } else {
            onTouchCallback.execute(event);
            return super.onTouchEvent(event);
        }
    }
}
