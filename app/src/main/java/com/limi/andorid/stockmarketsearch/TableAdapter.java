package com.limi.andorid.stockmarketsearch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by limi on 2017/11/20.
 */

public class TableAdapter extends ArrayAdapter<Table> {
    int resourceID;

    public TableAdapter(@NonNull Context context, int resource, @NonNull List<Table> objects) {
        super(context, resource, objects);
        resourceID = resource;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Table table = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceID, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.label = view.findViewById(R.id.table_label);
            viewHolder.content = view.findViewById(R.id.table_detail);
            viewHolder.imageView = view.findViewById(R.id.table_up_down);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.label.setText(table.getLabel());
        viewHolder.content.setText(table.getContent());

        if (table.getLabel().equals("Change")) {
            viewHolder.imageView.setVisibility(View.VISIBLE);
            if (table.isIncrease()) {
                viewHolder.imageView.setImageResource(R.drawable.up);
            } else {
                viewHolder.imageView.setImageResource(R.drawable.down);
            }
        } else {
            viewHolder.imageView.setVisibility(View.INVISIBLE);
        }
        return view;

    }

    class ViewHolder {
        TextView label;
        TextView content;
        ImageView imageView;
    }
}
