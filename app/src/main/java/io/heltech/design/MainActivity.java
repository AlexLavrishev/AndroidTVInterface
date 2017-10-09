package io.heltech.design;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener{

    private static final String TAG = "MainActivity: ";
    RelativeLayout controlView;
    RelativeLayout mainView;



    SwipeRefreshLayout mySwipeRefreshLayout;
    boolean visibleFlag = true;
    ImageButton settingsBtn;
    ImageButton fullviewBtn;


    List<Channel> list;
    LVAdapter adapter ;
    Context context;
    ListView listView ;
    ChannelsDB dbHelper;
    Preference pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        controlView = (RelativeLayout)findViewById(R.id.control_view);
        mainView = (RelativeLayout)findViewById(R.id.main_view);
        mySwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);
        context = this;
        pref = new Preference(context);
        listView = (ListView)findViewById(R.id.listview);
        settingsBtn = (ImageButton)findViewById(R.id.settings_btn);
        settingsBtn.setOnClickListener(this);
        fullviewBtn = (ImageButton)findViewById(R.id.fullview_btn);
        fullviewBtn.setOnClickListener(this);
        controlView.setOnTouchListener(this);
        mainView.setOnTouchListener(this);
        dbHelper = new ChannelsDB(this);
        InitListView();
        EPGThread myThread = new EPGThread(this);
        myThread.start();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(TAG, "onItemClick: " + list.get(i).getUrl());
            }
        });
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mySwipeRefreshLayout.setRefreshing(true);
                        ReloadListViewAsync();
                    }
                }
        );
    }


    @Override
    protected void onResume() {
        super.onResume();
        InitListView();
        Log.i(TAG, "onResume: ");
    }

    private void InitListView(){
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.query(ChannelsDB.TABLE_CHANNELS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(ChannelsDB.KEY_ID);
            int nameIndex = cursor.getColumnIndex(ChannelsDB.KEY_NAME);
            int streamIndex = cursor.getColumnIndex(ChannelsDB.KEY_STREAM);
            int logoIndex = cursor.getColumnIndex(ChannelsDB.KEY_LOGO);
            List<Channel> listTmp = new ArrayList<Channel>();
            String path = String.valueOf(context.getFilesDir());
            do {
                listTmp.add(new Channel(cursor.getInt(idIndex), cursor.getString(nameIndex), cursor.getString(streamIndex), path + "/logo" + cursor.getString(logoIndex) + ".png" ));
            } while (cursor.moveToNext());



            if (listTmp == null){
                Log.i(TAG, "InitListView: listTmp is null" );
                ReloadListViewAsync();
            }else{
                Log.i(TAG, "InitListView: listTmp is full" );
                list = listTmp;
                adapter  = new LVAdapter(context, list);
                listView.setAdapter(adapter);
                mySwipeRefreshLayout.setRefreshing(false);
            }

        }
        cursor.close();
    }

    private void ReloadListViewAsync(){
        AsyncHttpClient client;
        final RequestHandle requestHandle;
        client = new AsyncHttpClient();
        String playlistURL = "http://ott.inmart.tv/playlist?token=" + pref.getToken();
        Log.i(TAG, "ReloadListViewAsync: " + playlistURL);
        requestHandle = client.get(playlistURL , new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                String m3u = new String(responseBody);
                String lines[] = m3u.split("\\r?\\n");
                List<Channel> listTmp = new ArrayList<Channel>();
                String path = String.valueOf(context.getFilesDir());
//
                int count=0;
                int lcn = 0;
                SQLiteDatabase database = dbHelper.getWritableDatabase();
                database.delete(ChannelsDB.TABLE_CHANNELS, null, null);
                ContentValues contentValues = new ContentValues();
                for (int i = 0; i < lines.length; i++) {
                    boolean flag = pref.checkUrl(lines[i]);
                    if (flag) {
                        String url = lines[i];
                        String name = lines[i - 1].substring(lines[i - 1].lastIndexOf(",") + 2);
                        String logo = lines[i].substring(31, lines[i].length() - 50);


                        listTmp.add(new Channel(0, name, url, path + "/logo" + logo + ".png" ));
                        SaveImageFromUrl(logo);

                        contentValues.put(ChannelsDB.KEY_NAME, name);
                        contentValues.put(ChannelsDB.KEY_LCN, lcn++);
                        contentValues.put(ChannelsDB.KEY_STREAM, url);
                        contentValues.put(ChannelsDB.KEY_LOGO, logo);
                        database.insert(ChannelsDB.TABLE_CHANNELS, null, contentValues);


                    }
                }
                Log.i(TAG, "onSuccess: Count  - " + count);
                list = listTmp;
                adapter  = new LVAdapter(context, list);
                listView.setAdapter(adapter);
                mySwipeRefreshLayout.setRefreshing(false);


            }
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
            }
        });
    }



    private void SaveImageFromUrl(final String logo) {
        final String filename = "logo" + logo + ".png";
        final String URL = "http://ott.inmart.tv/logo/"+logo+".png";
        final File file = new File(this.getFilesDir(), filename);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = null;
                try {
                    InputStream input = new java.net.URL(URL).openStream();
                    bitmap = BitmapFactory.decodeStream(input);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                if (bitmap == null){
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.movie);
                }
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                try {
                    FileOutputStream myFileOutputStream = new FileOutputStream(file);
                    myFileOutputStream.write(byteArray);
                    myFileOutputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread backThread = new Thread(r);
        backThread.start();
    }





    private void ToggleView(){
        if (visibleFlag){
            Animation animation1 = new AlphaAnimation(1.0f, 0.0f);
            animation1.setDuration(500);
            controlView.startAnimation(animation1);
            controlView.setVisibility(View.INVISIBLE);
            visibleFlag = false;
        }else{
            controlView.setVisibility(View.VISIBLE);
            Animation animation1 = new AlphaAnimation(0.0f, 1.0f);
            animation1.setDuration(200);
            controlView.startAnimation(animation1);
            visibleFlag = true;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int id  = view.getId();
        if ( id == R.id.main_view && motionEvent.getAction() == MotionEvent.ACTION_DOWN){
            ToggleView();
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        int id  = view.getId();
        Intent intent;
        switch (id){
            case R.id.settings_btn:
                Log.i(TAG, "onClick: SettingsClick");
                intent = new Intent(this, Settings.class);
                startActivity(intent);
                break;
            case R.id.fullview_btn:
                Log.i(TAG, "onClick: SettingsClick");
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;
        }
    }
}
