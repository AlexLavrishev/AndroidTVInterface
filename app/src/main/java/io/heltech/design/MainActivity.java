package io.heltech.design;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener{

    private static final String TAG = "MainActivity: ";
    RelativeLayout controlView;
    RelativeLayout mainView;
    SwipeRefreshLayout mySwipeRefreshLayout;
    boolean visibleFlag = true;
    ImageButton settingsBtn;

    String playlistURL = "http://ott.inmart.tv/playlist?token=2b94986d3122505eb9f17afe8b6dead8";
    List<Channel> list;
    LVAdapter adapter ;
    Context context;
    ListView listView ;
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
        listView = (ListView)findViewById(R.id.listview);

        settingsBtn = (ImageButton)findViewById(R.id.settings_btn);
        settingsBtn.setOnClickListener(this);
        controlView.setOnTouchListener(this);
        mainView.setOnTouchListener(this);





        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(TAG, "onItemClick: " + list.get(i).getName());

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

    public boolean checkUrl (String url) {
        String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(url);
        if (m.find()){
            return true;
        }else{
            return false;
        }
    }


    private void ReloadListViewAsync(){
        AsyncHttpClient client;
        final RequestHandle requestHandle;
        client = new AsyncHttpClient();
        requestHandle = client.get(playlistURL , new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                String m3u = new String(responseBody);
                String lines[] = m3u.split("\\r?\\n");
                List<Channel> listTmp = new ArrayList<Channel>();
                String path = String.valueOf(context.getFilesDir());
                for (int i = 0; i < lines.length; i++) {
                    boolean flag = checkUrl(lines[i]);
                    if (flag) {
                        String url = lines[i];
                        String name = lines[i - 1].substring(lines[i - 1].lastIndexOf(",") + 1);
                        String logo = lines[i].substring(31, lines[i].length() - 50);
                        listTmp.add(new Channel(0, name, url, path + "/logo" + logo + ".png" ));
                        SaveImageFromUrl(logo);
                    }
                }
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
                    bitmap = null;
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
        switch (id){
            case R.id.settings_btn:
                Log.i(TAG, "onClick: SettingsClick");
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
                break;
        }
    }
}
