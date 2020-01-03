package com.example.scmxpert.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Toast;

import com.example.scmxpert.R;
import com.example.scmxpert.apiInterface.CompleteShipment;
import com.example.scmxpert.base.BaseActivity;
import com.example.scmxpert.databinding.ActivityFilterBinding;
import com.example.scmxpert.helper.SessionManager;
import com.example.scmxpert.model.CreateShipmentDrop;
import com.example.scmxpert.model.GoodsItem;
import com.example.scmxpert.model.RouteSpinnerData;
import com.example.scmxpert.model.ShipmentGoods;
import com.example.scmxpert.model.filter.FilterGetResponse;
import com.example.scmxpert.model.filter.FilterItemModel;
import com.example.scmxpert.service.RetrofitClientInstance;
import com.example.scmxpert.viewClick.DatePick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class FilterScreen extends BaseActivity implements View.OnClickListener , AdapterView.OnItemSelectedListener{
ActivityFilterBinding filterBinding;
    ArrayAdapter<String> device_Adapter, from_Adapter,to_Adapter,department_Adapter;
    private List<GoodsItem> goodsItems = new ArrayList<>();
    private List<String> device_id_list = new ArrayList<>();
    private List<String> from_list = new ArrayList<>();
    private List<String> to_list = new ArrayList<>();
    private List<String> department_list = new ArrayList<>();
    private CompositeDisposable disposable = new CompositeDisposable();
    ArrayAdapter<GoodsItem> good_adapter;
    String from_city,to_city,select_good,device, dep_type ;
    Intent intent;
    SessionManager session;
    private String user_name = "";
    private String partner_name = "";
    private String customer_id="";
    private String token = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filterBinding = DataBindingUtil.setContentView(this,R.layout.activity_filter);
        session = new SessionManager(getApplicationContext());
        getUserData();
        getDDData();
        setClickListener();
    }

    private void getUserData(){
        session.checkLogin();
        HashMap<String, String> user = session.getUserDetails();
        user_name = user.get(SessionManager.USER_NAME);
        partner_name = user.get(SessionManager.PARTNER_NAME);
        customer_id = user.get(SessionManager.CUSTOMER_ID);
        token = user.get(SessionManager.TOKEN);

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

    private void getDDData() {
        showProgressDialog(getResources().getString(R.string.loading));

        CompleteShipment apiService = RetrofitClientInstance.getClient(this).create(CompleteShipment.class);
        disposable.add(
                apiService.getDDData(customer_id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<CreateShipmentDrop>() {
                            @Override
                            public void onSuccess(CreateShipmentDrop shipmentDropDown) {
                                hideProgressDialog();
                               setDropDownItem(shipmentDropDown);
                               getFilterData();
                            }

                            @Override
                            public void onError(Throwable e) {
                                hideProgressDialog();
                                Toast.makeText(FilterScreen.this, getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                                finish();
                                //  Log.e(TAG, "onError: " + e.getMessage());
                            }
                        })
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.clear();
      //  getIntent().removeExtra("tag");
    }

    private void setDropDownItem(CreateShipmentDrop dropDownItem) {
        goodsItems.clear();
        device_id_list.clear();

        for (ShipmentGoods route : dropDownItem.getRoutes_type()) {
            if(!from_list.contains(route.getFrom()))
            from_list.add(route.getFrom());
            if(!to_list.contains(route.getTo()))
            to_list.add(route.getTo());
            }

        from_list.add(0,"Select From");
        to_list.add(0,"Select To");

        device_id_list = dropDownItem.getDeviceId();
        device_id_list.add(0, "Select Device");
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

        device_Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, device_id_list);
        device_Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        filterBinding.deviceIdSpinner.setAdapter(device_Adapter);

        /*

        reference_Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, reference_type_list);
        reference_Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        createShipmentBinding.typeReference.setAdapter(reference_Adapter);*/

        good_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, goodsItems);
        good_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        filterBinding.goodSpinner.setAdapter(good_adapter);


        from_Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, from_list);
        from_Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        filterBinding.fromSpinner.setAdapter(from_Adapter);


        to_Adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, to_list);
        to_Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        filterBinding.toSpinner.setAdapter(to_Adapter);
    }

    private void setClickListener(){
        filterBinding.searchButton.setOnClickListener(this);
        filterBinding.shipDateTxt.setOnClickListener(this);
        filterBinding.cancelButton.setOnClickListener(this);
        filterBinding.fromSpinner.setOnItemSelectedListener(this);
        filterBinding.toSpinner.setOnItemSelectedListener(this);
        filterBinding.goodSpinner.setOnItemSelectedListener(this);
        filterBinding.deviceIdSpinner.setOnItemSelectedListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ship_date_txt:
                new DatePick(this, filterBinding.shipDateTxt);
                break;


            case R.id.search_button:
               searchShipment();
            break;

            case R.id.cancel_button:
                finish();
                break;

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        switch (adapterView.getId()){
            case R.id.from_spinner:
                if(position > 0){
                   from_city = adapterView.getSelectedItem().toString();
                }else{
                    from_city ="";
                }
                break;

            case R.id.to_spinner:
                if(position > 0){
                    to_city = adapterView.getSelectedItem().toString();
                }else{
                    to_city ="";
                }
                break;

            case R.id.good_spinner:
                if(position > 0){
                    select_good = adapterView.getSelectedItem().toString();
                }else{
                    select_good ="";
                }
                break;

            case R.id.device_id_spinner:
                if(position > 0){
                    device = adapterView.getSelectedItem().toString();
                }else{
                    device ="";
                }
                break;

            case R.id.department_type:
                if(position > 0){
                    dep_type  = adapterView.getSelectedItem().toString();
                }else{
                    dep_type  ="";
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void searchShipment(){
        String ship_date = filterBinding.shipDateTxt.getText().toString();

        String reference=filterBinding.referenceTxt.getText().toString();
        String delivery_number = filterBinding.deliveryNumber.getText().toString();

        FilterItemModel itemModel = new FilterItemModel(from_city,to_city,
                select_good,device,dep_type,ship_date,reference,delivery_number);

        if(getIntent().getExtras()!= null){
            Bundle bundle = getIntent().getExtras();
           // String tag = bundle.getString("tag");
             intent = new Intent(FilterScreen.this,MapHome.class);
            intent.putExtra("filter_data",itemModel);
            startActivity(intent);
            finish();
        }else{
            intent = new Intent(FilterScreen.this,ShipmentHome.class);
            intent.putExtra("filter_data",itemModel);
            startActivity(intent);
            finish();
        }


    }


    private void getFilterData(){
        showProgressDialog(getResources().getString(R.string.loading));
        department_list.clear();
        CompleteShipment apiService = RetrofitClientInstance.getClient(this).create(CompleteShipment.class);
        disposable.add(
                apiService.getFilterData(customer_id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<FilterGetResponse>() {
                            @Override
                            public void onSuccess(FilterGetResponse filterGetResponse) {
                                hideProgressDialog();
                                department_list = filterGetResponse.getDepartments();
                                setFilterSpinner(department_list);
                            }

                            @Override
                            public void onError(Throwable e) {
                                hideProgressDialog();
                                Toast.makeText(FilterScreen.this, getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                                finish();
                                //  Log.e(TAG, "onError: " + e.getMessage());
                            }


                        })


        );
    }

    private void setFilterSpinner(List<String> department_list){
        department_list.add(0,"Select Dept/Type");
        department_Adapter= new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, department_list);
        department_Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        filterBinding.departmentType.setAdapter(department_Adapter);
    }


}
