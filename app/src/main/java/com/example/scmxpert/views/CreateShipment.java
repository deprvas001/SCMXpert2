package com.example.scmxpert.views;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.scmxpert.R;
import com.example.scmxpert.ViewModel.ShipmentStatus;
import com.example.scmxpert.ViewModel.ShipmentStatusFactory;
import com.example.scmxpert.apiInterface.CompleteShipment;
import com.example.scmxpert.base.BaseActivity;
import com.example.scmxpert.constants.ApiConstants;
import com.example.scmxpert.databinding.ActivityCreateShipmentBinding;
import com.example.scmxpert.helper.CommonMethod;
import com.example.scmxpert.helper.SessionManager;
import com.example.scmxpert.model.CreateNewShipmentResponse;
import com.example.scmxpert.model.CreateShipmentDrop;
import com.example.scmxpert.model.CreateShipmentResponse;
import com.example.scmxpert.model.Event_Details;
import com.example.scmxpert.model.GoodsItem;
import com.example.scmxpert.model.RouteSpinnerData;
import com.example.scmxpert.model.ShipmentDetail;
import com.example.scmxpert.model.ShipmentGoods;
import com.example.scmxpert.model.Shippment;
import com.example.scmxpert.service.RetrofitClientInstance;
import com.example.scmxpert.viewClick.DatePick;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.example.scmxpert.constants.ApiConstants.CREATED_BY;
import static com.example.scmxpert.constants.ApiConstants.CREATE_SHIPMENT;
import static com.example.scmxpert.constants.ApiConstants.CUSTOMER_NAME;
import static com.example.scmxpert.constants.ApiConstants.QRCODE_KEY;
import static com.example.scmxpert.constants.ApiConstants.SHARED_PREF_NAME;
import static com.google.android.gms.plus.PlusOneDummyView.TAG;

public class CreateShipment extends BaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    ActivityCreateShipmentBinding createShipmentBinding;
    ArrayAdapter<String> device_Adapter, reference_Adapter;
    ArrayAdapter<RouteSpinnerData> route_adapter;
    ArrayAdapter<GoodsItem> good_adapter;
    public static final int QRCODE_REQUEST_CODE = 99;
    private List<String> reference_type_list = new ArrayList<>();
    private List<String> device_id_list = new ArrayList<>();
    private List<String> good_type_list = new ArrayList<>();
    private List<String> route_list = new ArrayList<>();
    private List<RouteSpinnerData> route_data = new ArrayList<>();
    private List<GoodsItem> goodsItems = new ArrayList<>();
    private CompositeDisposable disposable = new CompositeDisposable();
    private String type_reference_data = "", device_detail_data = "", route_id = "", goods_data = "",
            goods_id_data = "", user_name = "", partner_name = "", timezone, token = "";
    private RouteSpinnerData routeSpinnerData;
    SessionManager session;
    List<Event_Details> event_list = new ArrayList<>();
    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createShipmentBinding = DataBindingUtil.setContentView(this, R.layout.activity_create_shipment);
        setSupportActionBar(createShipmentBinding.customToolbar);
        createShipmentBinding.customToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.back_arrow));

        setClickListener();
        getShipmentDetail();
    }

    private void setClickListener() {
        createShipmentBinding.shipmentNumber.setOnClickListener(this);
        createShipmentBinding.expectDate.setOnClickListener(this);
        createShipmentBinding.submitShipment.setOnClickListener(this);
        createShipmentBinding.routeView.setOnClickListener(this);
        createShipmentBinding.typeReference.setOnItemSelectedListener(this);
        createShipmentBinding.routeSpinner.setOnItemSelectedListener(this);
        createShipmentBinding.deviceSpinner.setOnItemSelectedListener(this);
        createShipmentBinding.goodsSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.shipment_number:
                 i = new Intent(CreateShipment.this, QrCodeScreen.class);
                startActivityForResult(i, QRCODE_REQUEST_CODE);
                break;

            case R.id.expect_date:
                DatePick fromDate = new DatePick(this, createShipmentBinding.expectDate);

                break;

            case R.id.route_view:
                i = new Intent(CreateShipment.this, AddRoute.class);
                startActivity(i);
                break;

            case R.id.submit_shipment:
                if (CommonMethod.isNetworkAvailable(CreateShipment.this)) {
                    String internal_shipment_id = createShipmentBinding.shipmentDetails.internalShipmentId.getText().toString();
                    String customer_name = createShipmentBinding.shipmentDetails.customerName.getText().toString();
                    String partner_name = createShipmentBinding.shipmentDetails.partnerName.getText().toString();
                    String shipment_number = createShipmentBinding.shipmentNumber.getText().toString();
                    String description = createShipmentBinding.shipmentDescription.getText().toString();
                    String expect_date = createShipmentBinding.expectDate.getText().toString();

                    if (internal_shipment_id.isEmpty()) {
                        showAlertDialog(CreateShipment.this, getString(R.string.internal_shipment_empty));
                    } else if (customer_name.isEmpty()) {
                        showAlertDialog(CreateShipment.this, getString(R.string.customer_name_empty));
                    } else if (partner_name.isEmpty()) {
                        showAlertDialog(CreateShipment.this, getString(R.string.partner_name_empty));
                    } else if (shipment_number.isEmpty()) {
                        showAlertDialog(CreateShipment.this, getString(R.string.shipment_number_empty));
                    } else if (description.isEmpty()) {
                        showAlertDialog(CreateShipment.this, getString(R.string.description_empty));
                    } else if (type_reference_data.isEmpty()) {
                        showAlertDialog(CreateShipment.this, getString(R.string.type_reference_empty));
                    } else if (route_id.isEmpty()) {
                        showAlertDialog(CreateShipment.this, getString(R.string.route_empty));
                    } else if (device_detail_data.isEmpty()) {
                        showAlertDialog(CreateShipment.this, getString(R.string.device_empty));
                    } else if (expect_date.isEmpty()) {
                        showAlertDialog(CreateShipment.this, getString(R.string.expect_date_empty));
                    } else if (goods_data.isEmpty()) {
                        showAlertDialog(CreateShipment.this, getString(R.string.goods_empty));
                    } else {
                        createShipment();
                    }
                } else {
                    CommonMethod.showAlert(getString(R.string.no_connection), CreateShipment.this);
                }

                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == QRCODE_REQUEST_CODE) {
                String qr_code = data.getExtras().getString(QRCODE_KEY);
                createShipmentBinding.shipmentNumber.setText(qr_code);
            }
        }
    }

    private void getShipmentDetail() {
        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        HashMap<String, String> user = session.getUserDetails();
        user_name = user.get(SessionManager.USER_NAME);
        partner_name = user.get(SessionManager.PARTNER_NAME);
        token = user.get(SessionManager.TOKEN);

        if (user_name != null) {
            createShipmentBinding.shipmentDetails.customerName.setText(user_name);
            createShipmentBinding.shipmentDetails.partnerName.setText(partner_name);
        }
        getDDData();
    }


    @Override
    protected void onStop() {
        super.onStop();

    }

    private void getDDData() {
        showProgressDialog(getResources().getString(R.string.loading));

        CompleteShipment apiService = RetrofitClientInstance.getClient(this).create(CompleteShipment.class);
        disposable.add(
                apiService.getDDData("SCM0001-A00001")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<CreateShipmentDrop>() {
                            @Override
                            public void onSuccess(CreateShipmentDrop shipmentDropDown) {
                                hideProgressDialog();
                                setDropDownItem(shipmentDropDown);
                            }

                            @Override
                            public void onError(Throwable e) {
                                hideProgressDialog();
                                Toast.makeText(CreateShipment.this, getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                                finish();
                                Log.e(TAG, "onError: " + e.getMessage());
                            }
                        })
        );
    }

    private void setDropDownItem(CreateShipmentDrop dropDownItem) {
        reference_type_list.clear();
        device_id_list.clear();
        good_type_list.clear();
        route_list.clear();
        route_data.clear();
        goodsItems.clear();
        device_id_list = dropDownItem.getDeviceId();
        reference_type_list = dropDownItem.getReference_type();
        good_type_list.add(0, "Select Goods");
        device_id_list.add(0, "Select Device");
        route_list.add(0, "Select Route");
        reference_type_list.add(0, "Select Reference");

        RouteSpinnerData route_start = new RouteSpinnerData();
        route_start.setRoute_from("Select Route");
        route_start.setRoute_to("");
        route_start.setMode_of_transport("");
        route_start.setInco_term("");

        route_data.add(0, route_start);
        for (ShipmentGoods route : dropDownItem.getRoutes_type()) {
            RouteSpinnerData route_spinner = new RouteSpinnerData();
            route_spinner.setRoute_from(route.getFrom());
            route_spinner.setRoute_to(route.getTo());
            route_spinner.setMode_of_transport(route.getMode_of_transport());
            route_spinner.setInco_term(route.getInco_term());
            route_spinner.setRoute_id(route.getRouteId());
            route_spinner.setEvent_details(route.getEvents());

            route_data.add(route_spinner);
            // route_list.add(route.getFrom()+","+route.getTo()+","+route.getMode_of_transport()+","+route.getInco_term());
        }

        GoodsItem goodsItem = new GoodsItem();
        goodsItem.setGoods("Select Goods");

        goodsItems.add(0, goodsItem);

        for (ShipmentGoods route : dropDownItem.getGoods_type()) {

            GoodsItem good_spinner = new GoodsItem();
            good_spinner.setGoods(route.getGood_item());
            good_spinner.setGoods_id(route.getGoods_id());

            goodsItems.add(good_spinner);
            // route_list.add(route.getFrom()+","+route.getTo()+","+route.getMode_of_transport()+","+route.getInco_term());
        }

        device_Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, device_id_list);
        device_Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        createShipmentBinding.deviceSpinner.setAdapter(device_Adapter);

        reference_Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, reference_type_list);
        reference_Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        createShipmentBinding.typeReference.setAdapter(reference_Adapter);

        good_adapter = new ArrayAdapter<GoodsItem>(this, android.R.layout.simple_spinner_item, goodsItems);
        good_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        createShipmentBinding.goodsSpinner.setAdapter(good_adapter);

        route_adapter = new ArrayAdapter<RouteSpinnerData>(this, android.R.layout.simple_spinner_item, route_data);
        route_adapter.setDropDownViewResource(R.layout.spinner_item);

        // attaching data adapter to spinner
        createShipmentBinding.routeSpinner.setAdapter(route_adapter);

        createShipmentBinding.shipmentDetails.internalShipmentId.setText(dropDownItem.getInternal_shipment_id());
    }

    private void createShipment() {

        JSONObject params = new JSONObject();
        try {
            JSONArray event_array = new JSONArray();
            for (int i = 0; i < event_list.size(); i++) {
                Event_Details details = event_list.get(i);
                JSONObject object = new JSONObject();
                object.put("event Id", details.getEvent_Id());
                object.put("partner", details.getBp_id());
                object.put("event", details.getEvent_name());
                object.put("date", "");
                event_array.put(object);
            }

            JSONArray comment = new JSONArray();
            comment.put(createShipmentBinding.shipmentDescription.getText().toString());
            params.put("internalShipmentId", createShipmentBinding.shipmentDetails.internalShipmentId.getText().toString());
            params.put("shipment_Num", createShipmentBinding.shipmentNumber.getText().toString());
            params.put("customerId", user_name);
            /* params.put("shipment_Number","T000000041");*/
            params.put("typeOfReference", type_reference_data);
            params.put("comments", comment);
            params.put("routeId", routeSpinnerData.getRoute_id());
            params.put("routeFrom", routeSpinnerData.getRoute_from());
            params.put("routeTo", routeSpinnerData.getRoute_to());
            params.put("goodsId", goods_id_data);
            params.put("goodsType", goods_data);
            params.put("parnterFrom", partner_name);  //fixed
            params.put("deviceId", device_detail_data);
            params.put("allEvents", event_array);
            params.put("incoTerms", routeSpinnerData.getInco_term());
            params.put("mode", routeSpinnerData.getMode_of_transport());
            params.put("temp", "");
            params.put("rH", "");
            params.put("created_Date", timeCreate());
            params.put("estimatedDeliveryDate", createShipmentBinding.expectDate.getText().toString());
            Log.i("SCM", params.toString());
        } catch (JSONException e) {

        }
        RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jobReq = new JsonObjectRequest(Request.Method.POST, CREATE_SHIPMENT, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {

                        if (jsonObject.has(getString(R.string.status))) {
                            try {
                                String message = jsonObject.getString(getString(R.string.message));
                                boolean status = jsonObject.getBoolean(getString(R.string.status));
                                if (status == true) {
                                    successAlert(message);
                                } else if (status == false) {
                                    showAlertDialog(CreateShipment.this, message);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        Toast.makeText(CreateShipment.this, String.valueOf(jsonObject), Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        String json = "";
                        if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                            try {
                                json = new String(volleyError.networkResponse.data,
                                        HttpHeaderParser.parseCharset(volleyError.networkResponse.headers));
                                try {
                                    JSONObject object = new JSONObject(json);
                                    String error = object.getString("error");
                                    if (object.has("status")) {
                                        showAlertDialog(CreateShipment.this, error);
                                    } else if (error.equals("invalid_token")) {
                                        Toast.makeText(CreateShipment.this, getString(R.string.session_timeout), Toast.LENGTH_SHORT).show();
                                        //  showAlertDialog(CreateShipment.this,getString(R.string.session_timeout));
                                        finish();
                                        session.logoutUser();
                                    } else {
                                        showAlertDialog(CreateShipment.this, getString(R.string.try_later));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    showAlertDialog(CreateShipment.this, getString(R.string.create_shipment));
                                }

                            } catch (UnsupportedEncodingException e) {

                            }

                        }
                        VolleyLog.e("Error: ", volleyError.getMessage());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put(ApiConstants.CONTENT_TYPE, "application/json");
                String bearer = "Bearer ".concat(token);
                params.put("Authorization", bearer);
                return params;
            }
        };
        mQueue.add(jobReq);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

        switch (adapterView.getId()) {
            case R.id.route_spinner:
                if (position > 0) {
                    RouteSpinnerData data = (RouteSpinnerData) createShipmentBinding.routeSpinner.getSelectedItem();
                    routeSpinnerData = data;
                    route_id = data.getRoute_id();
                    event_list = data.getEvent_details();
                }
                break;

            case R.id.type_reference:
                if (position > 0) {
                    type_reference_data = createShipmentBinding.typeReference.getSelectedItem().toString();
                }
                break;

            case R.id.device_spinner:
                if (position > 0) {
                    device_detail_data = createShipmentBinding.deviceSpinner.getSelectedItem().toString();
                }
                break;

            case R.id.goods_spinner:
                if (position > 0) {
                    GoodsItem item = (GoodsItem) createShipmentBinding.goodsSpinner.getSelectedItem();
                    goods_data = item.getGoods();
                    goods_id_data = item.getGoods_id();

                }
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private String timeCreate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z", Locale.ENGLISH);
        timezone = sdf.format(c.getTime());
        return timezone;
    }

    public void successAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateShipment.this);
        builder.setTitle(getString(R.string.app_name))
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, which) -> {
                    builder.create().dismiss();
                    startActivity(new Intent(CreateShipment.this, CreateShipment.class));
                    finish();
                });
        builder.create().show();
    }

}
