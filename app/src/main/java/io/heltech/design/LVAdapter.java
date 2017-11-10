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
import android.widget.ListView;
import android.widget.TextView;
import java.util.List;

import static android.content.ContentValues.TAG;


/**
 * Created by shadow on 03/08/17.
 */

public class LVAdapter extends BaseAdapter {
    private List<Channel> list;
    private LayoutInflater layoutInflater;
    private ListView lv;
    private static int selectedIndex;
    Preference pref ;
    public LVAdapter(Context context, List<Channel> list, ListView listView ) {
        this.list = list;
        this.lv = listView;

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
        Channel channel = getChannel(position);
        if (view == null){
            view = layoutInflater.inflate(R.layout.list_item, parent, false);
        }
        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(channel.getName());
        TextView url = (TextView) view.findViewById(R.id.url);
        url.setText(channel.getUrl());
        Bitmap img =  BitmapFactory.decodeFile(channel.getLogo());
        ImageView logo = (ImageView) view.findViewById(R.id.logo);
        logo.setImageBitmap(img);
//        view.setBackgroundResource(R.drawable.list_item_styles);
//        if (lv.isItemChecked(position)){
//            Log.i(TAG, "getView: " + position);
//            view.setBackgroundResource(R.drawable.list_item_styles_selected);
//            return view;
//        }

        if (position == selectedIndex){
            view.setBackgroundResource(R.drawable.list_item_styles_selected);

        }
        else{
            view.setBackgroundResource(R.drawable.list_item_styles);
        }




        return view;
    }

    public static void setSelectedIndex(int ind) {
        selectedIndex = ind;
    }

    private Channel getChannel(int position){
        return (Channel) getItem(position);
    }


}


