package com.example.scmxpert.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.scmxpert.R;
import com.example.scmxpert.apiInterface.CompleteShipment;
import com.example.scmxpert.model.MapModel;
import com.example.scmxpert.model.ShipmentLatLong;
import com.example.scmxpert.model.Shippment;
import com.example.scmxpert.model.WayPoint;
import com.example.scmxpert.service.RetrofitClientInstance;
import com.example.scmxpert.views.MapHome;
import com.example.scmxpert.views.ShipmentHome;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

public class LiveMapShipment extends Fragment implements OnMapReadyCallback {
    ArrayList<Object> waveList =new ArrayList<>();
    private View rootView;
    GoogleMap gMap;
    private ArrayList<Shippment> liveList = new ArrayList<>();
    private ArrayList<Shippment> deliverdList = new ArrayList<>();
    private CompositeDisposable disposable = new CompositeDisposable();

    public LiveMapShipment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        rootView = inflater.inflate(R.layout.map_fragment_layout, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.frg);  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        mapFragment.getMapAsync(this);
        return rootView;
    }


    private void getAllShipment() {
        ((MapHome) getActivity()).showProgressDialog(getResources().getString(R.string.loading));

        CompleteShipment apiService = RetrofitClientInstance.getClient(getActivity()).create(CompleteShipment.class);
        disposable.add(
                apiService.getAllShipmentDetails()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<Shippment>>() {
                            @Override
                            public void onSuccess(List<Shippment> notes) {
                                ((MapHome) getActivity()).hideProgressDialog();

                                liveList.clear();
                                deliverdList.clear();
                                for (Shippment shippment : notes) {
                                    if(shippment.getDelivery_status() !=null){
                                        if (!shippment.getDelivery_status().equals(getString(R.string.deliver))) {
                                            liveList.add(shippment);
                                            ((MapHome) getActivity()).live_count.setText(String.valueOf(liveList.size()));
                                        } else {
                                            deliverdList.add(shippment);
                                            ((MapHome) getActivity()).deliver_count.setText(String.valueOf(deliverdList.size()));
                                        }
                                    }
                                }

                                List<WayPoint> userList = new ArrayList<>();
                                for(int k=0;k<liveList.size();k++){
                                    String json = String.valueOf(liveList.get(k).getWaypoint());
                                  if(json !=null){
                                      try {
                                          JSONArray array = new JSONArray(json);
                                          int count = array.length();
                                          for (int i = 0; i < count; i++) {
                                              JSONArray innerArray = array.getJSONArray(i);
                                              WayPoint point = new WayPoint();
                                              point.setLat(innerArray.get(0).toString());
                                              point.setId(liveList.get(k).getShipment_id());
                                              point.setLongt(innerArray.get(1).toString());
                                              userList.add(point);
                                          }

                                      }catch (JSONException e){
                                          e.printStackTrace();
                                      }
                                  }
                                }

                                addMarker(userList);
                            }

                            @Override
                            public void onError(Throwable e) {
                                ((ShipmentHome) getActivity()).hideProgressDialog();
                                Log.e(TAG, "onError: " + e.getMessage());
                            }
                        })
        );
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        waveList.clear();
        gMap = googleMap;
        getAllShipment();


    }

    private void addMarker(List<WayPoint> wayPoints){
       /* List<ShipmentLatLong> shipmentLatLongList= new ArrayList<>();
        for(int i=0;i<wayPoints.size();i++){

            Double lat = Double.parseDouble(wayPoints.get(i).getLat());
            Double longt = Double.parseDouble(wayPoints.get(i).getLongt());

            ShipmentLatLong shipmentLatLong = new ShipmentLatLong(wayPoints.get(i).getId(), new LatLng(lat,longt));
            shipmentLatLongList.add(shipmentLatLong);

        }

        ClusterManager<ShipmentLatLong> clusterManager = new ClusterManager(getActivity(), gMap);  // 3
        gMap.setOnCameraIdleListener(clusterManager);
        clusterManager.addItems(shipmentLatLongList);  // 4
        clusterManager.cluster();  // 5*/

        /*Double init_lat = Double.parseDouble(wayPoints.get(0).getLat());
        Double init_longt = Double.parseDouble(wayPoints.get(0).getLongt());
        CameraPosition googlePlex = CameraPosition.builder()
                .target(new LatLng(init_lat, init_longt))
                .zoom(8)
                .bearing(0)
                .tilt(45)
                .build();
        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 10000, null);*/
        for(int i=0;i<wayPoints.size();i++){



            Double lat = Double.parseDouble(wayPoints.get(i).getLat());
            Double longt = Double.parseDouble(wayPoints.get(i).getLongt());

            ShipmentLatLong shipmentLatLong = new ShipmentLatLong(wayPoints.get(i).getId(), new LatLng(lat,longt));

            gMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, longt))
                    .title(wayPoints.get(i).getId()));
                    //.icon(bitmapDescriptorFromVector(getActivity(), R.drawable.position)));
                    //.snippet("His Talent : Plenty of money"));

        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.dispose();
    }
}
