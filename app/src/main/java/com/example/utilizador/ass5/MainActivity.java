package com.example.utilizador.ass5;

import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static final String FLIP = "FLIP";
    private static final String PHI = "Phi";
    private static final String MIX = "MIX";
    private static final String THETA = "Theta";
    private static final String DANCE = "Dance";
    private static final String TURN = "Turn";

    private float _x = 0;
    private float _y = 0;

    private float lastX = 0;
    private float lastY = 0;

    float[] gravity = {0, 0, 0};
    float[] linear_acceleration = {0, 0, 0};
    final float alpha = 0.8f;

    private SensorManager _sensorManager;

    private AccelerometerSensorListener acc_listener = new AccelerometerSensorListener();

    Button b;
    Socket socket = null;
    DataOutputStream dataOutputStream = null;
    DataInputStream dataInputStream = null;
    String serverIPAdress = "192.168.173.50";
    int serverPort = 3001;

    Boolean _isControl = false;
    Button _control;

    byte[] response = new byte[256];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Sensor accSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accSensor != null)
            _sensorManager.registerListener(acc_listener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);

        TabHost tab = (TabHost) findViewById(R.id.tabHost);

        tab.setup();

        TabHost.TabSpec tabSpec = tab.newTabSpec("Fligth Mode");
        tabSpec.setContent(R.id.tabFly);
        tabSpec.setIndicator("Fligth Mode");
        tab.addTab(tabSpec);

        tabSpec = tab.newTabSpec("Closer Mode");
        tabSpec.setContent(R.id.tabCloser);
        tabSpec.setIndicator("Closer Mode");
        tab.addTab(tabSpec);

        tabSpec = tab.newTabSpec("Tricks");
        tabSpec.setContent(R.id.Tricks);
        tabSpec.setIndicator("Tricks Mode");
        tab.addTab(tabSpec);

        //b = (Button)findViewById(R.id.takeoff_btn);

        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                try {
                    socket = new Socket(serverIPAdress, serverPort);
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    dataInputStream = new DataInputStream(socket.getInputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();

        Button rotateLeft = (Button) findViewById(R.id.left_btn);

        rotateLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    new CommandWorkerThread("[\"clockwise\",[0.7],1]\n").start();
                    Log.i("Button", "Clockwise");
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    new CommandWorkerThread("[\"stop\",[],1]\n").start();
                    Log.i("Button", "Clockwise-stop");

                }
                return false;
            }
        });

        Button rotateRight = (Button) findViewById(R.id.right_btn);

        rotateRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    new CommandWorkerThread("[\"counterClockwise\",[0.7],1]\n").start();
                    Log.i("Button", "counterClockwise");
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    new CommandWorkerThread("[\"stop\",[],1]\n").start();
                    Log.i("Button", "counterClockwise-stop");
                }
                return false;
            }
        });

        Button up = (Button) findViewById(R.id.up_btn);

        up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    new CommandWorkerThread("[\"up\",[0.7],2]\n").start();
                    Log.i("Button", "up");
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    new CommandWorkerThread("[\"stop\",[],1]\n").start();
                    Log.i("Button", "up stop");
                }
                return false;
            }
        });

        Button down = (Button) findViewById(R.id.down_btn);

        down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    new CommandWorkerThread("[\"down\",[0.7],1]\n").start();
                    Log.i("Button", "down");
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    new CommandWorkerThread("[\"stop\",[],1]\n").start();
                    Log.i("Button", "up stop");
                }
                return false;
            }
        });

        _control = (Button) findViewById(R.id.control_btn);

        _control.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    _isControl = true;
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    _isControl = false;
                    new CommandWorkerThread("[\"stop\",[],1]\n").start();

                }
                return false;
            }
        });


    }

    public void onTakeOffClick(View v) {
        new CommandWorkerThread("[\"calibrate\",[],1]\n").start();
        Button land = (Button) findViewById(R.id.land_btn);
        Button takeOff = (Button) findViewById(R.id.takeoff_btn);
        new CommandWorkerThread("[\"takeoff\",[],1]\n").start();
        takeOff.setVisibility(View.INVISIBLE);
        land.setVisibility(View.VISIBLE);

    }

    public void onFlipAheadClick(View v) {
        Random ran = new Random();
        int num = ran.nextInt(4);
        Log.i(FLIP, String.valueOf(num));

        if (num == 0)
            new CommandWorkerThread("[\"animate\",[\"flipAhead\",1000] ,2]\n").start();
        else if (num == 1)
            new CommandWorkerThread("[\"animate\",[\"flipBehind\",1000] ,2]\n").start();
        else if (num == 2)
            new CommandWorkerThread("[\"animate\",[\"flipLeft\",1000] ,2]\n").start();
        else new CommandWorkerThread("[\"animate\",[\"flipRight\",1000] ,2]\n").start();
    }

    public void onPhiM30DegClick(View v) {
        Random ran = new Random();
        int num = ran.nextInt(1);
        Log.i(PHI, String.valueOf(num));

        if (num == 0)
            new CommandWorkerThread("[\"animate\",[\"phiM30Deg\",1000] ,2]\n").start();
        else
            new CommandWorkerThread("[\"animate\",[\"phi30Deg\",1000] ,2]\n").start();
    }


    public void onThetaClick(View v) {
        Random ran = new Random();
        int num = ran.nextInt(4);
        Log.i(THETA, String.valueOf(num));

        if (num == 0)
            new CommandWorkerThread("[\"animate\",[\"thetaM30Deg\",1000] ,2]\n").start();
        else if (num == 1)
            new CommandWorkerThread("[\"animate\",[\"theta30Deg\",1000] ,2]\n").start();
        else if (num == 2)
            new CommandWorkerThread("[\"animate\",[\"theta20degYaw200deg\",1000] ,2]\n").start();
        else new CommandWorkerThread("[\"animate\",[\"theta20degYawM200deg\",1000] ,2]\n").start();
    }

    public void onTurnClick(View v) {
        Random ran = new Random();
        int num = ran.nextInt(1);
        Log.i(TURN, String.valueOf(num));

        if (num == 0)
            new CommandWorkerThread("[\"animate\",[\"turnaround\",1000] ,2]\n").start();
        else
            new CommandWorkerThread("[\"animate\",[\"turnaroundGodown\",1000] ,2]\n").start();
    }

    public void onDanceClick(View v) {
        Random ran = new Random();
        int num = ran.nextInt(5);
        Log.i(DANCE, String.valueOf(num));

        if (num == 0)
            new CommandWorkerThread("[\"animate\",[\"yawShake\",1000] ,2]\n").start();
        else if (num == 1)
            new CommandWorkerThread("[\"animate\",[\"yawDance\",1000] ,2]\n").start();
        else if (num == 2)
            new CommandWorkerThread("[\"animate\",[\"thetaDance\",1000] ,2]\n").start();
        else if (num == 3)
            new CommandWorkerThread("[\"animate\",[\"vzDance\",1000] ,2]\n").start();
        else new CommandWorkerThread("[\"animate\",[\"wave\",1000] ,2]\n").start();
    }

    public void onMixedClick(View v) {
        Random ran = new Random();
        int num = ran.nextInt(1);
        Log.i(MIX, String.valueOf(num));

        if (num == 0)
            new CommandWorkerThread("[\"animate\",[\"phiThetaMixed\",1000] ,2]\n").start();
        else
            new CommandWorkerThread("[\"animate\",[\"doublePhiThetaMixed\",1000] ,2]\n").start();
    }


    public void onControlClick(View v) {

    }

    /*public void onLeftClick(View v){
        new CommandWorkerThread("[\"left\",[0.2],2]\n").start();
    }


    public void onRightClick(View v){
        new CommandWorkerThread("[\"right\",[0.2],2]\n").start();
    }
*/
    public void landCommandClick(View v) {
        new CommandWorkerThread("[\"land\",[],1]\n").start();
        Button land = (Button) findViewById(R.id.land_btn);
        Button takeOff = (Button) findViewById(R.id.takeoff_btn);
        land.setVisibility(View.INVISIBLE);
        takeOff.setVisibility(View.VISIBLE);

    }
    /*

    public void onCalibrateDroneClick(View v){
        new CommandWorkerThread("[\"calibrate\",[],1]\n").start();
    }

    public void onUpClick(View v){
        new CommandWorkerThread("[\"up\",[0.2],2]\n").start();

    }

    public void onDownClick(View v){
        new CommandWorkerThread("[\"down\",[0.2],1]\n").start();
    }

    public void onFrontClick(View v){
        new CommandWorkerThread("[\"front\",[0.2],2]\n").start();

    }

    public void onBackClick(View v){
        new CommandWorkerThread("[\"back\",[0.2],2]\n").start();

    }

    public void onStopClick(View v){
        new CommandWorkerThread("[\"stop\",[],1]\n").start();
    }

    public void onTestVideoStreamClick(View v){
        Intent i = new Intent(this, VideoViewActivity.class);
        startActivity(i);
    }*/


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (dataOutputStream != null) {
            try {
                dataOutputStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (dataInputStream != null) {
            try {
                dataInputStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    private class CommandWorkerThread extends Thread {

        private String _command = "";

        public CommandWorkerThread(String command) {
            _command = command;
        }

        @Override
        public void run() {
            try {

                Log.i(TAG, "sending request");
                dataOutputStream.writeBytes(_command);
                dataInputStream.readFully(response);
                dataOutputStream.flush();

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class AccelerometerSensorListener implements SensorEventListener {

        private float x;
        private float y;

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2];

            // Remove the gravity contribution with the high-pass filter.
            linear_acceleration[0] = sensorEvent.values[0] - gravity[0];
            linear_acceleration[1] = sensorEvent.values[1] - gravity[1];
            linear_acceleration[2] = sensorEvent.values[2] - gravity[2];


            _x = (float) ((gravity[0]) * 0.1);
            _y = (float) ((gravity[1]) * 0.1);

            //Log.i("Sensor Listener", "gravity "+x + " y: " + y); //[0] =x, frente-tras ; [1] = y, esquerda-direita
            //Log.i("Sensor Listener", "linear_acceleration "+linear_acceleration[0]+" "+linear_acceleration[1]+" "+linear_acceleration[2]);
            if(_isControl){
                if(_x >= 0) {
                    new CommandWorkerThread("[\"back\",[" + _x + "],2]\n").start();
                    Log.i("Gravity", "back = " + _x);
                }

                else {
                    _x= Math.abs(_x);
                    new CommandWorkerThread("[\"front\",[" + _x + "],2]\n").start();
                    Log.i("Gravity", "front = " + _x);
                }

                if( _y >= 0) {
                    new CommandWorkerThread("[\"right\",[" + _y + "],2]\n").start();
                    Log.i("Gravity", "Right = " + _y);
                }
                else{
                    _y= Math.abs(_y);
                    new CommandWorkerThread("[\"left\",[" + _y + "],2]\n").start();
                    Log.i("Gravity", "left = " + _y);
                }
            }


        }
    }

    public static String SERVER_IP = "serverIP";
    public static String SERVER_PORT = "serverPort";
    public static String SWIPE_THRESHOLD = "_swipe_threshold";
    public static String MAXIMUM_INTERVAL = "_maximum_interval";
    public static String FLYING_MODE = "_flying_mode";
    public static String VERTICAL_SPEED = "vertical_speed";
    public static String HORIZONTAL_SPEED = "horizontal_speed";
    public static String ROTATION_SPEED = "rotation_speed";

    private static ConfPair[] keyArray = {
            new ConfPair(SERVER_IP, "192.168.1.3"),
            new ConfPair(SERVER_PORT, "3001"),
            new ConfPair(SWIPE_THRESHOLD, "3"),
            new ConfPair(MAXIMUM_INTERVAL, "300"),  //(300) times beyond this interval will resilt in a speed of zero ( the bigger the number, the slower it will run)
            new ConfPair(FLYING_MODE, "gravity"),
            new ConfPair(VERTICAL_SPEED, "1"),
            new ConfPair(HORIZONTAL_SPEED, "1"),
            new ConfPair(ROTATION_SPEED, "1")
    };

    private static ArrayList<ConfPair> data = new ArrayList<ConfPair>();
    private static SharedPreferences _sharedPref;

    public static void set(ConfPair confPair) {
        if (data.contains(confPair)) {
            data.get(data.indexOf(confPair)).value = confPair.value;
        } else {
            data.add(confPair);
//            fdsfdsffopd
        }
    }

    public static void set(String key, String value) {
        set(new ConfPair(key, value));
    }

    public static String get(ConfPair confPair) {
        if (data.contains(confPair)) {
            return data.get(data.indexOf(confPair)).value;
        }
        return null;
    }

    public static String get(String key) {
        return get(new ConfPair(key, ""));
    }

    public static void set_sharedPref(SharedPreferences pref) {
        _sharedPref = pref;
    }

    public static void loadConfig() {
        data.clear();
        for (ConfPair confPair : keyArray) {
            String value = _sharedPref.getString(confPair.key, "error_not_found");
            if (value == "error_not_found") {
                data.add(new ConfPair(confPair.key, confPair.value));
                Log.i("Config load", confPair.key + "=" + confPair.value);
            } else {
                data.add(new ConfPair(confPair.key, value));
                Log.i("Config load", confPair.key + "=" + value);
            }
        }
    }

    public static void saveConfig() {
        SharedPreferences.Editor editor = _sharedPref.edit();

        for (ConfPair confPair : keyArray) {
            editor.putString(confPair.key, get(confPair.key));
            Log.i("Config save", confPair.key + "=" + get(confPair.key));
        }
        editor.commit();
    }

    public static class ConfPair {
        public String key;
        public String value;

        public ConfPair(String aKey, String aValue) {
            key = aKey;
            value = aValue;
        }

        @Override
        public boolean equals(Object o) {
            return ((ConfPair) o).key == key;
        }
    }
}
