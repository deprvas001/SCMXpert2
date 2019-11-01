package com.example.scmxpert.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.scmxpert.R;
import com.example.scmxpert.adapter.ShipmentDetailAdapter;
import com.example.scmxpert.apiInterface.CompleteShipment;
import com.example.scmxpert.constants.ApiConstants;
import com.example.scmxpert.helper.SaveSharedPreference;
import com.example.scmxpert.map.Map;
import com.example.scmxpert.model.ShipmentDetail;
import com.example.scmxpert.model.Shippment;
import com.example.scmxpert.model.WayPoint;
import com.example.scmxpert.views.HomeScreen;
import com.example.scmxpert.views.ShipmentDetails;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapFragment extends Fragment implements OnMapReadyCallback{
    private GoogleMap googleMap;
    Shippment shippment;
    private ArrayList<ShipmentDetail> data = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.map_fragment_layout, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.frg);  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        mapFragment.getMapAsync(this);

        if(getActivity().getIntent()!=null){
            shippment = (Shippment) getActivity().getIntent().getSerializableExtra(ApiConstants.SHIPMENT);
        }
        return rootView;
    }


    public void getShipmentDetails(){
        List<WayPoint> userList = new  ArrayList<>();
        List<LatLng> route_array = new ArrayList<LatLng>();
        String json = String.valueOf(shippment.getWaypoint());
        if(json !=null) {
            try {
                JSONArray array = new JSONArray(json);
                int count = array.length();
                for (int i = 0; i < count; i++) {
                    JSONArray innerArray = array.getJSONArray(i);
                    WayPoint point = new WayPoint();
                    point.setLat(innerArray.get(0).toString());
                    point.setLongt(innerArray.get(1).toString());
                    point.setId(shippment.getShipment_id());

                    String lat = innerArray.get(0).toString();
                    String longt = innerArray.get(1).toString();
                    userList.add(point);

                    createMarker(Double.parseDouble(lat),Double.parseDouble(longt),shippment.getShipment_id());

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (int k=0;k<userList.size();k++){
            String lat = userList.get(k).getLat();
            String longt = userList.get(k).getLongt();
            LatLng latLng = new LatLng(Double.parseDouble(lat),Double.parseDouble(longt));

            if(!route_array.contains(latLng)){
                route_array.add(latLng);
            }
        }
        drawLine(route_array);




       /* ((ShipmentDetails)getActivity()).showProgressDialog(getResources().getString(R.string.loading));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CompleteShipment.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CompleteShipment api = retrofit.create(CompleteShipment.class);
        Call<List<ShipmentDetail>> call = api.getShipmentDetails(shippment.getShipment_id());

        call.enqueue(new Callback<List<ShipmentDetail>>() {
            @Override
            public void onResponse(Call<List<ShipmentDetail>> call, retrofit2.Response<List<ShipmentDetail>> response) {
                try {
                    ((ShipmentDetails)getActivity()).hideProgressDialog();
                    data = (ArrayList<ShipmentDetail>) response.body();
                    Log.e("SCMXpert",data.toString());

                    for(int i=0;i<data.size();i++){
                        Double latitude = Double.parseDouble(data.get(i).getLatitdue());
                        Double longitude = Double.parseDouble(data.get(i).getLongitude());
                        String title = data.get(i).getId();

                        createMarker(latitude,longitude,title*//*, markersArray.get(i).getTitle(), markersArray.get(i).getSnippet(), markersArray.get(i).getIconResID()*//*);
                    }

                } catch (Exception e) {
                    ((ShipmentDetails)getActivity()).hideProgressDialog();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<List<ShipmentDetail>> call, Throwable t) {
                ((ShipmentDetails)getActivity()).hideProgressDialog();
                Log.e("SCMXPERT",t.getMessage());
            }
        });*/
    }

    protected Marker createMarker(double latitude, double longitude,String title/*, String title, String snippet, int iconResID*/) {
        LatLng position = new LatLng(latitude, longitude);

        CameraPosition googlePlex = CameraPosition.builder()
                .target(position)
                .zoom(5)
                .bearing(0)
                .tilt(45)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex));
        return googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f)
                .title(title));
              /*  .snippet(snippet)
                .icon(BitmapDescriptorFactory.fromResource(iconResID)));*/
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        getShipmentDetails();
    }

    public void drawLine(List<LatLng> points) {
        if (points == null) {
            Log.e("Draw Line", "got null as parameters");
            return;
        }

        Polyline line = googleMap.addPolyline(new PolylineOptions().width(3).color(Color.RED));

        line.setPoints(points);
    }
}
