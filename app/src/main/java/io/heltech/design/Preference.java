package io.heltech.design;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shadow on 29/09/17.
 */

public class Preference {
    final String TOKEN = "token";
    final String LOGIN = "login";
    final String DEINTERLACE_MODE = "deinterlace";
    final String BUFFER_MODE = "buffer";
    final String INFO = "information";
    final String FORGOT_PASS = "forgot_password";
    final String CURRENT_CHANNEL = "current_channel";
    final String CURRENT_CHANNEL_INDEX = "current_channel_index";
    final String FIT_MODE = "fit_mode";
    public ArrayList<String> mode = new ArrayList<String>();



    private Context ctx;

    public Preference(Context ctx) {
        mode.add("Disabled");
        mode.add("Auto");
        mode.add("blend");
        mode.add("bob");
        mode.add("discard");
        mode.add("linear");
        mode.add("mean");
        mode.add("x");
        mode.add("yadif");
        mode.add("phosphor");
        mode.add("ivtc");
        this.ctx = ctx;
    }

    public String getToken(){
        return getString(TOKEN, ctx);
    }

    public String getLogin(){
        return getString(LOGIN, ctx);
    }
    public int getDeinterlaceMode(){
        return getInt(DEINTERLACE_MODE, ctx);
    }
    public int getBufferMode(){
        return getInt(BUFFER_MODE, ctx);
    }
    public String getInfoText(){
        return getString(INFO, ctx);
    }
    public String getForgotPassText(){
        return getString(FORGOT_PASS, ctx);
    }
    public boolean getFitMode() {
        if ( getInt(FIT_MODE, ctx) == 1)
            return true;
        else
            return false;
    }
    public String getCurrentChannel(){
        return getString(CURRENT_CHANNEL, ctx);
    }

    public int getCurrentChannelIndex() {
        return getInt(CURRENT_CHANNEL_INDEX, ctx);
    }

    public void setToken(String token){
        setString(TOKEN, token, ctx);
    }
    public void setLogin(String login){
        setString(LOGIN, login, ctx);
    }
    public void setDeinterlaceMode(int index){
        setInt(DEINTERLACE_MODE, index, ctx);
    }
    public void setBufferMode(int index){
        setInt(BUFFER_MODE, index, ctx);
    }
    public void setInfoText(String text){
        setString(INFO, text, ctx);
    }

    public void setFitMode(boolean mode){
        if (mode)
            setInt(FIT_MODE, 1, ctx);
        else
            setInt(FIT_MODE, 0, ctx);

    }

    public void setForgotPassText(String text){
        setString(FORGOT_PASS, text, ctx);
    }
    public void setCurrentChannel(String text, int id){
        setInt(CURRENT_CHANNEL_INDEX, id, ctx);
        setString(CURRENT_CHANNEL, text, ctx);
    }

    private static String getString(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }
    private static void setString(String key, String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }
    private static int getInt(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(key, 0);
    }
    private static void setInt(String key, int value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.commit();
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

}
