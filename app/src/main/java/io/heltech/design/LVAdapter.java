package io.heltech.design;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

import static android.content.ContentValues.TAG;


/**
 * Created by shadow on 03/08/17.
 */

public class LVAdapter extends BaseAdapter {
    private List<Channel> list;
    private LayoutInflater layoutInflater;
    public LVAdapter(Context context, List<Channel> list) {
        this.list = list;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;


        if (view == null){
            view = layoutInflater.inflate(R.layout.list_item, parent, false);
        }

        Channel channel = getChannel(position);

        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(channel.getName());
        TextView url = (TextView) view.findViewById(R.id.url);
        url.setText(channel.getUrl());

        Bitmap img =  BitmapFactory.decodeFile(channel.getLogo());
        ImageView logo = (ImageView) view.findViewById(R.id.logo);
        logo.setImageBitmap(img);
        return view;
    }

    private Channel getChannel(int position){
        return (Channel) getItem(position);
    }


}


