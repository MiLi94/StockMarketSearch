package com.limi.andorid.stockmarketsearch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by limi on 2017/11/20.
 */

public class FavouriteListAdapter extends ArrayAdapter<Stock> {
    int resourceID;

    public FavouriteListAdapter(@NonNull Context context, int resource, @NonNull List<Stock> objects) {
        super(context, resource, objects);
        resourceID = resource;
    }

    @SuppressLint("DefaultLocale")
    public View getView(int position, View convertView, ViewGroup parent) {
        Stock stock = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceID, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.stock_symbol_fav = view.findViewById(R.id.stock_symbol_fav);
            viewHolder.stock_price = view.findViewById(R.id.stock_price);
            viewHolder.fav_change = view.findViewById(R.id.fav_change);
            viewHolder.fav_change_percent = view.findViewById(R.id.fav_change_percent);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        if (stock != null) {
            viewHolder.stock_symbol_fav.setText(stock.getSymbol());
            viewHolder.stock_price.setText(String.format("%.2f", stock.getPrice()));
            viewHolder.fav_change.setText(String.format("%.2f", stock.getChange()));
            String percent = "(" + String.format("%.2f", stock.getChange_percent()) + "%)";
            viewHolder.fav_change_percent.setText(percent);

            if (stock.getChange() > 0) {
                viewHolder.fav_change.setTextColor(getContext().getResources().getColor(R.color.green));
                viewHolder.fav_change_percent.setTextColor(getContext().getResources().getColor(R.color.green));

            } else {
                viewHolder.fav_change.setTextColor(getContext().getResources().getColor(R.color.red));
                viewHolder.fav_change_percent.setTextColor(getContext().getResources().getColor(R.color.red));
            }
        }
        return view;
    }

    @Nullable
    @Override
    public Stock getItem(int position) {
        return super.getItem(position);
    }

    class ViewHolder {
        TextView stock_symbol_fav;
        TextView stock_price;
        TextView fav_change;
        TextView fav_change_percent;
    }
}
