package com.tangobravo.webviewmessagebenchmark;

import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebMessageCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private WebMessageCompat startMessage;
    private WebMessageCompat smallMessage;
    private WebMessageCompat largeMessage;
    private WebMessageCompat endMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= 19)
            WebView.setWebContentsDebuggingEnabled(true);

        webView = (WebView)findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);

        try {
            String html = readWholeAssetFile("test.html", "utf-8");
            webView.loadDataWithBaseURL("https://example.chromium.org", html,
                    "text/html", "utf-8", null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        startMessage = new WebMessageCompat("start");
        StringBuilder sb = new StringBuilder();
        sb.setLength(50);
        smallMessage = new WebMessageCompat(sb.toString());
        sb.setLength(500000);
        largeMessage = new WebMessageCompat(sb.toString());
        endMessage = new WebMessageCompat("end");
    }

    public void postMessages(View view) {
        if(!WebViewFeature.isFeatureSupported(WebViewFeature.POST_WEB_MESSAGE)) return;

        Log.i("WebViewMessages", "Post messages");
        Uri targetOrigin = Uri.fromParts("https", "example.chromium.org", null);

        long startTime = System.nanoTime();

        WebViewCompat.postWebMessage(webView, startMessage, targetOrigin);
        for(int i = 0; i < 10; ++i)
            WebViewCompat.postWebMessage(webView, smallMessage, targetOrigin);
        for(int i = 0; i < 10; ++i)
            WebViewCompat.postWebMessage(webView, largeMessage, targetOrigin);
        WebViewCompat.postWebMessage(webView, endMessage, targetOrigin);

        long postTime = System.nanoTime() - startTime;

        Log.i("WebViewMessages",
                String.format("Post messages finished in %f ms", postTime / 1000000.0));
    }

    private String readWholeAssetFile(String path, String encoding) throws IOException {
        InputStream inputStream = getAssets().open(path);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byte buffer[] = new byte[8192];
        int bytesRead = inputStream.read(buffer);
        while(bytesRead > 0) {
            byteStream.write(buffer, 0, bytesRead);
            bytesRead = inputStream.read(buffer);
        }

        return byteStream.toString(encoding);
    }

}
