package io.heltech.design;

import android.app.LauncherActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


public class MainActivity extends AppCompatActivity implements IVLCVout.Callback, MediaPlayer.EventListener , View.OnTouchListener, View.OnClickListener, ListView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "MainActivity: ";
    RelativeLayout controlView;
    FrameLayout mainView;
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
    float x1,x2;
    float y1,y2;
    static final int MINX_DISTANCE = 150;
    static final int MINY_DISTANCE = 100;
    int currentChannelIndex = 0;
    //////VLC
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private LibVLC libvlc = null;
    private MediaPlayer mediaPlayer = null;
    private IVLCVout ivlcVout;
    private Media media;
    String url;
    boolean FLAG_CREATED_SURFACE;

    Runnable mVolRunnable;
    Handler mVolHandler;
    int HIDE_TIME = 10000; // seconds
    int videoWidth, videoHeight;
    int videoVisibleWidth, videoVisibleHeight;
    int mSarDen, mSarNum;

    boolean IS_SURFACE_FIT;
    View listItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        context = this;
        pref = new Preference(context);
        dbHelper = new ChannelsDB(this);
        if ( pref.getLogin() == null ){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        mainView    = (FrameLayout)findViewById(R.id.fullScreenFrame);
        listView    = (ListView)findViewById(R.id.listview);
        settingsBtn = (ImageButton)findViewById(R.id.settings_btn);
        fullviewBtn = (ImageButton)findViewById(R.id.fullview_btn);
        surfaceView = (SurfaceView) findViewById(R.id.player_surface);
        controlView = (RelativeLayout)findViewById(R.id.control_view);
        mySwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);

        mainView.setOnTouchListener(this);
        listView.setOnItemClickListener(this);
        listView.setOnTouchListener(this);
        settingsBtn.setOnClickListener(this);
        fullviewBtn.setOnClickListener(this);
        controlView.setOnTouchListener(this);
        mySwipeRefreshLayout.setOnRefreshListener(this);
        FLAG_CREATED_SURFACE = true;
        mVolHandler = new Handler();

        mVolRunnable = new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Animation animation1 = new AlphaAnimation(1.0f, 0.0f);
                animation1.setDuration(500);
                controlView.startAnimation(animation1);
                controlView.setVisibility(View.INVISIBLE);
                visibleFlag = false;
            }
        };
        mVolHandler.postDelayed(mVolRunnable, HIDE_TIME);

    }


    @Override
    protected void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
        InitListView();
        FLAG_CREATED_SURFACE = true;
        url = pref.getCurrentChannel();
        IS_SURFACE_FIT = pref.getFitMode();
        initPlayer();
        InitInformation();
        currentChannelIndex = pref.getCurrentChannelIndex();
        Log.i(TAG, "run: " + listView.getChildCount());
//        listView.getChildAt(currentChannelIndex).setBackgroundResource(R.drawable.list_item_styles_selected);



    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int id  = view.getId();
        if ( id == R.id.fullScreenFrame ){

            switch(motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    x1 = motionEvent.getX();
                    y1 = motionEvent.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    x2 = motionEvent.getX();
                    y2 = motionEvent.getY();
                    float deltaX = x2 - x1;
                    float deltaY = y2 - y1;

                    if (deltaX > MINX_DISTANCE && Math.abs(deltaY) < MINY_DISTANCE && !visibleFlag) {
                        PreviousChannel();
                    }else if( Math.abs(deltaX) > MINX_DISTANCE && Math.abs(deltaY) < MINY_DISTANCE && !visibleFlag){
                        NextChannel();
                    }else if( Math.abs(deltaY) > MINY_DISTANCE ){
                        // up down swipe
                    }else{
                        ToggleView();
                    }
                    break;
            }
            return true;
        }
        if ( ( id == R.id.listview  ) && motionEvent.getAction() == MotionEvent.ACTION_DOWN){
            mVolHandler.removeCallbacks(mVolRunnable);
            mVolHandler.postDelayed(mVolRunnable, HIDE_TIME);
        }
        return false;
    }


    private void PreviousChannel(){
        currentChannelIndex--;
        if ( currentChannelIndex < 0 ){
            currentChannelIndex = list.size() - 1;

        }
        pref.setCurrentChannel(list.get(currentChannelIndex).getUrl(), currentChannelIndex);
        PlayChannel();
    }

    private void NextChannel(){
        currentChannelIndex++;
        if ( currentChannelIndex > (list.size() - 1) ){
            currentChannelIndex = 0;

        }
        pref.setCurrentChannel(list.get(currentChannelIndex).getUrl(), currentChannelIndex);
        PlayChannel();
    }

    private void PlayChannel(){
        url = pref.getCurrentChannel();
        Toast.makeText(this, list.get(currentChannelIndex).getName(), Toast.LENGTH_SHORT).show ();
        mediaPlayer.stop();
        media = new Media(libvlc, Uri.parse(url));
        media.setHWDecoderEnabled(true, true);
        mediaPlayer.setMedia(media);
        mediaPlayer.play();
        setScreenSize(IS_SURFACE_FIT);
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
                IS_SURFACE_FIT = !IS_SURFACE_FIT;
                pref.setFitMode(IS_SURFACE_FIT);
                setScreenSize(IS_SURFACE_FIT);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

//        Log.i(TAG, "onItemClick: !!!" + view);
//
//        if  (listItem != null){
//
//            listItem.setBackgroundResource(R.drawable.list_item_styles);
//        }else{
//            Log.i(TAG, "onItemClick: listItem is null!!!");
//        }
//        view.setBackgroundResource(R.drawable.list_item_styles_selected);
//        listItem = view;
        pref.setCurrentChannel(list.get(i).getUrl(), i);
        currentChannelIndex = i;

        PlayChannel();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setScreenSize(IS_SURFACE_FIT);
    }

    @Override
    public void onRefresh() {
        mySwipeRefreshLayout.setRefreshing(true);
        ReloadListViewAsync();
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
                adapter  = new LVAdapter(context, list, listView);
                listView.setAdapter(adapter);
                mySwipeRefreshLayout.setRefreshing(false);
            }
            if ( pref.getCurrentChannel() == null){
                pref.setCurrentChannel(list.get(0).getUrl(), 0);
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
                adapter  = new LVAdapter(context, list, listView);
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
            mVolHandler.removeCallbacks(mVolRunnable);
        }else{
            controlView.setVisibility(View.VISIBLE);
            Animation animation1 = new AlphaAnimation(0.0f, 1.0f);
            animation1.setDuration(200);
            controlView.startAnimation(animation1);
            visibleFlag = true;

            mVolHandler.removeCallbacks(mVolRunnable);
            mVolHandler.postDelayed(mVolRunnable, HIDE_TIME);
        }
    }

    private void InitInformation(){
        String url = "http://ott.inmart.tv/info/info.json";
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // TODO Auto-generated method stub
                String info = null;
                String forgot = null;
                try {
                    info = response.getString("information") ;
                } catch (JSONException e) {
                    e.printStackTrace();
                    info = "";
                }
                try {
                    forgot = response.getString("forgotPassword") ;
                } catch (JSONException e) {
                    e.printStackTrace();
                    forgot = "";
                }
                pref.setInfoText(info);
                pref.setForgotPassText(forgot);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Log.i(TAG, "onErrorResponses: " + error.toString());
            }
        });
        queue.add(jsObjRequest);
    }

    ///// VLC
    private void initPlayer() {

        if (url == null){
            Log.i(TAG, "initPlayer: Empty playlist");
            return;
        }else{
            Log.i(TAG, "initPlayer: " + url);
        }
        ArrayList<String> options = new ArrayList<>();
        options.add("--aout=opensles");
        options.add("--audio-time-stretch");
        options.add("--avcodec-hw=any");
//        options.add("-vvvvv");
        options.add("--no-sub-autodetect-file");

//        options.add("--network-caching=60000");

        int networkCachingMode = pref.getBufferMode();
        switch  (networkCachingMode){
            case 0:
                break;
            case 1:
                options.add("--network-caching=5000");
                break;
            case 2:
                options.add("--network-caching=15000");
                break;
            case 3:
                options.add("--network-caching=60000");
                break;
        }
        Log.i(TAG, "initPlayer: networkCachingMode" + networkCachingMode);
        int indexMode = pref.getDeinterlaceMode();
        Log.i(TAG, "initPlayer deinterlace mode: " + pref.mode.get(indexMode));
        if  (indexMode == 0 ){
            options.add("--deinterlace=0");
        }else if(indexMode == 1) {
            options.add("--deinterlace=-1");
        }else{
            options.add("--deinterlace=1");
            options.add("--sout-deinterlace-mode=" + pref.mode.get(indexMode) );
            options.add("--deinterlace-mode=" + pref.mode.get(indexMode) );
            options.add("--video-filter=deinterlace");
        }


        libvlc = new LibVLC(MainActivity.this, options);

        if (FLAG_CREATED_SURFACE){
            Log.i(TAG, "initPlayer: first player init");

            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.setKeepScreenOn(true);
            FLAG_CREATED_SURFACE = false;
        }

        mediaPlayer = new MediaPlayer(libvlc);
        media = new Media(libvlc, Uri.parse(url));
        media.setHWDecoderEnabled(true, true);
        mediaPlayer.setMedia(media);
        ivlcVout = mediaPlayer.getVLCVout();
        ivlcVout.setVideoView(surfaceView);
        ivlcVout.addCallback(this);
        ivlcVout.attachViews();
        mediaPlayer.play();
        mediaPlayer.setEventListener(this);



    }

    @Override
    public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        mSarDen = sarDen;
        mSarNum = sarNum;
        videoWidth = width;
        videoHeight = height;
        videoVisibleWidth = visibleWidth;
        videoVisibleHeight = visibleHeight;
        setScreenSize(IS_SURFACE_FIT);
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {
        Log.i(TAG, "onSurfacesCreated: ");
    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {
        Log.i(TAG, "onSurfacesDestroyed: ");
        mediaPlayer.stop();
//        libvlc = null;
//        surfaceHolder = null;
    }

    @Override
    public void onHardwareAccelerationError(IVLCVout vlcVout) {
        Log.i(TAG, "onHardwareAccelerationError: ");
    }

    private void setScreenSize( boolean mode ) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int sh = displayMetrics.heightPixels;
        int sw = displayMetrics.widthPixels;
        if (mediaPlayer != null) {
            final IVLCVout vlcVout = mediaPlayer.getVLCVout();
            vlcVout.setWindowSize(sw, sh);
        }
        double dw = sw, dh = sh;
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh;
            dh = sw;
        }
        // sanity check
        if (dw * dh == 0 || videoWidth * videoHeight == 0) {
            Log.e(TAG, "Invalid surface size");
            return;
        }

        double ar, vw;
        if (mSarDen == mSarNum) {
        /* No indication about the density, assuming 1:1 */
            vw = videoVisibleWidth;
            ar = (double)videoVisibleWidth / (double)videoVisibleHeight;
        } else {
        /* Use the specified aspect ratio */
            vw = videoVisibleWidth * (double)mSarNum / mSarDen;
            ar = vw / videoVisibleHeight;
        }
        double dar = dw / dh;
        // compute the display aspect ratio
        if (IS_SURFACE_FIT){
                dh = dw / ar;
        }else{
            if (dar < ar)
                dh = dw / ar;
            else
                dw = dh * ar;
        }


        LayoutParams lp = surfaceView.getLayoutParams();
        lp.width  = (int) Math.ceil(dw * videoWidth / videoVisibleWidth);
        lp.height = (int) Math.ceil(dh * videoHeight / videoVisibleHeight);
        surfaceView.setLayoutParams(lp);
    }

    @Override
    public void onEvent(MediaPlayer.Event event) {

        switch  (event.type){
            case MediaPlayer.Event.Opening:
                break;
            case MediaPlayer.Event.Playing:
                break;
            case MediaPlayer.Event.EncounteredError:
                Log.i(TAG, "onEvent Playing");
                Toast.makeText(this, "Ошибка открытия канала, попробуйте позже", Toast.LENGTH_LONG).show ();

                Handler nextChHandler = new Handler();
                Runnable nextCh = new Runnable() {
                    @Override
                    public void run() {
                        NextChannel();
                    }
                };
                nextChHandler.postDelayed(nextCh, 4000);
                break;

        }
    }
}
