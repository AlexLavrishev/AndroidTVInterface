package io.heltech.design;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by shadow on 03/08/17.
 */

public class LVAdapter extends BaseAdapter {
    Typeface face;
    private List<Channel> list;
    private LayoutInflater layoutInflater;
    public LandscapeListViewAdapter(Context context, List<Channel> list) {
        this.list = list;
        face= Typeface.createFromAsset(context.getAssets(), "fonts/thin.ttf");
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
        TextView name = (TextView) view.findViewById(R.id.channelName);
        name.setText(channel.getName());
        name.setTextColor(Color.parseColor("#484848"));
        name.setTypeface(face);
        TextView  stream = (TextView) view.findViewById(R.id.stream);
        stream.setText(channel.getStream());
        stream.setVisibility(View.INVISIBLE);
        Bitmap img =  BitmapFactory.decodeByteArray(channel.getLogo(), 0, channel.getLogo().length);
        ImageView logo = (ImageView) view.findViewById(R.id.icon);
        logo.setImageBitmap(img);
        view.findViewById(R.id.channelName);
        return view;
    }

    private Channel getChannel(int position){
        return (Channel) getItem(position);
    }
}
