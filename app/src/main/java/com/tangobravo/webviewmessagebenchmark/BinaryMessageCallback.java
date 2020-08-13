package com.tangobravo.webviewmessagebenchmark;

import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;

import androidx.webkit.WebMessageCompat;
import androidx.webkit.WebMessagePortCompat;
import androidx.webkit.WebViewCompat;


public class BinaryMessageCallback extends WebMessagePortCompat.WebMessageCallbackCompat {

    static {
        System.loadLibrary("native");
    }

    private WebView webView;
    private Uri targetOrigin;
    private WebMessagePortCompat nativePort;
    private WebMessageCompat webMessage;
    private int messageLen = 0;

    public BinaryMessageCallback(WebView view, Uri origin, WebMessagePortCompat port) {
        webView = view;
        targetOrigin = origin;
        nativePort = port;
    }

    public void onMessage(WebMessagePortCompat port, WebMessageCompat message) {
        Log.i("WebViewMessages", "Received message: " + message.getData());
        String data = message.getData();

        if(data != null && data.startsWith("app://")) {
            processAppMessage(data);
        }
    };

    private void processAppMessage(String message) {
        Uri messageUri = Uri.parse(message);
        String host = messageUri.getHost();
        if(host == null) return;
        if(!host.equals("data")) return;

        String lenStr = messageUri.getQueryParameter("len");
        if(lenStr == null) return;
        int len = Integer.parseInt(lenStr);

        boolean mainFrame = messageUri.getBooleanQueryParameter("mainFrame", false);

        long start = System.nanoTime();
        if(messageLen != len) {
            String binaryString = fillRandomString(len);
            webMessage = new WebMessageCompat(binaryString);
            messageLen = len;
        }
        long fillTime = System.nanoTime() - start;
        start = System.nanoTime();
        if(mainFrame) {
            WebViewCompat.postWebMessage(webView, webMessage, targetOrigin);
        } else {
            nativePort.postMessage(webMessage);
        }
        long postTime = System.nanoTime() - start;
        Log.i("WebMessageBenchmark", String.format("Filling string: %d us, Posting message: %d us", (fillTime / 1000), (postTime / 1000)));
    }

    private native String fillRandomString(int length);

}
