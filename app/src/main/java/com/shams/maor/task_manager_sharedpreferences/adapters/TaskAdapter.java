package com.shams.maor.task_manager_sharedpreferences.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shams.maor.task_manager_sharedpreferences.R;
import com.shams.maor.task_manager_sharedpreferences.utils.Constant;
import com.shams.maor.task_manager_sharedpreferences.adapters.TaskAdapter;
import com.shams.maor.task_manager_sharedpreferences.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Copyright © 2017 Maor Shams. All rights reserved.
 **/

public class TaskAdapter extends BaseAdapter implements Filterable {

    private final Context context;//Context for view creation
    private ArrayList<JSONObject> data;//raw data

    public TaskAdapter(ArrayList<JSONObject> data, Context context) {
        this.data = data;
        this.context = context;
    }

    //how many views to create
    public int getCount() {
        return data.size();
    }

    //item by index (position)
    public JSONObject getItem(int i) {
        return data.get(i);
    }

    //ID => index of given item
    public long getItemId(int i) {
        return i;
    }

    //called .getCount() times - for each View of item in data
    public View getView(int i, View recycleView, ViewGroup parent) {
        if (recycleView == null)
            recycleView = LayoutInflater.from(context).inflate(R.layout.item, null);

        TextView taskNum = (TextView) ((LinearLayout) recycleView).getChildAt(0);// TextView number of task
        TextView taskTxt = (TextView) ((LinearLayout) recycleView).getChildAt(1);// TextView the task

        try {
            taskTxt.setText(data.get(i).getString(Constant.TASK));
            taskNum.setText(i + 1 + "");
            LinearLayout li = (LinearLayout) recycleView;
            boolean currentState = (data.get(i)).getBoolean(Constant.DONE);
            li.setBackgroundResource(currentState ? R.drawable.strike : 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return recycleView;
    }

    public void updateResults(ArrayList<JSONObject> data, String filter) {
        this.data = data;
        this.getFilter().filter(filter);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String filtParam = charSequence.toString();
                FilterResults results = new FilterResults();
                ArrayList<JSONObject> FilteredTasks = new ArrayList<>();

                try {
                    for (int i = 0; i < data.size(); i++) {
                        if ((data.get(i).getBoolean(Constant.DONE) && filtParam.equals(Constant.DONE)) || filtParam.equals(Constant.ALL))
                            FilteredTasks.add(data.get(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                results.count = FilteredTasks.size();
                results.values = FilteredTasks;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                data = (ArrayList<JSONObject>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
