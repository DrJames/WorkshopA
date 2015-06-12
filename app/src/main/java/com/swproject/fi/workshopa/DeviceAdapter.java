package com.swproject.fi.workshopa;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by alex on 12.6.2015.
 */
public class DeviceAdapter extends ArrayAdapter<BluetoothDevice> {
    private List<BluetoothDevice> devList;
    private Context context;
    private BluetoothDevice device;
    private BluetoothAdapter adapter;
    private int layoutResourceId;
    private int cross;
    private OnCrossClickedListener crossListener;

    public DeviceAdapter(Context context, int layoutResourceId, List<BluetoothDevice> dev){
        super(context, layoutResourceId, dev);
        this.layoutResourceId = layoutResourceId;
        this.devList = dev;
        this.context = context;
        //this.cross = cross;
        adapter = BluetoothAdapter.getDefaultAdapter();

    }

    public interface OnCrossClickedListener {
        public void onCrossClicked(final int position, BluetoothDevice device);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        View row = convertView;
        DevHolder holder;

        if (row == null){
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new DevHolder();
            holder.name = (TextView) row.findViewById(R.id.devName);
            holder.cross = (ImageView) row.findViewById(R.id.devClose);
            row.setTag(holder);
        }
        else {
            holder = (DevHolder) row.getTag();
        }
        final BluetoothDevice devs = devList.get(position);

        holder.name.setText(devs.getName());
        holder.cross.setId(position);

        holder.cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crossListener = (OnCrossClickedListener) context;
                crossListener.onCrossClicked(position, devs);
                //devList.remove(devs);
                //notifyDataSetChanged();


            }
        });
        return row;
    }

    static class DevHolder{
        TextView name;
        ImageView cross;
    }

}
