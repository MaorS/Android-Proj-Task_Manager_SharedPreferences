package com.shams.maor.task_manager_sharedpreferences;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.shams.maor.task_manager_sharedpreferences.adapters.TaskAdapter;
import com.shams.maor.task_manager_sharedpreferences.utils.Constant;
import com.shams.maor.task_manager_sharedpreferences.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Copyright © 2017 Maor Shams. All rights reserved.
 **/

public class MainActivity extends AppCompatActivity {

    private static String c_key;
    private static JSONObject userData;
    private static ArrayList<JSONObject> tasks;
    private Utils utils = new Utils();
    private ListView myList;
    private TextView input;
    private SharedPreferences pref, users_prefs;
    private TaskAdapter TA;
    private ToggleButton tBtn;
    private ListAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        users_prefs = getSharedPreferences(Constant.USER_DB, MODE_PRIVATE);
        pref = getSharedPreferences(Constant.TASKS_DB, MODE_PRIVATE);
        c_key = users_prefs.getString(Constant.LOGGED_KEY, null);

        input = (TextView) findViewById(R.id.edit_newTask);

        tasks = new ArrayList<>();
        TA = new TaskAdapter(tasks, this);

        // list setup
        myList = (ListView) findViewById(R.id.myList);
        myList.setAdapter(TA);
        myAdapter = myList.getAdapter();
        myList.setOnItemClickListener(itemClickListener);
        myList.setOnItemLongClickListener(longClickListener);

        // ToggleButton - filter tasks
        tBtn = (ToggleButton) findViewById(R.id.toggleBtn);
        tBtn.setOnCheckedChangeListener(toggleListener);


        setInitialData();
    }

    // when clicking "Add task" button
    public void onClickAddNewTask(View view) {
        if (!(checkForValidTask(input))) return;
        try {
            tasks.add(new JSONObject().put(Constant.TASK, input.getText().toString().trim()).put(Constant.DONE, false));
            TA.updateResults(tasks, getFilter());
            updateGreetingMsg().saveState();
            input.setText("");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void setInitialData() {
        try {
            if (pref.getString(c_key, null) == null) {
                pref.edit().putString(c_key, null).apply();
                userData = new JSONObject();
            } else {
                userData = new JSONObject(pref.getString(c_key, "No_Data"));
                getTasksForUser(userData.getJSONArray(Constant.TASK_KEY)).updateGreetingMsg();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
            try {
                boolean currentState = ((JSONObject) myAdapter.getItem(i)).getBoolean(Constant.DONE);
                LinearLayout ll = (LinearLayout) myAdapter.getView(i, view, myList);
                ((JSONObject) myAdapter.getItem(i)).put(Constant.DONE, !currentState);
                ll.setBackgroundResource(!currentState ? R.drawable.strike : 0);
                saveState();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

            View editTask = LayoutInflater.from(MainActivity.this).inflate(R.layout.edit_task, null);
            final TextView input = (TextView) editTask.findViewById(R.id.editOldTask);
            final JSONObject current_item = (JSONObject) myAdapter.getItem(i);

            try {
                input.setText(current_item.getString(Constant.TASK));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.edit_task).setView(editTask).setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                if (checkForValidTask(input)) {
                                    current_item.put(Constant.TASK, input.getText().toString());
                                    TA.updateResults(tasks, getFilter());
                                    saveState();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            tasks.remove(current_item);
                            TA.updateResults(tasks, getFilter());
                            updateGreetingMsg().saveState();
                        }
                    }).setNeutralButton(R.string.cancel, null).setIcon(android.R.drawable.ic_menu_edit).show().show();
            return true;
        }
    };

    private boolean checkForValidTask(TextView tvTask) {
        String task = tvTask.getText().toString().trim();
        if (!(task.length() == 0)) {
            try {
                for (int i = 0; i < tasks.size(); i++) {
                    if ((tasks.get(i).getString(Constant.TASK).equalsIgnoreCase(task))) {
                        utils.toast(getBaseContext(), getString(R.string.task_exist));
                        return false;
                    }
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else utils.toast(getBaseContext(), getString(R.string.empty_task));
        return false;
    }

    private MainActivity getTasksForUser(JSONArray tasksFromObj) throws JSONException {
        tasks = new ArrayList<>();
        if (tasksFromObj != null) {
            for (int i = 0; i < tasksFromObj.length(); i++) {
                JSONObject t = new JSONObject(tasksFromObj.get(i).toString());
                tasks.add(t);
            }
        }
        TA.updateResults(tasks, getFilter());
        return this;
    }

    private void saveState() {
        try {
            userData.put(Constant.TASK_KEY, new JSONArray(tasks)); //set the new tasks array
            pref.edit().putString(c_key, userData.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void onClickDelete(View view) throws JSONException {
        int counter = 0;
        for (int i = 0; i < tasks.size(); i++) {
            if (!(tasks.get(i).getBoolean(Constant.DONE))) counter++;
        }
        String toDel = getString(R.string.delete_all_tasks);
        if (counter >= 1) toDel = counter + getString(R.string.uncomplete_task);
        new AlertDialog.Builder(this).setTitle(R.string.delete_tasks).setMessage(toDel)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        tasks.clear();
                        TA.updateResults(tasks, getFilter());
                        updateGreetingMsg().saveState();
                    }
                }).setNegativeButton(R.string.cancel, null).setIcon(android.R.drawable.ic_menu_delete).show();
    }

    CompoundButton.OnCheckedChangeListener toggleListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (tasks.size() >= 1) {
                TA.updateResults(tasks, getFilter());
            }
        }
    };

    private String getFilter() {
        if (tBtn.isChecked()) return Constant.DONE;
        else return "all";
    }

    private MainActivity updateGreetingMsg() {
        TextView tv = (TextView) findViewById(R.id.txt_task_count);
        tv.setText(getString(R.string.you_have) + " " + tasks.size() + " " + getString(R.string.tasks));
        return this;
    }

    private void logOut() {
        users_prefs.edit().remove("connected").apply();
        userData = null;
        tasks = null;
        startActivity(new Intent(getBaseContext(), SignActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        setInitialData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);//What Options Menu to present
        menu.findItem(R.id.connectedAs).setTitle(getString(R.string.hi) + " " + utils.getUser(c_key));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logoutBtn) {
            logOut();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
    }

}