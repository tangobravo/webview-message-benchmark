package com.tangobravo.webviewmessagebenchmark;

import android.net.Uri;
import android.util.Log;

import androidx.webkit.WebMessageCompat;
import androidx.webkit.WebMessagePortCompat;


public class BinaryMessageCallback extends WebMessagePortCompat.WebMessageCallbackCompat {

    static {
        System.loadLibrary("native");
    }

    private WebMessagePortCompat nativePort;

    public BinaryMessageCallback(WebMessagePortCompat port) {
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
        String binaryString = fillRandomString(len);

        nativePort.postMessage(new WebMessageCompat(binaryString));
    }

    private native String fillRandomString(int length);

}
