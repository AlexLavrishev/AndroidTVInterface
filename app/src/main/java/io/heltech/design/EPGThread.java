package io.heltech.design;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by shadow on 23/08/17.
 */

public class EPGThread extends Thread {
    Context ctx;
    public EPGThread(Context context) {
        super("EPGThread");
        System.out.println("Создан EPGThread  " + this);
        ctx = context;
    }

    @Override
    public void run() {
        super.run();
        try {
            while(true){
                LoadEPG();
                System.out.println("EPGThread  Program Guide is downloaded");
                Thread.sleep(300000);

            }
        } catch (InterruptedException e) {
            System.out.println("EPGThread  прерван");
        }
        System.out.println("EPGThread  завершён");
    }

    private void LoadEPG() {
        try
        {
            final String filename = "program.json";
            String u =  "http://ott.inmart.tv:9443/epg.php";
            final File file = new File(ctx.getFilesDir(), filename);
            URL url = new URL(u);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());


            String jsonData = IOUtils.toString(in, "UTF-8");

//            System.out.println("DATA: " + jsonData);
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                outputStream.write(jsonData.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch(Exception e){
        }

    }
}
