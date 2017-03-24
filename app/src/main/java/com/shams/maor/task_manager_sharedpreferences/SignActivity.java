package com.shams.maor.task_manager_sharedpreferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.shams.maor.task_manager_sharedpreferences.utils.Constant;
import com.shams.maor.task_manager_sharedpreferences.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Copyright © 2017 Maor Shams. All rights reserved.
**/

public class SignActivity extends AppCompatActivity {

    private TextView email, username, password, notRegistered, alreadyRegistered, passHint;
    private Button register, login;
    private SharedPreferences prefs;
    private JSONObject jUser = new JSONObject();
    private Utils utils = new Utils();
    private static String uName, uPass, uEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        // Initialization
        prefs = getSharedPreferences(Constant.USER_DB, MODE_PRIVATE);

        alreadyRegistered = (TextView) findViewById(R.id.sign_alreadyRegistered);
        notRegistered = (TextView) findViewById(R.id.sign_notRegistered);
        passHint = (TextView) findViewById(R.id.sign_passHint);
        register = (Button) findViewById(R.id.sign_RegisterBtn);
        login = (Button) findViewById(R.id.sign_loginBtn);

        email = (EditText) findViewById(R.id.sign_editEmail);
        username = (EditText) findViewById(R.id.sign_editUserName);
        password = (EditText) findViewById(R.id.sign_editPassword);

        checkIfLoggedIn();

        reloadDB();
    }

    // Get User details
    private void getUserDetails() {
        uEmail = email.getText().toString();
        uName = username.getText().toString();
        uPass = password.getText().toString();
    }

    // check if the user is already connected
    private void checkIfLoggedIn() {
        String user = prefs.getString(Constant.LOGGED_KEY, null);
        if (user != null && !user.equals("")) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    // Check if user is not null, add to json // only logged details no tasks
    private void reloadDB() {
        String user = prefs.getString(Constant.USER_KEY, null);
        if (user != null) {
            try {
                jUser = new JSONObject(user);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //Get username if just login
    private String getUser() {
        uName = "no name";
        try {
            for (int i = 0; i < jUser.length(); i++) {
                if (jUser.getString(Constant.EMAIL).equals(uEmail)) {
                    uName = jUser.getString(Constant.USERNAME);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return uName;
    }

    // When clicking sign-in button
    public void onClickSignIn(View view) throws JSONException {
        getUserDetails();
        if (checkIfUserExist()) {
            if (checkIfUserMatch()) {
                prefs.edit().putString(Constant.LOGGED_KEY, uEmail + "|" + getUser()).apply();
                utils.setDraw(email, R.drawable.valid_input).setDraw(password, R.drawable.valid_input);
                startActivity(new Intent(this, MainActivity.class));
            } else {
                utils.toast(this, getString(R.string.user_or_pass_wrong));
                utils.setDraw(email, R.drawable.invalid_input).setDraw(password, R.drawable.invalid_input);
            }
        } else {
            utils.toast(this, getString(R.string.user_not_exist));
            utils.setDraw(email, R.drawable.invalid_input);
        }
    }

    // When clicking register button
    public void onClickRegister(View view) throws JSONException {
        getUserDetails();//get details from ui
        if (!checkIfUserExist()) {// check first if user already exist
            if (utils.isMatch(Utils.EMAIL_REGEX, uEmail) && utils.isMatch(Utils.PASS_REGEX, uPass) && !uName.isEmpty()) { // validation checking
                createNewUser(uName, uEmail, uPass);
                onClickSignIn(view);
            } else {// validation failed
                utils.toast(this, getString(R.string.invalid_validation));
                utils.setDraw(email, R.drawable.invalid_input).setDraw(password, R.drawable.invalid_input).setDraw(username, R.drawable.invalid_input);
            }
        } else {
            utils.toast(this, getString(R.string.already_exist));
            utils.setDraw(email, R.drawable.invalid_input);
        }
    }

    // Check if user already exist
    private boolean checkIfUserExist() {
        try {
            for (int i = 0; i < jUser.length(); i++) {
                if (jUser.getString(Constant.EMAIL).equals(uEmail)) return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Check if user email & pass match
    private boolean checkIfUserMatch() throws JSONException {
        for (int i = 0; i < jUser.length(); i++) {
            if (jUser.getString(Constant.EMAIL).equals(uEmail) &&
                    jUser.getString(Constant.PASSWORD).equals(uPass))
                return true;
        }
        return false;
    }

    // Add new user prefs
    private void createNewUser(String u, String e, String p) throws JSONException {
        jUser.put(Constant.USERNAME, u).put(Constant.EMAIL, e).put(Constant.PASSWORD, p);
        prefs.edit().putString(Constant.USER_KEY, jUser.toString()).apply();
        utils.setDraw(email, R.drawable.valid_input).setDraw(password, R.drawable.valid_input).setDraw(username, R.drawable.valid_input);
    }

    // Toggle to show Registration stuff
    public void onClickNotRegistered(View view) {
        username.setVisibility(View.VISIBLE);
        alreadyRegistered.setVisibility(View.VISIBLE);
        notRegistered.setVisibility(View.GONE);
        login.setVisibility(View.GONE);
        register.setVisibility(View.VISIBLE);
        passHint.setVisibility(View.VISIBLE);
    }

    // Toggle to show Login stuff
    public void onClickAlreadyRegistered(View view) {
        username.setVisibility(View.GONE);
        alreadyRegistered.setVisibility(View.GONE);
        notRegistered.setVisibility(View.VISIBLE);
        login.setVisibility(View.VISIBLE);
        register.setVisibility(View.GONE);
        passHint.setVisibility(View.GONE);
    }

    // When returning to this screen
    @Override
    protected void onStart() {
        super.onStart();
        checkIfLoggedIn();
        reloadDB();
    }

    // When Clicking back button quit app
    @Override
    public void onBackPressed() {
        this.finishAffinity();
    }

}
