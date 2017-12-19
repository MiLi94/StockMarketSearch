package com.limi.andorid.stockmarketsearch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by limi on 2017/11/20.
 */

public class NewsFragment extends Fragment {
    ListView listView;
    View rootView;
    TextView newsErrorMessage;

    public NewsFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_news, container, false);
            String symbol = getActivity().getIntent().getExtras().getString("symbol");
            Log.d("symbol", symbol);
            String[] symbols = symbol.split("-");
            String symbol_after = symbols[0].trim();
            listView = rootView.findViewById(R.id.news_list);
            newsErrorMessage = rootView.findViewById(R.id.newsErrorMessage);
            requestForNewsList(symbol_after);

        }
        return rootView;
    }

    private void requestForNewsList(String s) {


        String paras = "?symbol=" + s;
        if (s.matches("^[a-zA-z]+$")) {
            newsErrorMessage.setVisibility(View.GONE);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, AppURLs.NEWS + paras, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    JSONObject jObj = null;
                    try {
                        ArrayList<News> news_items = new ArrayList<>();
                        Log.d("response from table", response);
                        jObj = new JSONObject(response);
                        int error = jObj.getInt("error");
                        if (error == 0) {
                            JSONArray jsonArray = jObj.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject item = jsonArray.getJSONObject(i);
                                String title = item.getString("title");
                                String link = item.getString("link");
                                String author = item.getString("author");
                                String date = item.getString("date");
                                News news = new News(title, author, link, date);
                                news_items.add(news);
                            }
                            setUpNews(news_items);
                        } else {
                            newsErrorMessage.setVisibility(View.VISIBLE);
                            setUpNews(news_items);
                        }
                    } catch (JSONException e) {
                        setUpNews(new ArrayList<News>());
                        newsErrorMessage.setVisibility(View.VISIBLE);
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    setUpNews(new ArrayList<News>());
                    newsErrorMessage.setVisibility(View.VISIBLE);
                }
            }) {
            };
            AppController.getInstance().addToRequestQueue(stringRequest, "News");
        } else {
            setUpNews(new ArrayList<News>());
            newsErrorMessage.setVisibility(View.VISIBLE);
        }
    }

    private void setUpNews(final ArrayList<News> table_items) {
        NewsAdapter newsAdapter = new NewsAdapter(getContext(), R.layout.news_item_layout, table_items);
        listView.setAdapter(newsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String url = ((News) adapterView.getItemAtPosition(i)).getUrl();
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

}
