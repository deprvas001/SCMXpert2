package com.example.scmxpert.helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.se.omapi.Session;

import com.example.scmxpert.views.LoginScreen;

import java.util.HashMap;

public class SessionManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE =0;
    private static final String PREF_NAME = "SCMXpertPref";
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_NAME = "name";
    public static final String USER_NAME ="user_name";
    public static final String PARTNER_NAME = "partner_name";
    public static final String TOKEN = "token";


    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        editor = pref.edit();
    }

    //Create Login Session
    public void createLoginSession(String user_name,String partner,String token){
        editor.putBoolean(IS_LOGIN,true);
        editor.putString(USER_NAME,user_name);
        editor.putString(PARTNER_NAME,partner);
        editor.putString(TOKEN,token);
        editor.commit();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */

    public void checkLogin(){
        //Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginScreen.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }
    }

    /**
     * Get stored session data
     * */

    public HashMap<String,String> getUserDetails(){
        HashMap<String,String> user = new HashMap<String,String>();
        //user name
        user.put(USER_NAME,pref.getString(USER_NAME,null));
        user.put(PARTNER_NAME,pref.getString(PARTNER_NAME,null));
        user.put(TOKEN,pref.getString(TOKEN,null));

        return  user;
    }

    /**
     * Clear session details
     * */

    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        //After logout redirect user to login screen.
        Intent intent = new Intent(_context,LoginScreen.class);
        //Closing all the activities
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //Add new flag to start new activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        _context.startActivity(intent);

    }


    //Quick check for login
    public boolean isLoggedIn(){
        return  pref.getBoolean(IS_LOGIN,false);
    }
}
