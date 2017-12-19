package com.limi.andorid.stockmarketsearch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener {
    AutoCompleteTextView autoCompleteTextView;
    private SharedPreferenceManager sharedPreferenceManager;
    ArrayList<String> autoCompleteList;
    ArrayList<Stock> stocks;
    ListView favouriteList;
    private Timer timer = new Timer();
    private final long DELAY = 300; // milliseconds
    TextView get_quote_textView;
    TextView clearButtonTextView;
    private int mInterval = 5000; // 5 seconds by default, can be changed later
    private Handler mHandler;
    Switch mSwitch;
    private String TAG = MainActivity.class.getSimpleName();
    private int lastPress = 0;
    private Spinner sort_by;
    private Spinner order;
    private boolean isAscending = true;
    String sort_by_key = "sort_by";
    private Button manual_refresh_button;
    //    private HashMap<String, Stock> stringStockHashMap;
    //    private boolean delState = false;
    private ProgressBar mLoadingIndicator;
    private ProgressBar mFavouriteListLoadingIndicator;

    Toast validationToast;

    String[] sort_by_item = new String[]{
            "Sort by",
            "Default",
            "Symbol",
            "Price",
            "Change",
            "Change Percent"
    };
    String[] order_by_item = new String[]{"Order",
            "Ascending", "Descending"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stocks = new ArrayList<>();
        setContentView(R.layout.activity_main);
        sharedPreferenceManager = new SharedPreferenceManager(getApplicationContext());
        favouriteList = findViewById(R.id.favourite_list);
        mFavouriteListLoadingIndicator = findViewById(R.id.favouriteListProgressBar);
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.addTextChangedListener(this);
        get_quote_textView = findViewById(R.id.get_quote);
        get_quote_textView.setOnClickListener(this);
        clearButtonTextView = findViewById(R.id.clear_btn);
        clearButtonTextView.setOnClickListener(this);
        mLoadingIndicator = findViewById(R.id.autoCompleteProgressBar);
        mLoadingIndicator.setVisibility(View.GONE);
        sort_by = findViewById(R.id.sort_by);
        order = findViewById(R.id.order);
        mHandler = new Handler();
        mSwitch = findViewById(R.id.switch_auto_refresh);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    startRepeatingTask();
                } else {
                    stopRepeatingTask();
                }
            }
        });
        manual_refresh_button = findViewById(R.id.manual_refresh_button);
        manual_refresh_button.setOnClickListener(this);

        List<String> sortByItemsList = new ArrayList<>(Arrays.asList(sort_by_item));
        List<String> orderByItemsList = new ArrayList<>(Arrays.asList(order_by_item));

        // Initializing an ArrayAdapter
        final SpinnerAdapter<String> sortBySpinnerArrayAdapter = new SpinnerAdapter<String>(
                this, R.layout.spinner_item, sortByItemsList);

        final SpinnerAdapter<String> orderByArrayAdapter = new SpinnerAdapter<String>(
                this, R.layout.spinner_item, orderByItemsList);

        sortBySpinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        sort_by.setAdapter(sortBySpinnerArrayAdapter);

        orderByArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        order.setAdapter(orderByArrayAdapter);


        sort_by.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sort_by_key = adapterView.getItemAtPosition(i).toString();
                Log.d("sort_by", sort_by_key);
                if (sort_by_key != null) {
                    switch (sort_by_key) {
                        case "Default":
                            Collections.sort(stocks, new Comparator<Stock>() {
                                @Override
                                public int compare(Stock t0, Stock t1) {
                                    if (isAscending) {
                                        return t0.getTime() > t1.getTime() ? 1 : -1;

                                    } else {
                                        return t1.getTime() > t0.getTime() ? 1 : -1;

                                    }
                                }
                            });
                            break;
                        case "Symbol":
                            Collections.sort(stocks, new Comparator<Stock>() {
                                @Override
                                public int compare(Stock t0, Stock t1) {
                                    if (isAscending) {
                                        return t0.getSymbol().compareTo(t1.getSymbol());

                                    } else {
                                        return t1.getSymbol().compareTo(t0.getSymbol());

                                    }
                                }
                            });
                            break;
                        case "Price":
                            Collections.sort(stocks, new Comparator<Stock>() {
                                @Override
                                public int compare(Stock t0, Stock t1) {
                                    if (isAscending) {
                                        return (t0.getPrice() > t1.getPrice()) ? 1 : -1;

                                    } else {
                                        return (t1.getPrice() > t0.getPrice()) ? 1 : -1;

                                    }
                                }
                            });
                            break;
                        case "Change":
                            Collections.sort(stocks, new Comparator<Stock>() {
                                @Override
                                public int compare(Stock t0, Stock t1) {
                                    if (isAscending) {

                                        return (t0.getChange() > t1.getChange()) ? 1 : -1;
                                    } else {
                                        return (t1.getChange() > t0.getChange()) ? 1 : -1;

                                    }
                                }
                            });
                            break;
                        case "Change Percent":
                            Log.d("hh", "hehe");
                            Collections.sort(stocks, new Comparator<Stock>() {
                                @Override
                                public int compare(Stock t0, Stock t1) {
                                    if (isAscending) {
                                        return (t0.getChange_percent() > t1.getChange_percent()) ? 1 : -1;
                                    } else {
                                        return (t1.getChange_percent() > t0.getChange_percent()) ? 1 : -1;
                                    }
                                }
                            });
                            break;
                    }
                    FavouriteListAdapter tableAdapter = new FavouriteListAdapter(getApplicationContext(), R.layout.fav_list_layout, stocks);
                    favouriteList.setAdapter(tableAdapter);
                }
                sortBySpinnerArrayAdapter.setSelection(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        order.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String temp = adapterView.getItemAtPosition(i).toString();
                if (temp.equals("Ascending")) {
                    isAscending = true;
                } else if (temp.equals("Descending")) {
                    isAscending = false;
                }
                if (sort_by_key != null) {
                    switch (sort_by_key) {
                        case "Default":
                            Collections.sort(stocks, new Comparator<Stock>() {
                                @Override
                                public int compare(Stock t0, Stock t1) {
                                    if (isAscending) {
                                        return t0.getTime() > t1.getTime() ? 1 : -1;

                                    } else {
                                        return t1.getTime() > t0.getTime() ? 1 : -1;

                                    }
                                }
                            });
                            break;
                        case "Symbol":
                            Collections.sort(stocks, new Comparator<Stock>() {
                                @Override
                                public int compare(Stock t0, Stock t1) {
                                    if (isAscending) {
                                        return t0.getSymbol().compareTo(t1.getSymbol());
                                    } else {
                                        return t1.getSymbol().compareTo(t0.getSymbol());

                                    }
                                }
                            });
                            break;
                        case "Price":
                            Collections.sort(stocks, new Comparator<Stock>() {
                                @Override
                                public int compare(Stock t0, Stock t1) {
                                    if (isAscending) {
                                        return (t0.getPrice() > t1.getPrice()) ? 1 : -1;

                                    } else {
                                        return (t1.getPrice() > t0.getPrice()) ? 1 : -1;

                                    }
                                }
                            });
                            break;
                        case "Change":
                            Collections.sort(stocks, new Comparator<Stock>() {
                                @Override
                                public int compare(Stock t0, Stock t1) {
                                    if (isAscending) {
                                        return (t0.getChange() > t1.getChange()) ? 1 : -1;
                                    } else {
                                        return (t1.getChange() > t0.getChange()) ? 1 : -1;

                                    }
                                }
                            });
                            break;
                        case "Change Percent":
                            Collections.sort(stocks, new Comparator<Stock>() {
                                @Override
                                public int compare(Stock t0, Stock t1) {
                                    if (isAscending) {
                                        return (t0.getChange_percent() > t1.getChange_percent()) ? 1 : -1;
                                    } else {
                                        return (t1.getChange_percent() > t0.getChange_percent()) ? 1 : -1;
                                    }
                                }
                            });
                            break;
                    }
                    FavouriteListAdapter tableAdapter = new FavouriteListAdapter(getApplicationContext(), R.layout.fav_list_layout, stocks);
                    favouriteList.setAdapter(tableAdapter);
                }
                orderByArrayAdapter.setSelection(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        favouriteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Stock stock = (Stock) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(MainActivity.this, StockDetailActivity.class);
                intent.putExtra("symbol", stock.getSymbol());
                startActivity(intent);
            }
        });
        favouriteList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view, Gravity.RIGHT);
                MenuInflater menuInflater = getMenuInflater();
                menuInflater.inflate(R.menu.menu_popup, popupMenu.getMenu());
                popupMenu.getMenu().findItem(R.id.title_item).setEnabled(false);
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.no:
                                break;
                            case R.id.yes:
                                Stock stock = stocks.get(i);
                                sharedPreferenceManager.removeFavourite(stock.getSymbol());
                                setUpDeleteFavouriteList();
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                return true;
            }
        });

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.favourite_list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle("Remove from Favourites");
            String[] menuItems = getResources().getStringArray(R.array.menu);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }


    void startRepeatingTask() {
        mAutoRefreshRunnable.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mAutoRefreshRunnable);
        hideProgressBar(mFavouriteListLoadingIndicator);
    }

    Runnable mAutoRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            refreshFavouriteList();
            mHandler.postDelayed(mAutoRefreshRunnable, mInterval);
        }
    };

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        String[] menuItems = getResources().getStringArray(R.array.menu);
        String menuItemName = menuItems[menuItemIndex];
        Stock stock = stocks.get(info.position);

        if (menuItemIndex == 0) {

        } else {
            sharedPreferenceManager.removeFavourite(stock.getSymbol());
//            stocks.remove(stock);
            setUpDeleteFavouriteList();

        }


//        TextView text = (TextView) findViewById(R.id.footer);
//        text.setText(String.format("Selected %s for item %s", menuItemName, listItemName));


        return true;
    }

    private void setUpFavouriteList() {
        stocks.clear();
        Map<String, ?> favList = sharedPreferenceManager.getAll();
        for (Map.Entry<String, ?> entry : favList.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue() instanceof String) {
                String value = (String) entry.getValue();
                Gson gson = new Gson();
                Stock stock = gson.fromJson(value, Stock.class);
                stocks.add(stock);
            }
        }
        Collections.sort(stocks, new Comparator<Stock>() {
            @Override
            public int compare(Stock t0, Stock t1) {
                return t0.getTime() > t1.getTime() ? 1 : -1;
            }
        });
        FavouriteListAdapter tableAdapter = new FavouriteListAdapter(getApplicationContext(), R.layout.fav_list_layout, stocks);
        favouriteList.setAdapter(tableAdapter);
    }

    private void setUpDeleteFavouriteList() {
        stocks.clear();
        hideProgressBar(mFavouriteListLoadingIndicator);
        Map<String, ?> favList = sharedPreferenceManager.getAll();
        for (Map.Entry<String, ?> entry : favList.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue() instanceof String) {
                String value = (String) entry.getValue();
                Gson gson = new Gson();
                Stock stock = gson.fromJson(value, Stock.class);
                stocks.add(stock);
            }
        }
        Collections.sort(stocks, new Comparator<Stock>() {
            @Override
            public int compare(Stock t0, Stock t1) {
                return t0.getTime() > t1.getTime() ? 1 : -1;
            }
        });
        FavouriteListAdapter tableAdapter = new FavouriteListAdapter(getApplicationContext(), R.layout.fav_list_layout, stocks);
        favouriteList.setAdapter(tableAdapter);

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.itemlayout,
                new ArrayList<String>());
        autoCompleteTextView.setAdapter(arrayAdapter);

    }

    private void requestForAutoCompleteList() {
        String paras = "?symbol=" + autoCompleteTextView.getText().toString();
        if (autoCompleteTextView.getText().toString().matches("^[a-zA-z]+$")) {
            showProgressBar(mLoadingIndicator);
//            mLoadingIndicator.setVisibility(View.VISIBLE);

            StringRequest stringRequest = new StringRequest(Request.Method.GET, AppURLs.AUTOCOMPLETE + paras, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    JSONObject jObj = null;

                    try {
                        ArrayList<String> autoCompleteItems = new ArrayList<>();
                        Log.d("response from autocomplete", response);
                        jObj = new JSONObject(response);
                        int error = jObj.getInt("error");
                        if (error == 0) {
                            JSONArray jsonArray = jObj.getJSONArray("data");
                            int size = jsonArray.length();
                            size = (size > 5) ? 5 : size;
                            for (int i = 0; i < size; i++) {
                                JSONObject items = jsonArray.getJSONObject(i);
                                String symbol = items.getString("Symbol");
                                String name = items.getString("Name");
                                String exchange = items.getString("Exchange");
                                AutoCompleteItem autoCompleteItem = new AutoCompleteItem(symbol, name, exchange);
                                autoCompleteItems.add(autoCompleteItem.toString());
                            }

                            ArrayAdapter arrayAdapter = new ArrayAdapter<String>(
                                    getApplicationContext(),
                                    R.layout.itemlayout,
                                    autoCompleteItems);
                            autoCompleteTextView.setAdapter(arrayAdapter);
                            autoCompleteTextView.setThreshold(1);
                            autoCompleteTextView.showDropDown();
                            hideProgressBar(mLoadingIndicator);
                        } else {
                            Log.d("Else", "error");

                            ArrayAdapter arrayAdapter = new ArrayAdapter<String>(
                                    getApplicationContext(),
                                    R.layout.itemlayout,
                                    new ArrayList<String>());
                            autoCompleteTextView.setAdapter(arrayAdapter);
                            autoCompleteTextView.setThreshold(1);
                            hideProgressBar(mLoadingIndicator);
                        }


                    } catch (JSONException e) {
                        Log.d("JSONException", "error");

                        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(
                                getApplicationContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                new ArrayList<String>());
                        autoCompleteTextView.setAdapter(arrayAdapter);
                        autoCompleteTextView.setThreshold(1);
                        hideProgressBar(mLoadingIndicator);

                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("responseError", error.getMessage());

                    ArrayAdapter arrayAdapter = new ArrayAdapter<String>(
                            getApplicationContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            new ArrayList<String>());
                    autoCompleteTextView.setAdapter(arrayAdapter);
                    autoCompleteTextView.setThreshold(1);
                    hideProgressBar(mLoadingIndicator);
                }
            }) {

            };
            AppController.getInstance().addToRequestQueue(stringRequest, "AutoComplete");
        } else {
            Log.d("hh", "error");
            autoCompleteTextView.dismissDropDown();

        }

    }

    @Override
    public void afterTextChanged(Editable editable) {
        timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                requestForAutoCompleteList();
            }
        }, DELAY); // 600ms delay before the timer executes the „run“ method from TimerTask

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.get_quote:
                String symbol = autoCompleteTextView.getText().toString();
                if (symbol.matches(".*[a-zA-z].*")) {
                    Intent intent = new Intent(MainActivity.this, StockDetailActivity.class);
                    intent.putExtra("symbol", symbol);
                    startActivity(intent);

                } else {
                    validationToast = Toast.makeText(getApplicationContext(), "Please enter the valid symbol", Toast.LENGTH_SHORT);
                    validationToast.show();
                }
                break;
            case R.id.manual_refresh_button:
                refreshFavouriteList();
                break;
            case R.id.clear_btn:
                clearInput();
                break;
        }
    }

    private void clearInput() {
        if (validationToast != null) {
            validationToast.cancel();
        }
        autoCompleteTextView.clearListSelection();
        autoCompleteTextView.dismissDropDown();
        autoCompleteTextView.setText("");
    }

    private void refreshFavouriteList() {

        final ArrayList<Stock> tempStocks = new ArrayList<>(stocks);
        final int[] size = {0};
        showProgressBar(mFavouriteListLoadingIndicator);
        for (final Stock stock : stocks) {

            String stock_symbol = stock.getSymbol();
            String paras = "?symbol=" + stock_symbol;

            StringRequest stringRequest = new StringRequest(Request.Method.GET, AppURLs.TABLE + paras, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    JSONObject jObj = null;
                    try {
                        Log.d("Response from Fav List", response);
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
                            Stock currentStock = new Stock(symbol, price, change, change_percent, stock.getTime());
                            Gson gson = new Gson();
                            String jsonInString = gson.toJson(currentStock);
                            sharedPreferenceManager.setFavourite(currentStock.getSymbol(), jsonInString);
                            tempStocks.remove(stock);
                            tempStocks.add(currentStock);
                            size[0]++;
                            if (size[0] == stocks.size()) {
                                hideProgressBar(mFavouriteListLoadingIndicator);
                                stocks.clear();
                                stocks.addAll(tempStocks);
                                sort_stocks();
                                ((ArrayAdapter) (favouriteList.getAdapter())).notifyDataSetChanged();

                            }
                        } else {
                            size[0]++;
                            if (size[0] == stocks.size()) {
                                hideProgressBar(mFavouriteListLoadingIndicator);
                                stocks.clear();
                                stocks.addAll(tempStocks);
                                sort_stocks();
                                ((ArrayAdapter) (favouriteList.getAdapter())).notifyDataSetChanged();

                            }
                        }


                    } catch (Exception e) {
                        size[0]++;
                        if (size[0] == stocks.size()) {
                            hideProgressBar(mFavouriteListLoadingIndicator);
                            stocks.clear();
                            stocks.addAll(tempStocks);
                            sort_stocks();
                            ((ArrayAdapter) (favouriteList.getAdapter())).notifyDataSetChanged();

                        }
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    size[0]++;
                    if (size[0] == stocks.size()) {
                        hideProgressBar(mFavouriteListLoadingIndicator);
                        stocks.clear();
                        stocks.addAll(tempStocks);
                        sort_stocks();
                        ((ArrayAdapter) (favouriteList.getAdapter())).notifyDataSetChanged();
                    }
                }
            }) {


            };
            AppController.getInstance().addToRequestQueue(stringRequest, "Refresh");

        }
    }

    private void sort_stocks() {
        if (sort_by_key != null) {
            switch (sort_by_key) {
                case "Default":
                    Collections.sort(stocks, new Comparator<Stock>() {
                        @Override
                        public int compare(Stock t0, Stock t1) {
                            if (isAscending) {
                                return t0.getTime() > t1.getTime() ? 1 : -1;

                            } else {
                                return t1.getTime() > t0.getTime() ? 1 : -1;

                            }
                        }
                    });
                    break;
                case "Symbol":
                    Collections.sort(stocks, new Comparator<Stock>() {
                        @Override
                        public int compare(Stock t0, Stock t1) {
                            if (isAscending) {
                                return t0.getSymbol().compareTo(t1.getSymbol());

                            } else {
                                return t1.getSymbol().compareTo(t0.getSymbol());

                            }
                        }
                    });
                    break;
                case "Price":
                    Collections.sort(stocks, new Comparator<Stock>() {
                        @Override
                        public int compare(Stock t0, Stock t1) {
                            if (isAscending) {
                                return (t0.getPrice() > t1.getPrice()) ? 1 : -1;

                            } else {
                                return (t1.getPrice() > t0.getPrice()) ? 1 : -1;

                            }
                        }
                    });
                    break;
                case "Change":
                    Collections.sort(stocks, new Comparator<Stock>() {
                        @Override
                        public int compare(Stock t0, Stock t1) {
                            if (isAscending) {
                                return (t0.getChange() > t1.getChange()) ? 1 : -1;
                            } else {
                                return (t1.getChange() > t0.getChange()) ? 1 : -1;

                            }
                        }
                    });
                    break;
                case "Change Percent":
                    Log.d("hh", "hehe");
                    Collections.sort(stocks, new Comparator<Stock>() {
                        @Override
                        public int compare(Stock t0, Stock t1) {
                            if (isAscending) {
                                return (t0.getChange_percent() > t1.getChange_percent()) ? 1 : -1;
                            } else {
                                return (t1.getChange_percent() > t0.getChange_percent()) ? 1 : -1;
                            }
                        }
                    });
                    break;
                default:
                    Collections.sort(stocks, new Comparator<Stock>() {
                        @Override
                        public int compare(Stock t0, Stock t1) {
                            return t0.getTime() > t1.getTime() ? 1 : -1;
                        }
                    });
                    break;
            }
        } else {
            Collections.sort(stocks, new Comparator<Stock>() {
                @Override
                public int compare(Stock t0, Stock t1) {
                    if (isAscending) {
                        return t0.getTime() > t1.getTime() ? 1 : -1;

                    } else {
                        return t1.getTime() > t0.getTime() ? 1 : -1;

                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpFavouriteList();
    }


    private void showProgressBar(final ProgressBar progressBar) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private void hideProgressBar(final ProgressBar progressBar) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

}