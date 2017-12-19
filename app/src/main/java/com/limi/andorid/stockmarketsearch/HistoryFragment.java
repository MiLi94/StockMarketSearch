package com.limi.andorid.stockmarketsearch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;


public class HistoryFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */

    private View mRootView;
    private WebView mWebView;
    private ProgressBar historyProgressBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_history_detail, container, false);
            mWebView = mRootView.findViewById(R.id.historyView);
            String symbol = getActivity().getIntent().getExtras().getString("symbol");
            Log.d("symbol", symbol);
            String[] symbols = symbol.split("-");
            final String symbol_after = symbols[0].trim();
            historyProgressBar = mRootView.findViewById(R.id.historyProgressBar);
            loadWebView(symbol_after);
        }
        return mRootView;
    }

    private void loadWebView(String symbol) {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.addJavascriptInterface(new IJavascriptHandler(), "cpjs");
        historyProgressBar.setVisibility(View.VISIBLE);

        mWebView.loadUrl("file:///android_asset/history.html");
        final String symbolH = symbol.replaceAll("\\\\", "\\\\\\\\");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mWebView.evaluateJavascript("javascript:generate_chart('" + symbolH + "')", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {

                    }
                });
            }
        });
    }

    final class IJavascriptHandler {
        IJavascriptHandler() {
        }

        // This annotation is required in Jelly Bean and later:
        @JavascriptInterface
        public void sendToAndroid() {
            // this is called from JS with passed value
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (historyProgressBar.getVisibility() != View.GONE) {
                        historyProgressBar.setVisibility(View.GONE);
                    }
                }
            });
        }

    }


}


