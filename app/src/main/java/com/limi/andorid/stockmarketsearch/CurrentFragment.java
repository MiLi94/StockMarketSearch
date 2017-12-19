package com.limi.andorid.stockmarketsearch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class CurrentFragment extends Fragment implements View.OnClickListener {
    private static final int MY_SOCKET_TIMEOUT_MS = 7000;
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
//    private ArrayList<>
    ListView listView;
    Bundle savedState;
    private View rootView;
    private WebView mWebView;
    private String options;
    private String currentIndicator = "Price";
    private String selectIndicator = "Price";
    private boolean isClickable = false;
    private boolean isLoadedTable = false;
    private Button facebook_btn;
    private Button favourite_btn;
    private String symbol_after;
    private SharedPreferenceManager sharedPreferenceManager;
    private Stock currentStock;
    CallbackManager callbackManager;
    ShareDialog shareDialog;
    private ProgressBar mChartProgressbar;
    private ProgressBar mDetailProgressbar;
    TextView stockDetailErrorMessage;
    Button changeButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_current_detail, container, false);
            String symbol = getActivity().getIntent().getExtras().getString("symbol");
            Log.d("symbol", symbol);
            String[] symbols = symbol.split("-");
            symbol_after = symbols[0].trim();
            sharedPreferenceManager = new SharedPreferenceManager(getContext());
            listView = rootView.findViewById(R.id.table_list);
            facebook_btn = rootView.findViewById(R.id.facebook_btn);
            facebook_btn.setOnClickListener(this);
            favourite_btn = rootView.findViewById(R.id.fav_btn);
            favourite_btn.setOnClickListener(this);
            callbackManager = CallbackManager.Factory.create();
            mChartProgressbar = rootView.findViewById(R.id.chartProgressBar);
            mDetailProgressbar = rootView.findViewById(R.id.stockDetailProgressBar);
            mChartProgressbar.setVisibility(View.GONE);
            mDetailProgressbar.setVisibility(View.GONE);
            shareDialog = new ShareDialog(getActivity());
            stockDetailErrorMessage = rootView.findViewById(R.id.stockDetailErrorMessage);
            stockDetailErrorMessage.setVisibility(View.GONE);
            changeButton = rootView.findViewById(R.id.change_indicator);
            changeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentIndicator = selectIndicator;
                    loadWebView(symbol_after, currentIndicator);
                    changeButton.setEnabled(false);
                    changeButton.setTextColor(Color.GRAY);
                }
            });

//            ViewGroup header = (ViewGroup) inflater.inflate(R.layout.current_header_layout, listView,
//                    false);
//            ViewGroup footer = (ViewGroup) inflater.inflate(R.layout.current_footer_layout, listView,
//                    false);
//            listView.addHeaderView(header, null, false);
//            listView.addFooterView(footer, null, false);
//            TableAdapter tableAdapter = new TableAdapter(getContext(), R.layout.stock_detail_list_layout, new ArrayList<Table>());
//            listView.setAdapter(tableAdapter);
            final Spinner spinner = (Spinner) rootView.findViewById(R.id.indicator_spinner);
            String[] indicators = getResources().getStringArray(R.array.indicators);
            final SpinnerAdapter<String> indicatorArrayAdapter = new SpinnerAdapter<String>(
                    getContext(), R.layout.spinner_item, Arrays.asList(indicators)) {
                @Override
                public boolean isEnabled(int position) {
                    return true;
                }

                @Override
                public View getDropDownView(int position, View convertView,
                                            ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView tv = (TextView) view;
                    if (position == mSelectedIndex) {
                        // Set the disable item text color
                        tv.setTextColor(Color.GRAY);
                    } else {
                        tv.setTextColor(Color.BLACK);
                    }
                    return view;
                }
            };

            indicatorArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
            spinner.setAdapter(indicatorArrayAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    String selected_value = adapterView.getItemAtPosition(i).toString().toLowerCase();
                    if (!currentIndicator.equalsIgnoreCase(selected_value)) {
                        selectIndicator = selected_value;
                        Log.d("currentIdi", selectIndicator);
                        isClickable = true;
                        changeButton.setTextColor(Color.BLACK);
                        changeButton.setEnabled(true);
                    } else {
                        selectIndicator = currentIndicator;
                        isClickable = false;
                        changeButton.setTextColor(Color.GRAY);
                        changeButton.setEnabled(false);
                    }
                    indicatorArrayAdapter.setSelection(i);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            currentIndicator = "price";
            mWebView = rootView.findViewById(R.id.webview);

            initFavouriteView();
            requestForTableList(symbol_after);
            loadWebView(symbol_after, currentIndicator);
        }
        return rootView;
    }

    private void initFavouriteView() {
        if (sharedPreferenceManager.isFavourite(symbol_after)) {
            favourite_btn.setBackground(getResources().getDrawable(R.drawable.filled));
        } else {
            favourite_btn.setBackground(getResources().getDrawable(R.drawable.empty));
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView(String symbol, final String currentIndicator) {
        mWebView.stopLoading();
        mChartProgressbar.setVisibility(ProgressBar.VISIBLE);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.addJavascriptInterface(new IJavascriptHandler(), "cpjs");

        final String symbolH = symbol.replaceAll("\\\\", "\\\\\\\\");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
//                mWebView.setVisibility(View.GONE);
                mChartProgressbar.setVisibility(ProgressBar.VISIBLE);
                Log.d("progreebar", String.valueOf(mChartProgressbar.getVisibility()));

            }

            @Override
            public void onPageFinished(WebView view, String url) {
//                mWebView.setVisibility(View.VISIBLE);
                super.onPageFinished(view, url);
                mWebView.evaluateJavascript("javascript:generate_chart('" + symbolH + "','" + currentIndicator + "')", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {

                    }
                });
            }
        });
        mWebView.loadUrl("file:///android_asset/mimi.html");
    }

    private void requestForTableList(String s) {
        mDetailProgressbar.setVisibility(View.VISIBLE);
        String paras = "?symbol=" + s;
        if (s.matches("^[a-zA-z]+$")) {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, AppURLs.TABLE + paras, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    JSONObject jObj = null;
                    try {
                        ArrayList<Table> table_itmes = new ArrayList<>();
                        Log.d("response from table", response);
                        jObj = new JSONObject(response);
                        int error = jObj.getInt("error");

                        if (error == 0) {
                            JSONObject jsonArray = jObj.getJSONObject("data");
                            String symbol = null;
                            double price = 0;
                            double change = 0;
                            double change_percent = 0;
                            Iterator<?> keys = jsonArray.keys();
                            while (keys.hasNext()) {
                                String key = (String) keys.next();
                                String content = jsonArray.getString(key);
                                Table table = new Table(key, content);
                                table_itmes.add(table);
                                if (key.equals("Stock Ticker Symbol")) {
                                    symbol = content;
                                } else if (key.equals("Last Price")) {
                                    price = Double.parseDouble(content);
                                } else if (key.equals("Change (Change Percent)")) {
                                    String[] temp = content.split(" ");
                                    change = Double.parseDouble(temp[0]);
                                    String s_change_percent = temp[1].substring(1, temp[1].length() - 2);
                                    change_percent = Double.parseDouble(s_change_percent);
                                }
                            }
                            isLoadedTable = true;
                            currentStock = new Stock(symbol, price, change, change_percent, System.currentTimeMillis());
                            mDetailProgressbar.setVisibility(View.GONE);
                            setUpTable(table_itmes);


                        } else {
                            setUpTable(table_itmes);
                            mDetailProgressbar.setVisibility(View.GONE);
                            stockDetailErrorMessage.setVisibility(View.VISIBLE);

                        }


                    } catch (Exception e) {
                        setUpTable(new ArrayList<Table>());
                        mDetailProgressbar.setVisibility(View.GONE);
                        stockDetailErrorMessage.setVisibility(View.VISIBLE);

                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    setUpTable(new ArrayList<Table>());
                    mDetailProgressbar.setVisibility(View.GONE);
                    stockDetailErrorMessage.setVisibility(View.VISIBLE);

                }
            }) {


            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    MY_SOCKET_TIMEOUT_MS,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            AppController.getInstance().addToRequestQueue(stringRequest, "Table");
        } else {
            setUpTable(new ArrayList<Table>());
            mDetailProgressbar.setVisibility(View.GONE);
            stockDetailErrorMessage.setVisibility(View.VISIBLE);

        }

    }

    private void setUpTable(ArrayList<Table> table_items) {
        TableAdapter tableAdapter = new TableAdapter(getContext(), R.layout.stock_detail_list_layout, table_items);
        listView.setAdapter(tableAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d("Fragment 1", "onAttach");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("Fragment 1", "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("Fragment 1", "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Fragment 1", "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("Fragment 1", "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("Fragment 1", "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("Fragment 1", "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Fragment 1", "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("Fragment 1", "onDetach");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.facebook_btn:
                shareFacebook();
                break;
            case R.id.fav_btn:
                changeFavourite();
                break;
        }
    }


    private void changeFavourite() {
        if (isLoadedTable) {
            if (sharedPreferenceManager.isFavourite(symbol_after)) {
                favourite_btn.setBackground(getResources().getDrawable(R.drawable.empty));
                sharedPreferenceManager.removeFavourite(symbol_after);
            } else {
                favourite_btn.setBackground(getResources().getDrawable(R.drawable.filled));
                Gson gson = new Gson();
                String jsonInString = gson.toJson(currentStock);
                sharedPreferenceManager.setFavourite(currentStock.getSymbol(), jsonInString);
            }
        } else {
            Toast.makeText(getContext(), "Table data is not loaded yet, please try again later", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareFacebook() {
        if (options != null) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppURLs.FACEBOOK, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    ShareLinkContent content = new ShareLinkContent.Builder()
                            .setContentUrl(Uri.parse(AppURLs.FACEBOOK + response))
                            .build();
                    shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                        @Override
                        public void onSuccess(Sharer.Result result) {
                            Toast.makeText(getContext(), "Success Posted", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancel() {
                            Toast.makeText(getContext(), "User Cancelled", Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onError(FacebookException error) {
                            Toast.makeText(getContext(), "Error when posting", Toast.LENGTH_SHORT).show();

                        }
                    });
                    shareDialog.show(content);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("async", "true");
                    params.put("type", "jpeg");
                    params.put("options", options);
                    return params;
                }
            };
            AppController.getInstance().addToRequestQueue(stringRequest, "Facebook");


        } else {
            Toast.makeText(getContext(), "Chart is not loaded yet, Please wait", Toast.LENGTH_SHORT).show();
        }
    }

    final class IJavascriptHandler {
        IJavascriptHandler() {
        }

        // This annotation is required in Jelly Bean and later:
        @JavascriptInterface
        public void sendToAndroid(String text) {
            // this is called from JS with passed value
            options = text;
            Log.d("From JS", text);
//            Toast.makeText(getContext(), options, Toast.LENGTH_SHORT).show();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mChartProgressbar.getVisibility() != View.GONE) {
                        mChartProgressbar.setVisibility(View.GONE);
                    }
                }
            });
        }

        @JavascriptInterface
        public void showError() {
            // this is called from JS with passed value
            Log.e("Chart Error", "Chart Error");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mChartProgressbar.getVisibility() != View.GONE) {
                        mChartProgressbar.setVisibility(View.GONE);
                    }
                }
            });
        }

    }
}


