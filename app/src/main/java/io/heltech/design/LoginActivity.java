package io.heltech.design;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    String TAG  = "LoginActivity";
    Button loginBtn, infoBtn, forgotPassBtn;
    ImageButton backBtn;
    EditText loginField, passField;
    AlertDialog.Builder builder;
    AlertDialog alert;
    final String TOKEN = "token";
    final String LOGIN = "login";
    final String INFO = "information";
    final String FORGOT_PASS = "forgot_password";
    Context context;
    ChannelsDB dbHelper;
    Preference pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        dbHelper = new ChannelsDB(this);

        pref = new Preference(context);

        setContentView(R.layout.activity_login);

        loginBtn = (Button) findViewById(R.id.loginBtn);
        infoBtn = (Button) findViewById(R.id.infoBtn);
        backBtn = (ImageButton) findViewById(R.id.backBtn);
        forgotPassBtn = (Button) findViewById(R.id.forgotPassBtn);
        loginField = (EditText) findViewById(R.id.loginField);
        passField = (EditText) findViewById(R.id.passField);
        loginBtn.setOnClickListener(this);
        infoBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        forgotPassBtn.setOnClickListener(this);



    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id){
            case R.id.loginBtn:
                CheckAuth();
                break;
            case R.id.infoBtn:
                builder = new AlertDialog.Builder(new ContextThemeWrapper(LoginActivity.this, R.style.AlertDialogCustom));
                builder.setTitle("Информация")
                        .setMessage(pref.getInfoText())
                        .setCancelable(false)
                        .setNegativeButton("Закрыть окно",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                alert = builder.create();
                alert.show();
                break;
            case R.id.forgotPassBtn:
                builder = new AlertDialog.Builder(new ContextThemeWrapper(LoginActivity.this, R.style.AlertDialogCustom));
                builder.setTitle("Забыли пароль?")
                        .setMessage(pref.getForgotPassText())
                        .setCancelable(false)
                        .setNegativeButton("Закрыть окно",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                alert = builder.create();
                alert.show();
                break;

            case R.id.backBtn:
                finish();
                break;

            default:
                break;

        }
    }



    public String md5 (String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

    private void CheckAuth(){
        AsyncHttpClient client;
        final RequestHandle requestHandle;
        String lp = loginField.getText().toString() + passField.getText().toString();
        final String token = md5(lp);
        String playlist = "http://ott.inmart.tv/playlist?token=" + token;
        Log.d(TAG, "CheckAuth: "+ playlist);
        client = new AsyncHttpClient();
        requestHandle = client.get(playlist , new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Авторизация прошла успешно, загружаем плейлист пожалуйста подождите. ", Toast.LENGTH_SHORT);
                toast.show();
                Log.d(TAG, "onSuccess: "+ responseBody.toString());
                pref.setToken(token);
                pref.setLogin(loginField.getText().toString());
                String m3u = new String(responseBody);

                String lines[] = m3u.split("\\r?\\n");
                int lcn = 0;
                SQLiteDatabase database = dbHelper.getWritableDatabase();
                database.delete(ChannelsDB.TABLE_CHANNELS, null, null);
                ContentValues contentValues = new ContentValues();
                for (int i = 0; i < lines.length; i++) {
                    boolean flag = checkUrl(lines[i]);
                    if (flag) {
                        String url = lines[i];
                        String name = lines[i - 1].substring(lines[i - 1].lastIndexOf(",") + 2);
                        String logo = lines[i].substring(31, lines[i].length() - 50);
                        SaveImageFromUrl(logo);
                        Log.i(TAG, "onSuccess: " + name);
                        contentValues.put(ChannelsDB.KEY_NAME, name);
                        contentValues.put(ChannelsDB.KEY_LCN, lcn++);
                        contentValues.put(ChannelsDB.KEY_STREAM, url);
                        contentValues.put(ChannelsDB.KEY_LOGO, logo);
                        database.insert(ChannelsDB.TABLE_CHANNELS, null, contentValues);
                    }
                }
                finish();
            }
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Не правильные имя пользователя или пароль.", Toast.LENGTH_LONG);
                toast.show();
                Log.d(TAG, "onFailure: "+ responseBody.toString());
                loginField.setText("");
                passField.setText("");

            }
        });
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
}
