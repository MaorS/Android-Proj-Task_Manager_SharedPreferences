package com.shams.maor.task_manager_sharedpreferences.utils;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright © 2017 Maor Shams. All rights reserved.
 **/

public class Utils {
    public static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    public static final Pattern PASS_REGEX = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\\s).{5,16}$", Pattern.CASE_INSENSITIVE);

    public String getUser(String key) {
        return key.substring(key.lastIndexOf("|") + 1);
    }

    public void toast(Context c, String v) {
        Toast.makeText(c, v, Toast.LENGTH_SHORT).show();
    }

    public boolean isMatch(Pattern p, String toCheck) {
        Matcher matcher = p.matcher(toCheck);
        return matcher.find();
    }

    public Utils setDraw(TextView tv, int drw) {
        tv.setBackgroundResource(drw);
        return this;
    }

}

