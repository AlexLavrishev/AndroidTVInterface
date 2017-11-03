package io.heltech.design;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

public class Settings extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "Settings: ";
    ImageButton finishBtn, deinterlaceBtn, bufferBtn;
    Button authBtn;
    Spinner deinterlace, buffer;
    TextView authText;
    Context ctx;
    Preference pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        finishBtn = (ImageButton)findViewById(R.id.finishBtn);
        deinterlaceBtn = (ImageButton)findViewById(R.id.deinterlaceBtn);
        bufferBtn = (ImageButton)findViewById(R.id.bufferBtn);
        authBtn = (Button)findViewById(R.id.authBtn);
        finishBtn.setOnClickListener(this);
        authBtn.setOnClickListener(this);
        deinterlaceBtn.setOnClickListener(this);
        bufferBtn.setOnClickListener(this);
        deinterlace = (Spinner) findViewById(R.id.deinterlace);
        ArrayAdapter<CharSequence> adapterDeinterlace = ArrayAdapter.createFromResource(this,
                R.array.deinterlace, android.R.layout.simple_spinner_item);
        adapterDeinterlace.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deinterlace.setAdapter(adapterDeinterlace);
        buffer = (Spinner) findViewById(R.id.buffer);
        ArrayAdapter<CharSequence> adapterBuffer = ArrayAdapter.createFromResource(this,
                R.array.buffer, android.R.layout.simple_spinner_item);
        adapterBuffer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        buffer.setAdapter(adapterBuffer);
        deinterlace.setOnItemSelectedListener(this);
        buffer.setOnItemSelectedListener(this);
        authText = (TextView) findViewById(R.id.authText);
        ctx = this;
        pref = new Preference(ctx);
        deinterlace.setSelection(pref.getDeinterlaceMode());
        buffer.setSelection(pref.getBufferMode());
    }


    @Override
    protected void onResume() {
        super.onResume();
        if ( pref.getLogin() != null &&  pref.getLogin().length() > 1 ){
            authText.setText("Вы авторизованы. Логин: " + pref.getLogin());
            authBtn.setText("Сменить пользователя");
        }else{
            authText.setText("Вы не авторизованы.");
            authBtn.setText("Авторизоваться");
        }
        deinterlace.setSelection(pref.getDeinterlaceMode());
        buffer.setSelection(pref.getBufferMode());
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.finishBtn:
                finish();
                break;

            case R.id.deinterlaceBtn:
                deinterlace.performClick();
                break;

            case R.id.bufferBtn:
                buffer.performClick();
                break;
            case R.id.authBtn:
                Intent intent;
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        int id = adapterView.getId();
        Log.i(TAG, "onItemSelected: " + id);
        switch(id){
            case R.id.deinterlace:
                Log.i(TAG, "onItemSelected deinterlace: " + i);
                pref.setDeinterlaceMode(i);
                break;
            case R.id.buffer:
                Log.i(TAG, "onItemSelected buffer: " + i);
                pref.setBufferMode(i);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}
