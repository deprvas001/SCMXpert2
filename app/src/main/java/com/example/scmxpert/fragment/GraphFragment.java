package com.example.scmxpert.fragment;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.scmxpert.R;
import com.example.scmxpert.apiInterface.CompleteShipment;
import com.example.scmxpert.constants.ApiConstants;
import com.example.scmxpert.model.DeviceTempData;
import com.example.scmxpert.model.Shippment;
import com.example.scmxpert.service.RetrofitClientInstance;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.PointValue;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

public class GraphFragment extends Fragment implements LineChartOnValueSelectListener {
    View view;
    Shippment shippment;
    private CompositeDisposable disposable = new CompositeDisposable();
    private LineChart mChart;
    List<String> xAxisValues = new ArrayList<>();
    List<String> yAxisValues = new ArrayList<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.activity_shipment_graph,container,false);
        mChart =  view.findViewById(R.id.lineChart);

        if(getActivity().getIntent()!=null){
            shippment = (Shippment) getActivity().getIntent().getSerializableExtra(ApiConstants.SHIPMENT);
        }

        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                //  float value =  e.getX();
                //  Toast.makeText(getActivity(), h.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });

        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setPinchZoom(true);
        mChart.setDoubleTapToZoomEnabled(true);
        mChart.setHorizontalScrollBarEnabled(true);
        mChart.getViewPortHandler().setMaximumScaleX(1.5f);
        mChart.getViewPortHandler().setMaximumScaleY(1.f);

        if(shippment.getWaypoint()!=null){
            getTempData();
        }



      /*  MyMarkerView mv = new MyMarkerView(getApplicationContext(), R.layout.custom_marker_view);
        mv.setChartView(mChart);
        mChart.setMarker(mv);*/
      //String setter in x-Axis
        return view;
    }

    public void renderData() {
        LimitLine llXAxis = new LimitLine(0.0f, "Index 10");
        llXAxis.setLineWidth(1f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);

        XAxis xAxis = mChart.getXAxis();
        //  xAxis.enableGridDashedLine(10f, 10f, 0f);
            xAxis.setAxisMaximum(10f);
        xAxis.setAxisMinimum(0f);
        xAxis.setDrawLimitLinesBehindData(true);
        xAxis.setGranularity(0f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(-65f);


       /* LimitLine ll1 = new LimitLine(215f, "Maximum Limit");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);*/

      /*  LimitLine ll2 = new LimitLine(70f, "Minimum Limit");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);*/

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
       /* leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);*/
        //   leftAxis.setAxisMaximum(60f);

        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(170f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
      //  leftAxis.setAxisMaximum(80f);

        /*if (yAxisValues != null || yAxisValues.size() != 0) {
            // create a new list to avoid modification
            // in the original list
         List<Float> internal_temp_list  =  getList(yAxisValues);
            // sort list in natural order
            Collections.sort(internal_temp_list);

            // first element in the sorted list
            // would be minimum
            leftAxis.setAxisMinimum(internal_temp_list.get(0));
        }else{
            leftAxis.setAxisMinimum(0f);
        }*/
        //  leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);

        leftAxis.setDrawLimitLinesBehindData(false);

        mChart.getAxisRight().setEnabled(false);
        setData();


    }

    private void setData() {
        ArrayList<Entry> values = new ArrayList<>();

        for(int i =0;i<yAxisValues.size();i++){
            values.add(new Entry(i, (float) Float.parseFloat(yAxisValues.get(i))));
        }


        LineDataSet set1;
        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set1 = new LineDataSet(values, "Internal Temperature");
            set1.setDrawIcons(false);
            // set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(getResources().getColor(R.color.view_background));
            set1.setCircleColor(getResources().getColor(R.color.view_background));
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(5f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);
            mChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(xAxisValues));

            if (Utils.getSDKInt() >= 18) {
                Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.fade_blue);
                set1.setFillDrawable(drawable);
            } else {
                set1.setFillColor(Color.DKGRAY);
            }
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            LineData data = new LineData(dataSets);
            mChart.setData(data);

            mChart.invalidate();
        }
    }

    @Override
    public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
        Toast.makeText(getActivity(), "Selected: " + value, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onValueDeselected() {

    }

    private void getTempData() {

        CompleteShipment apiService = RetrofitClientInstance.getClient(getActivity()).create(CompleteShipment.class);
        disposable.add(
                apiService.getDeviceDataTemp(shippment.getShipment_id())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<DeviceTempData>>() {
                            @Override
                            public void onSuccess(List<DeviceTempData> deviceTempData) {
                                xAxisValues.clear();
                                yAxisValues.clear();
                                if(deviceTempData.size()>0){
                                    List<DeviceTempData> last_ten_item = deviceTempData.subList(deviceTempData.size()-10, deviceTempData.size());

                                    try{
                                        for(int i=0;i<last_ten_item.size();i++){

                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

                                            try{
                                                xAxisValues.add(sdf1.format(sdf.parse(last_ten_item.get(i).getDate())));
                                            }catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                            yAxisValues.add(last_ten_item.get(i).getInternal_temp());

                                        }
                                        renderData();
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }

                            }

                            @Override
                            public void onError(Throwable e) {

                                Toast.makeText(getActivity(), "Try Later", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "onError: " + e.getMessage());
                            }
                        })
        );


    }


    List<Float> getList(List<String> stringList)
    {
        List<Float> floatList = new ArrayList<>();

        for (int i = 0; i < stringList.size(); i++)
        {
            Float number = Float.valueOf(stringList.get(i));
            floatList.add(number);
        }

        return floatList;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
       /* disposable.clear();*/
    }
}

