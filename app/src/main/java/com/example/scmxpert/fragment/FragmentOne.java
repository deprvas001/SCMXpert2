package com.example.scmxpert.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.scmxpert.R;
import com.example.scmxpert.ViewModel.ShipmentStatus;
import com.example.scmxpert.ViewModel.ShipmentStatusFactory;
import com.example.scmxpert.adapter.ShipmentAdapter;
import com.example.scmxpert.adapter.ShipmentDetailAdapter;
import com.example.scmxpert.apiInterface.CompleteShipment;
import com.example.scmxpert.constants.ApiConstants;
import com.example.scmxpert.databinding.FragmentOneBinding;
import com.example.scmxpert.helper.SaveSharedPreference;
import com.example.scmxpert.model.ShipmentDetail;
import com.example.scmxpert.model.Shippment;
import com.example.scmxpert.views.CompleteShipmentFill;
import com.example.scmxpert.views.HomeScreen;
import com.example.scmxpert.views.ShipmentDetails;
import com.example.scmxpert.views.UpdateEvent;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FragmentOne extends Fragment implements View.OnClickListener {
    private ShipmentDetailAdapter mAdapter;
    Shippment shippment;
    View view;
    private ArrayList<ShipmentDetail> shipmentArrayList = new ArrayList<>();
    ShipmentStatus shipmentViewModel;
    FragmentOneBinding fragmentOneBinding;
    Intent intent;
    public FragmentOne() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentOneBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_one,container,false);

        // Inflate the layout for this fragment
         view = fragmentOneBinding.getRoot();
         if(getActivity().getIntent()!=null){
            shippment = (Shippment) getActivity().getIntent().getSerializableExtra(ApiConstants.SHIPMENT);
          }
         initializeView();
         setView();

        mAdapter = new ShipmentDetailAdapter(shipmentArrayList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        fragmentOneBinding.recyclerView.setLayoutManager(mLayoutManager);
        fragmentOneBinding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        fragmentOneBinding.recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        fragmentOneBinding.recyclerView.setAdapter(mAdapter);
        getShipmentDetails();

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.complete_shipment:
               fragmentOneBinding.shipmentLayout.updateEvent.
                       setBackgroundDrawable(getResources().getDrawable(R.drawable.button_second_background));
               fragmentOneBinding.shipmentLayout.updateEvent.setTextColor(getResources().getColor(R.color.view_background));
                fragmentOneBinding.shipmentLayout.shareButton.
                        setBackgroundDrawable(getResources().getDrawable(R.drawable.button_second_background));
                fragmentOneBinding.shipmentLayout.shareButton.setTextColor(getResources().getColor(R.color.view_background));
                fragmentOneBinding.shipmentLayout.completeShipment.
                        setBackgroundDrawable(getResources().getDrawable(R.drawable.button_background));
                fragmentOneBinding.shipmentLayout.completeShipment.setTextColor(getResources().getColor(R.color.color_white));

                intent = new Intent(getActivity(),CompleteShipmentFill.class);
                intent.putExtra(ApiConstants.SHIPMENT, shippment);
                startActivity(intent);
                break;

            case R.id.update_event:
                fragmentOneBinding.shipmentLayout.completeShipment.
                        setBackgroundDrawable(getResources().getDrawable(R.drawable.button_second_background));
                fragmentOneBinding.shipmentLayout.completeShipment.setTextColor(getResources().getColor(R.color.view_background));
                fragmentOneBinding.shipmentLayout.updateEvent.
                        setBackgroundDrawable(getResources().getDrawable(R.drawable.button_background));
                fragmentOneBinding.shipmentLayout.updateEvent.setTextColor(getResources().getColor(R.color.color_white));

                fragmentOneBinding.shipmentLayout.shareButton.
                        setBackgroundDrawable(getResources().getDrawable(R.drawable.button_second_background));
                fragmentOneBinding.shipmentLayout.shareButton.setTextColor(getResources().getColor(R.color.view_background));

                intent= new Intent(getActivity(),UpdateEvent.class);
                intent.putExtra(ApiConstants.SHIPMENT, shippment);
                startActivity(intent);
                 break;

            case R.id.share_button:
                fragmentOneBinding.shipmentLayout.completeShipment.
                    setBackgroundDrawable(getResources().getDrawable(R.drawable.button_second_background));
                fragmentOneBinding.shipmentLayout.completeShipment.setTextColor(getResources().getColor(R.color.view_background));
                fragmentOneBinding.shipmentLayout.updateEvent.
                        setBackgroundDrawable(getResources().getDrawable(R.drawable.button_second_background));
                fragmentOneBinding.shipmentLayout.updateEvent.setTextColor(getResources().getColor(R.color.view_background));

                fragmentOneBinding.shipmentLayout.shareButton.
                        setBackgroundDrawable(getResources().getDrawable(R.drawable.button_background));
                fragmentOneBinding.shipmentLayout.shareButton.setTextColor(getResources().getColor(R.color.color_white));

               //  startActivity(new Intent(getActivity(), UpdateEvent.class));
                break;
        }
    }

    public void getShipmentDetails(){
        shipmentViewModel = ViewModelProviders.of(this,new ShipmentStatusFactory(getActivity().getApplication(),shippment.getShipment_id())).get(ShipmentStatus.class);

        shipmentViewModel.getShipmentDetaillData().observe(this, new Observer<List<ShipmentDetail>>() {
            @Override
            public void onChanged(List<ShipmentDetail> articleResponse) {
                if (articleResponse != null) {
                    List<ShipmentDetail> shippments = articleResponse;
                    shipmentArrayList.addAll(shippments);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void initializeView(){
        fragmentOneBinding.shipmentLayout.updateEvent.setOnClickListener(this);
        fragmentOneBinding.shipmentLayout.completeShipment.setOnClickListener(this);
        fragmentOneBinding.shipmentLayout.shareButton.setOnClickListener(this);
    }


    public  String getDate(String time_date){
        if(time_date!=null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
            try {
                time_date = (sdf1.format(sdf.parse(time_date)));

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return  time_date;
    }

    public void setView(){
        fragmentOneBinding.shipmentLayout.customerShippmentId.setText(shippment.getShipment_id());
        String created_date = getDate(shippment.getCreated_date());
        String delivery_date = getDate(shippment.getDelivery_date());
        fragmentOneBinding.shipmentLayout.fromDate.setText("From- "+shippment.getRoute_form());
        fragmentOneBinding.shipmentLayout.toDate.setText("To- "+shippment.getRoute_to());
        fragmentOneBinding.shipmentLayout.status.setText("Status: "+shippment.getDelivery_status());
        fragmentOneBinding.shipmentLayout.createDate.setText(created_date);
        fragmentOneBinding.shipmentLayout.deliverDate.setText(delivery_date);
    }


}
