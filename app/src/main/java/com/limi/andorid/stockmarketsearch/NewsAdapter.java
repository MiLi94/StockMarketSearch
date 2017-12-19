package com.limi.andorid.stockmarketsearch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by limi on 2017/11/20.
 */

public class NewsAdapter extends ArrayAdapter<News> {
    int resourceID;

    public NewsAdapter(@NonNull Context context, int resource, @NonNull List<News> objects) {
        super(context, resource, objects);
        resourceID = resource;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        News table = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceID, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.title = view.findViewById(R.id.news_title);
            viewHolder.author = view.findViewById(R.id.news_author);
            viewHolder.date = view.findViewById(R.id.news_date);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.title.setText(table.getTitle());
        viewHolder.author.setText(table.getAuthor());
        viewHolder.date.setText(table.getDate());

        return view;

    }

    class ViewHolder {
        TextView title;
        TextView author;
        TextView date;
    }
}
