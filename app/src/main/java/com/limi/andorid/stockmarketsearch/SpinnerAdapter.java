package com.limi.andorid.stockmarketsearch;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by limi on 2017/11/28.
 */

public class SpinnerAdapter<String> extends ArrayAdapter<String> {
    public SpinnerAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
    }

    int mSelectedIndex = -1;

    @Override
    public boolean isEnabled(int position) {
        if (position == 0 || position == mSelectedIndex) {
            // Disable the second item from Spinner
            return false;
        } else {
            return true;
        }
    }

    public void setSelection(int position) {
        mSelectedIndex = position;
        notifyDataSetChanged();
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView tv = (TextView) view;
        if (position == 0 || position == mSelectedIndex) {
            // Set the disable item text color
            tv.setTextColor(Color.GRAY);
        } else {
            tv.setTextColor(Color.BLACK);
        }
        return view;
    }
}
