package com.example.scmxpert.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.scmxpert.R;
import com.example.scmxpert.model.Shippment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class ShipmentAdapter extends RecyclerView.Adapter<ShipmentAdapter.MyViewHolder>{
    private List<Shippment> shipmentList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView shipment_id,from_place,deliver_place,
                status,created_date,delivery_date;

        public MyViewHolder(View view) {
            super(view);
            shipment_id = (TextView)view.findViewById(R.id.shippment_id);
            from_place = (TextView)view.findViewById(R.id.from_date);
            deliver_place = (TextView)view.findViewById(R.id.to_date);
            status = (TextView)view.findViewById(R.id.status);
            created_date = (TextView)view.findViewById(R.id.create_date);
            delivery_date = (TextView)view.findViewById(R.id.deliver_date);

        }
    }


    public ShipmentAdapter(List<Shippment> shipmentList) {
        this.shipmentList = shipmentList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shipment_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Shippment shippment = shipmentList.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if(shippment.getCreated_by()!=null && shippment.getDelivery_date()!=null){
                holder.created_date.setText(sdf1.format(sdf.parse(shippment.getCreated_date())));
                holder.delivery_date.setText(sdf1.format(sdf.parse(shippment.getDelivery_date())));
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.shipment_id.setText(shippment.getShipment_id());
        holder.from_place.setText("From-"+shippment.getRoute_form());
        holder.deliver_place.setText("To-"+shippment.getRoute_to());
        holder.status.setText("Status:"+shippment.getDelivery_status());
    }

    @Override
    public int getItemCount() {
        return shipmentList.size();
    }
}
