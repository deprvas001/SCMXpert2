package com.example.scmxpert.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.scmxpert.R;
import com.example.scmxpert.adapter.ShipmentAdapter;
import com.example.scmxpert.apiInterface.CompleteShipment;
import com.example.scmxpert.constants.ApiConstants;
import com.example.scmxpert.map.Map;
import com.example.scmxpert.model.Shippment;
import com.example.scmxpert.service.RetrofitClientInstance;
import com.example.scmxpert.viewClick.RecyclerTouchListener;
import com.example.scmxpert.views.MapHome;
import com.example.scmxpert.views.ShipmentDetails;
import com.example.scmxpert.views.ShipmentHome;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;
import static com.example.scmxpert.constants.ApiConstants.CREATED_BY;
import static com.example.scmxpert.constants.ApiConstants.CUSTOMER_NAME;
import static com.example.scmxpert.constants.ApiConstants.SHARED_PREF_NAME;
import static com.google.android.gms.plus.PlusOneDummyView.TAG;

public class LiveShipment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private View view;
    private RecyclerView recyclerView;
    private ShipmentAdapter mAdapter;
    private FloatingActionButton fab;
    private SharedPreferences.Editor editor;
    private SharedPreferences sp;
    SwipeRefreshLayout swipeRefresh;
    private ArrayList<Shippment> liveList = new ArrayList<>();
    private CompositeDisposable disposable = new CompositeDisposable();
    private ArrayList<Shippment> deliverdList = new ArrayList<>();
    public LiveShipment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.live_shipment, container, false);
        initializeView();
        getAllShipment();

        mAdapter = new ShipmentAdapter(liveList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        swipeRefresh.setOnRefreshListener(this);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Shippment shippment = liveList.get(position);
                Intent intent = new Intent(getActivity(), ShipmentDetails.class);
                intent.putExtra(ApiConstants.SHIPMENT, shippment);
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
              startActivity(new Intent(getActivity(), MapHome.class));
                break;
        }
    }

    public void initializeView() {
        recyclerView =  view.findViewById(R.id.recycler_view);
        swipeRefresh = view.findViewById(R.id.swiperefresh);
        fab =  view.findViewById(R.id.fab);
        sp = getActivity().getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        editor = sp.edit();
        fab.setImageResource(R.drawable.map_show);
        fab.setOnClickListener(this);
    }


    private void getAllShipment() {
        swipeRefresh.setRefreshing(true);
      //  ((ShipmentHome)getActivity()).showProgressDialog(getResources().getString(R.string.loading));
        final String customer_id = "SCM0002-A00001";
        final String partner = "BP0001";

        CompleteShipment apiService = RetrofitClientInstance.getClient(getActivity()).create(CompleteShipment.class);
        disposable.add(
                apiService.getAllShipmentDetails()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<Shippment>>() {
                            @Override
                            public void onSuccess(List<Shippment> notes) {
                               swipeRefresh.setRefreshing(false);
                             //   ((ShipmentHome)getActivity()).hideProgressDialog();

                                editor.putString(CUSTOMER_NAME, customer_id);
                                editor.putString(CREATED_BY, partner);
                                editor.apply();

                                deliverdList.clear();
                                liveList.clear();
                                for(Shippment shippment:notes){
                                    try {
                                        String status = shippment.getDelivery_status();
                                        if(status !=null){
                                            if(!status.equals(getString(R.string.deliver)) ){
                                                liveList.add(shippment);
                                            }else {
                                                deliverdList.add(shippment);
                                            }
                                        }
                                        }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                                ((ShipmentHome)getActivity()).live_count.setText(String.valueOf(liveList.size()));
                                ((ShipmentHome)getActivity()).deliver_count.setText(String.valueOf(deliverdList.size()));
                                mAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(Throwable e) {
                                swipeRefresh.setRefreshing(false);
                             //   ((ShipmentHome)getActivity()).hideProgressDialog();
                                Log.e(TAG, "onError: " + e.getMessage());
                            }
                        })

        );
    }

    @Override
    public void onRefresh() {
        getAllShipment();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.dispose();
    }
}
