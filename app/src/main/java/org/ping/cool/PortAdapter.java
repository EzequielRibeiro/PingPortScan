package org.ping.cool;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.ping.cool.utils.Port;

import java.util.ArrayList;

public class PortAdapter extends ArrayAdapter<Port> {

    private Context context;

    public PortAdapter(Context context, ArrayList<Port> ports) {
        super(context, 0, ports);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Port port = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.port_list, parent, false);
        }

        TextView textViewProtocol = (TextView) convertView.findViewById(R.id.textViewPortProtocol);
        TextView textViewService = (TextView) convertView.findViewById(R.id.textViewPorService);
        TextView textViewNumber = (TextView) convertView.findViewById(R.id.textViewPortNumber);
        TextView textViewPortISOpen = (TextView) convertView.findViewById(R.id.textViewPortIsOpen);
        textViewProtocol.setText(port.getPortProtocol());
        textViewService.setText(port.getPortService());
        textViewNumber.setText(String.valueOf(port.getPort()));

        if (port.isOpen()) {
             textViewPortISOpen.setText(Html.fromHtml("<font color='green'>Open</font>"));
        }else {
             textViewPortISOpen.setText(Html.fromHtml("<font color='red'>Closed</font>"));

        }

        return convertView;
    }
}
