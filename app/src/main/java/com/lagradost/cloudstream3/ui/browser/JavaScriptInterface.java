package com.lagradost.cloudstream3.ui.browser;

import android.util.Log;
import android.webkit.JavascriptInterface;


public class JavaScriptInterface {
    ICallback<String> iCallback;

    public JavaScriptInterface(ICallback<String> iCallback) {
        this.iCallback = iCallback;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void processHTML(String html) {
        iCallback.onCallback(html);
    }
}
