package com.example.scmxpert.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.scmxpert.R;
import com.example.scmxpert.ViewModel.ShipmentStatus;
import com.example.scmxpert.ViewModel.ShipmentStatusFactory;
import com.example.scmxpert.adapter.UpdateEventAdapter;
import com.example.scmxpert.apiInterface.CompleteShipment;
import com.example.scmxpert.base.BaseActivity;
import com.example.scmxpert.constants.ApiConstants;
import com.example.scmxpert.databinding.ActivityCompleteShipmentBinding;
import com.example.scmxpert.helper.SessionManager;
import com.example.scmxpert.model.ApiResponse;
import com.example.scmxpert.model.CompleteShipmentModel;
import com.example.scmxpert.model.CreateShipmentDrop;
import com.example.scmxpert.model.Shippment;
import com.example.scmxpert.model.UpdateEventDetails;
import com.example.scmxpert.model.UpdateEventModel;
import com.example.scmxpert.service.RetrofitClientInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

public class CompleteShipmentFill extends BaseActivity implements View.OnClickListener {
    ActivityCompleteShipmentBinding completeShipmentBinding;
    private UpdateEventAdapter eventAdapter;
    Shippment shippment;
    private List<UpdateEventModel> event_list = new ArrayList<>();
    private CompositeDisposable disposable = new CompositeDisposable();
    ShipmentStatus shipmentViewModel;
    private List<String> reference_type_list = new ArrayList<>();
    private List<String> partner_list = new ArrayList<>();
    ArrayAdapter<String>  reference_Adapter,partner_Adapter;
    SessionManager session;
    String user_name="",partner_name="",timezone,token="",partner_id="",reference_id="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        completeShipmentBinding = DataBindingUtil.setContentView(this,R.layout.activity_complete_shipment);
        if (getIntent() != null) {
            shippment = (Shippment) getIntent().getSerializableExtra(ApiConstants.SHIPMENT);
        }
        initializeView();
        setOnClickListener();
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

    private void initializeView(){
        setSupportActionBar(completeShipmentBinding.customToolbar);
        completeShipmentBinding.customToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.back_arrow));
        completeShipmentBinding.layoutCompleteFill.shipmentId.setText(shippment.getShipment_id());
        completeShipmentBinding.layoutCompleteFill.shipmentNumber.setText(shippment.getShipment_num());
        completeShipmentBinding.layoutCompleteFill.refernceType.setText(shippment.getType_reference());
        completeShipmentBinding.layoutCompleteFill.connectedDevice.setText(shippment.getDevice_id());

        eventAdapter =new  UpdateEventAdapter(CompleteShipmentFill.this,event_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        completeShipmentBinding.layoutCompleteFill.recyclerView.setLayoutManager(mLayoutManager);
        completeShipmentBinding.layoutCompleteFill.recyclerView.setItemAnimator(new DefaultItemAnimator());
        completeShipmentBinding.layoutCompleteFill.recyclerView.setAdapter(eventAdapter);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        HashMap<String,String> user = session.getUserDetails();
        user_name = user.get(SessionManager.USER_NAME);
        partner_name = user.get(SessionManager.PARTNER_NAME);
        token = user.get(SessionManager.TOKEN);

        getDDData();

    }
    private void getDDData() {
        showProgressDialog(getResources().getString(R.string.loading));

        CompleteShipment apiService = RetrofitClientInstance.getClient(this).create(CompleteShipment.class);
        disposable.add(
                apiService.getUpdateEventDetails(shippment.getShipment_id())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<UpdateEventDetails>>() {
                            @Override
                            public void onSuccess(List<UpdateEventDetails> updateEvent) {
                                hideProgressDialog();
                                event_list.clear();
                                for(int i=0;i<updateEvent.size();i++){
                                    String event_id = updateEvent.get(i).getEvent_ID();
                                    String partner = updateEvent.get(i).getPartner_from();
                                    String event = updateEvent.get(i).getEvent_Name();
                                    String status = updateEvent.get(i).getEvent_status();
                                    String date = updateEvent.get(i).getEvent_exec_date();
                                    UpdateEventModel model = new UpdateEventModel(event_id,partner,event,date,status);
                                    event_list.add(model);
                                    // reference_type_list.add(updateEvent.get(i).getType_of_reference());
                                }

                                // notify adapter about data set changes
                                // so that it will render the list with new data
                                eventAdapter.notifyDataSetChanged();
                                 getReferenceType();

                            }

                            @Override
                            public void onError(Throwable e) {
                                hideProgressDialog();
                                Toast.makeText(CompleteShipmentFill.this, "Try Later", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "onError: " + e.getMessage());
                            }
                        })
        );
    }

    public void getReferenceType(){
        reference_type_list.clear();
        shipmentViewModel = ViewModelProviders.of(this,new ShipmentStatusFactory(getApplication(),user_name)).get(ShipmentStatus.class);

        shipmentViewModel.getDropDownData().observe(this, new Observer<CreateShipmentDrop>() {
            @Override
            public void onChanged(CreateShipmentDrop response) {
                if (response != null) {

                    reference_type_list = response.getReference_type();
                    reference_type_list.add(0,getString(R.string.select_reference));
                    reference_Adapter = new ArrayAdapter<String>(CompleteShipmentFill.this, android.R.layout.simple_spinner_item, reference_type_list);
                    reference_Adapter.setDropDownViewResource(R.layout.spinner_item);

                    // attaching data adapter to spinner
                    completeShipmentBinding.completeShipmentReference.setAdapter(reference_Adapter);


                    partner_list = response.getPartner_id();
                    partner_list.add(0,getString(R.string.select_partner));
                    partner_Adapter = new ArrayAdapter<String>(CompleteShipmentFill.this, android.R.layout.simple_spinner_item, partner_list);
                    partner_Adapter.setDropDownViewResource(R.layout.spinner_item);

                    // attaching data adapter to spinner
                    completeShipmentBinding.completeShipmentPartner.setAdapter(partner_Adapter);

                }
            }
        });
    }


    private void completeShipment(){
        showProgressDialog(getString(R.string.loading));
        CompleteShipmentModel completeShipment = new CompleteShipmentModel();
        completeShipment.setShipment_number("T000000011");
        completeShipment.setPartner("BP0003");
        completeShipment.setEvent("Final Shipment");
        completeShipment.setDateandTime("today time");
        completeShipment.setEventId("E0003");
        completeShipment.setPartnerFrom("BP0003");
        completeShipment.setReceivingLocation("Chennai");
        completeShipment.setReceivingReferenceNumber("999999999");
        completeShipment.setTypeOfReference("Delivery");
        completeShipment.setComments("Testing");
        completeShipment.setDeviceReturnLocation("Melborne");

        CompleteShipment apiService = RetrofitClientInstance.getClient(this).create(CompleteShipment.class);
        disposable.add(apiService.completeShipment(completeShipment)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ApiResponse>() {
                    @Override
                    public void onSuccess(ApiResponse updateEvent) {
                        if(updateEvent.getStatus()){
                            showAlertDialog(CompleteShipmentFill.this,updateEvent.getMessage());
                        }else{
                            showAlertDialog(CompleteShipmentFill.this,updateEvent.getMessage());
                        }

                        hideProgressDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgressDialog();
                        Toast.makeText(CompleteShipmentFill.this, "Try Later", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onError: " + e.getMessage());
                    }
                })
        );

    }

    private void setOnClickListener(){
        completeShipmentBinding.createShipment.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.createShipment:
                completeShipment();
                break;
        }
    }
}
