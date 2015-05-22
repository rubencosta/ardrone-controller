package com.example.utilizador.ass5;

import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by utilizador on 19/05/2015.
 */
public class Settings {
    public static String SERVER_IP = "serverIP";
    public static String SERVER_PORT = "serverPort";

    public static String FLYING_MODE = "_flying_mode";
    public static String VERTICAL_SPEED = "vertical_speed";
    public static String HORIZONTAL_SPEED = "horizontal_speed";
    public static String ROTATION_SPEED = "rotation_speed";

    private static ConfPair[] keyArray = {
            new ConfPair(SERVER_IP, "192.168.1.3"),
            new ConfPair(SERVER_PORT, "3001"),

            new ConfPair(FLYING_MODE, "normal"),
            new ConfPair(VERTICAL_SPEED, "1"),
            new ConfPair(HORIZONTAL_SPEED, "1"),
            new ConfPair(ROTATION_SPEED, "1")
    };

    private static ArrayList<ConfPair> data = new ArrayList<ConfPair>();
    private static SharedPreferences _sharedPref;

    public static void set(ConfPair confPair)
    {
        if(data.contains(confPair)) {
            data.get(data.indexOf(confPair)).value = confPair.value;
        }
        else {
            data.add(confPair);
        }
    }

    public static void set(String key, String value)
    {
        set(new ConfPair(key, value));
    }

    public static String get(ConfPair confPair)
    {
        if(data.contains(confPair)) {
            return data.get(data.indexOf(confPair)).value;
        }
        return null;
    }

    public static <T> T get(ConfPair confPair, Class<T> c)
    {
        if(data.contains(confPair)) {
            if(c == int.class)
                return (T)((Object)(Integer.parseInt(data.get(data.indexOf(confPair)).value)));
            if(c == float.class)
                return (T)((Object)(Float.parseFloat(data.get(data.indexOf(confPair)).value)));
            if(c == boolean.class)
                return (T)((Object)(Boolean.parseBoolean(data.get(data.indexOf(confPair)).value)));
            return (T)((Object)(data.get(data.indexOf(confPair)).value));
        }
        return null;
    }

    public static String get(String key)
    {
        return get(new ConfPair(key, ""), String.class);
    }

    public static <T> T get(String key, Class<T> c)
    {
        return get(new ConfPair(key, ""), c);
    }

    public static void set_sharedPref(SharedPreferences pref)
    {
        _sharedPref = pref;
    }

    public static void loadConfig(){
        data.clear();
        for(ConfPair confPair : keyArray) {
            String value = _sharedPref.getString(confPair.key, "error_not_found");
            if(value == "error_not_found") {
                data.add(new ConfPair(confPair.key, confPair.value));
                Log.i("Config load", confPair.key + "=" + confPair.value);
            }
            else {
                data.add(new ConfPair(confPair.key, value));
                Log.i("Config load", confPair.key + "=" + value);
            }
        }
    }

    public static void saveConfig(){
        SharedPreferences.Editor editor = _sharedPref.edit();

        for(ConfPair confPair : keyArray) {
            editor.putString(confPair.key, get(confPair.key));
            Log.i("Config save", confPair.key + "=" + get(confPair.key));
        }
        editor.commit();
    }

    public static class ConfPair
    {
        public String key;
        public String value;

        public ConfPair(String aKey, String aValue)
        {
            key = aKey;
            value = aValue;
        }

        @Override
        public boolean equals(Object o) {
            return ((ConfPair)o).key == key;
        }
    }

}
