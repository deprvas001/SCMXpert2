package com.example.scmxpert.views;

import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.example.scmxpert.R;
import com.example.scmxpert.base.BaseActivity;

public class ForgotPassword extends BaseActivity implements View.OnClickListener {
Button reset_password;
EditText email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        reset_password = findViewById(R.id.btn_reset);
        email = findViewById(R.id.input_email);
        Toolbar toolbar = (Toolbar) findViewById(R.id.custom_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.back_arrow));
        setClickListener();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_reset:
                checkLoginCredentials();
                break;

        }
    }

    private void setClickListener() {
        reset_password.setOnClickListener(this);
    }

    private void checkLoginCredentials() {
        /*if (TextUtils.isEmpty(email.getText().toString())) {
            email.setError(getString(R.string.error_field_empty));
        } else {
            showProgressDialog(getString(R.string.progress_log_in));
            userReset(email.getText().toString());
        }*/
    }

    private void userReset(String email){

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                //  alertDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
