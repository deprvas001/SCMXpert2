package com.example.scmxpert.views;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.scmxpert.R;
import com.example.scmxpert.base.BaseActivity;
import com.example.scmxpert.constants.ApiConstants;
import com.example.scmxpert.helper.SaveSharedPreference;
import com.example.scmxpert.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginScreen extends BaseActivity implements View.OnClickListener {
    EditText email, password;
    TextView forgot_password;
    Button login;
    LinearLayout login_layout;
    SessionManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        initializeView();
        setClickListener();

        manager = new SessionManager(getApplicationContext());

        // Check if UserResponse is Already Logged In
        if (manager.isLoggedIn()) {
              startActivity(new Intent(LoginScreen.this,ShipmentHome.class));
              finish();
        } else {
            login_layout.setVisibility(View.VISIBLE);
        }
    }

    private void initializeView() {
        email = findViewById(R.id.input_email);
        password = findViewById(R.id.input_password);
        forgot_password = findViewById(R.id.forgot_password);
        login = findViewById(R.id.btn_login);
        login_layout = findViewById(R.id.layout);
    }

    private void setClickListener() {
        login.setOnClickListener(this);
        forgot_password.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                checkLoginCredentials();
                break;

            case R.id.forgot_password:
                goToForgotPassword();
                break;
        }


    }

    private void checkLoginCredentials() {
        if (TextUtils.isEmpty(email.getText().toString())) {
            showAlertDialog(this, getString(R.string.error_user_empty));
        } else if (TextUtils.isEmpty(password.getText().toString())) {
            showAlertDialog(this, getString(R.string.error_passwrod_empty));
        } else {
            if (isNetworkAvailable(this)) {
                showProgressDialog(getString(R.string.progress_log_in));
                userLogin(email.getText().toString(), password.getText().toString());
            } else {
                showAlertDialog(this, getString(R.string.no_connection));
            }

        }
    }

    private void goToForgotPassword() {
        startActivity(new Intent(this, ForgotPassword.class));
    }

    /**
     * Login API call
     */
    private void userLogin(String username, String password) {
        RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiConstants.BASE_LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideProgressDialog();
                        try {
                            JSONObject object = new JSONObject(response);
                            String token = object.getString("access_token");
                            manager.createLoginSession(username, "BP0001", token);
                            startActivity(new Intent(LoginScreen.this, ShipmentHome.class));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideProgressDialog();
                        Toast.makeText(LoginScreen.this, "Invalid Login ", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(ApiConstants.USERNAME, username);
                params.put(ApiConstants.PASSWORD, password);
                params.put(ApiConstants.GRANT_TYPE, "password");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put(ApiConstants.CONTENT_TYPE, "application/x-www-form-urlencoded");
                String creds = String.format("%s:%s", ApiConstants.AUTH_USER_NAME, ApiConstants.AUTH_PASSWORD);
                String auth = ApiConstants.BASIC + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put(ApiConstants.AUTHROZATION, auth);
                return params;
            }
        };

        mQueue.add(stringRequest);
    }


}
