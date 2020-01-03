package com.example.scmxpert.views.login;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import com.example.scmxpert.R;
import com.example.scmxpert.apiInterface.CompleteShipment;
import com.example.scmxpert.base.BaseActivity;
import com.example.scmxpert.databinding.ActivityLoginScreenBinding;
import com.example.scmxpert.helper.SessionManager;
import com.example.scmxpert.model.Shippment;
import com.example.scmxpert.model.UserDetails;
import com.example.scmxpert.model.filter.FilterItemModel;
import com.example.scmxpert.service.RetrofitClientInstance;
import com.example.scmxpert.views.forgotPassword.ForgotPassword;
import com.example.scmxpert.views.ShipmentHome;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;


public class LoginScreen extends BaseActivity implements View.OnClickListener {
    SessionManager manager;
    ActivityLoginScreenBinding screenBinding;
    LoginViewModel loginViewModel;
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        screenBinding = DataBindingUtil.setContentView(this,R.layout.activity_login_screen);
        setClickListener();

        manager = new SessionManager(getApplicationContext());

        // Check if UserResponse is Already Logged In
        if (manager.isLoggedIn()) {
              startActivity(new Intent(LoginScreen.this, ShipmentHome.class));
              finish();
        } else {
            screenBinding.layout.setVisibility(View.VISIBLE);
        }
    }

    private void setClickListener() {
        screenBinding.btnLogin.setOnClickListener(this);
        screenBinding.forgotPassword.setOnClickListener(this);
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
        if (TextUtils.isEmpty(screenBinding.inputEmail.getText().toString())) {
            showAlertDialog(this, getString(R.string.error_user_empty));
        } else if (TextUtils.isEmpty(screenBinding.inputPassword.getText().toString())) {
            showAlertDialog(this, getString(R.string.error_passwrod_empty));
        } else {
            if (isNetworkAvailable(this)) {
                userLogin(screenBinding.inputEmail.getText().toString(), screenBinding.inputPassword.getText().toString());
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
        showProgressDialog(getString(R.string.loading));
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        loginViewModel.getLoginUser(this, username, password).observe(this, loginApiResponse -> {

            if (loginApiResponse == null) {
                // handle error here
                hideProgressDialog();
                showAlertDialog(this, getString(R.string.invalid_credentails));
                return;
            }
            if (loginApiResponse.getError() == null) {

                // call is successful
                String access = loginApiResponse.getResponse().getAccessToken();
                getUserDetails(username,access);
               // Toast.makeText(LoginScreen.this, access, Toast.LENGTH_SHORT).show();
               // finish();

            } else {
                // call failed.
                hideProgressDialog();
                Throwable e = loginApiResponse.getError();
                Toast.makeText(LoginScreen.this, "Error is " + e.getMessage(), Toast.LENGTH_SHORT).show();
                // Log.e(TAG, "Error is " + e.getLocalizedMessage());
            }
        });
    }

    private void getUserDetails(String username,String token){

        CompleteShipment apiService = RetrofitClientInstance.getClient(LoginScreen.this).create(CompleteShipment.class);
        disposable.add(
                apiService.getUserDetails(username)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<UserDetails>() {
                            @Override
                            public void onSuccess(UserDetails userDetails) {
                                hideProgressDialog();
                                Toast.makeText(LoginScreen.this, userDetails.getUserName(), Toast.LENGTH_SHORT).show();
                                manager.createLoginSession(userDetails.getUserName(), userDetails.getAdmin_Name(), token,userDetails.getCustomer_Id());
                                startActivity(new Intent(LoginScreen.this, ShipmentHome.class));
                            }
                            @Override
                            public void onError(Throwable e) {
                                hideProgressDialog();
                            }
                        })
        );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
