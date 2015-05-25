package com.example.utilizador.ass5;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TabHost;

import com.example.utilizador.ass5.mjpeg.MjpegInputStream;
import com.example.utilizador.ass5.mjpeg._MjpegView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static final String FLIP = "FLIP";
    private static final String PHI = "Phi";
    private static final String MIX = "MIX";
    private static final String THETA = "Theta";
    private static final String DANCE = "Dance";
    private static final String TURN = "Turn";
    private static String videoUrl = "http://88.53.197.250/axis-cgi/mjpg/video.cgi?resolution=320x240";
    final float alpha = 0.8f;
    float[] gravity = {0, 0, 0};
    Socket socket = null;
    DataOutputStream dataOutputStream = null;
    DataInputStream dataInputStream = null;
    TabHost _tab;
    Boolean _isControl = false;
    Boolean _isUp = false;
    Boolean _isDown = false;
    Boolean _isRight = false;
    Boolean _isLeft = false;
    Button _control;
    Button _rotateLeft;
    Button _rotateRight;
    Button _up;
    Button _down;
    String serverIPAdress;
    int serverPort;
    private SensorManager _sensorManager;
    private Sensor _accSensor;
    private AccelerometerSensorListener _accListener = new AccelerometerSensorListener();
    private MjpegInputStream _videoInputStream;
    private RelativeLayout _videoLayout;
    private _MjpegView _mjpegView;

    private JoystickView _joystickView;
    private CommandWorker _commandWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Configs
        Settings.set_sharedPref(getApplicationContext().getSharedPreferences("ConfigIp", 0));
        Settings.loadConfig();

        setContentView(R.layout.activity_main);


        _commandWorker = new CommandWorker();


        //Initialize video
//        videoUrl = "http://" + Settings.get(Settings.SERVER_IP) + ":3002/nodecopter.mjpeg";
        _videoLayout = (RelativeLayout) findViewById(R.id.relativeLayoutVideoView);

        _videoInputStream = new MjpegInputStream(null);
        _videoInputStream = _videoInputStream.read(videoUrl);

        _mjpegView = new _MjpegView(this);
        _mjpegView.setSource(_videoInputStream);
        _mjpegView.setDisplayMode(_mjpegView.SIZE_BEST_FIT);
        _mjpegView.startPlayback();
        _mjpegView.showFps(true);
        _videoLayout.addView(_mjpegView);

        //Tab Creation
        _tab = (TabHost) findViewById(R.id.tabHost);
        setupTabs();

        //Buttons
        _joystickView = (JoystickView) findViewById(R.id.joystick);
        _joystickView.setOnJostickMovedListener(new MyJoystickMovedListener());
        _control = (Button) findViewById(R.id.control_btn);
        setupButtons();

        _sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        _accSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (_accSensor != null)
            _sensorManager.registerListener(_accListener, _accSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onResume() {
        super.onResume();
        connectSocket();
        serverIPAdress = Settings.get(Settings.SERVER_IP);
        serverPort = Integer.parseInt(Settings.get(Settings.SERVER_PORT));
    }

    private void setupButtons() {

        //<Control>
        _control.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    _isControl = true;
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    _isControl = false;
                    _commandWorker.newCommand("[\"stop\",[],1]\n");
                }
                return false;
            }
        });

        //</Control>
    }

    private void setupTabs() {
        _tab.setup();
        TabHost.TabSpec tabSpec = _tab.newTabSpec("Fligth Mode");
        tabSpec.setContent(R.id.tabFly);
        tabSpec.setIndicator("Fligth Mode");
        _tab.addTab(tabSpec);

        tabSpec = _tab.newTabSpec("Closer Mode");
        tabSpec.setContent(R.id.tabCloser);
        tabSpec.setIndicator("Closer Mode");
        _tab.addTab(tabSpec);

        tabSpec = _tab.newTabSpec("Tricks");
        tabSpec.setContent(R.id.Tricks);
        tabSpec.setIndicator("Tricks Mode");
        _tab.addTab(tabSpec);
    }

    public void connectSocket() {
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
    }

    private void onLeft() {
        if (_isLeft == true) {
            _commandWorker.newCommand("[\"clockwise\",[0.7],1]\n");
            Log.i("Button", "Clockwise");
        } else {
            _commandWorker.newCommand("[\"stop\",[],1]\n");
            Log.i("Button", "counterClockwise-stop");
        }
    }

    public void onRight() {
        if (_isRight == true) {
            _commandWorker.newCommand("[\"counterClockwise\",[0.7],1]\n");
            Log.i("Button", "counterClockwise");
        } else {
            _commandWorker.newCommand("[\"stop\",[],1]\n");
            Log.i("Button", "counterClockwise-stop");
        }
    }

    public void onUp() {
        if (_isUp) {
            _commandWorker.newCommand("[\"up\",[0.7],2]\n");
            Log.i("Button", "up");
        } else {
            _commandWorker.newCommand("[\"stop\",[],1]\n");
            Log.i("Button", "up stop");
        }

    }

    public void onDown() {
        if (_isDown) {
            _commandWorker.newCommand("[\"down\",[0.7],2]\n");
            Log.i("Button", "down");
        } else {
            _commandWorker.newCommand("[\"stop\",[],1]\n");
            Log.i("Button", "down stop");
        }

    }

    public void onTakeOffClick(View v) {
        _commandWorker.newCommand("[\"calibrate\",[],1]\n");
        Button land = (Button) findViewById(R.id.land_btn);
        Button takeOff = (Button) findViewById(R.id.takeoff_btn);
        _commandWorker.newCommand("[\"takeoff\",[],1]\n");
        takeOff.setVisibility(View.INVISIBLE);
        land.setVisibility(View.VISIBLE);
    }

    public void onFlipAheadClick(View v) {
        Random ran = new Random();
        int num = ran.nextInt(4);
        Log.i(FLIP, String.valueOf(num));
        if (num == 0)
            _commandWorker.newCommand("[\"animate\",[\"flipAhead\",10] ,2]\n");
        else if (num == 1)
            _commandWorker.newCommand("[\"animate\",[\"flipBehind\",10] ,2]\n");
        else if (num == 2)
            _commandWorker.newCommand("[\"animate\",[\"flipLeft\",10] ,2]\n");
        else _commandWorker.newCommand("[\"animate\",[\"flipRight\",10] ,2]\n");
    }

    public void onPhiM30DegClick(View v) {
        Random ran = new Random();
        int num = ran.nextInt(1);
        Log.i(PHI, String.valueOf(num));
        if (num == 0)
            _commandWorker.newCommand("[\"animate\",[\"phiM30Deg\",10] ,2]\n");
        else
            _commandWorker.newCommand("[\"animate\",[\"phi30Deg\",10] ,2]\n");
    }

    public void onThetaClick(View v) {
        Random ran = new Random();
        int num = ran.nextInt(4);
        Log.i(THETA, String.valueOf(num));
        if (num == 0)
            _commandWorker.newCommand("[\"animate\",[\"thetaM30Deg\",10] ,2]\n");
        else if (num == 1)
            _commandWorker.newCommand("[\"animate\",[\"theta30Deg\",10] ,2]\n");
        else if (num == 2)
            _commandWorker.newCommand("[\"animate\",[\"theta20degYaw200deg\",10] ,2]\n");
        else _commandWorker.newCommand("[\"animate\",[\"theta20degYawM200deg\",10] ,2]\n");
    }

    public void onTurnClick(View v) {
        Random ran = new Random();
        int num = ran.nextInt(1);
        Log.i(TURN, String.valueOf(num));
        if (num == 0)
            _commandWorker.newCommand("[\"animate\",[\"turnaround\",10] ,2]\n");
        else
            _commandWorker.newCommand("[\"animate\",[\"turnaroundGodown\",10] ,2]\n");
    }

    public void onDanceClick(View v) {
        Random ran = new Random();
        int num = ran.nextInt(5);
        Log.i(DANCE, String.valueOf(num));
        if (num == 0)
            _commandWorker.newCommand("[\"animate\",[\"yawShake\",10] ,2]\n");
        else if (num == 1)
            _commandWorker.newCommand("[\"animate\",[\"yawDance\",10] ,2]\n");
        else if (num == 2)
            _commandWorker.newCommand("[\"animate\",[\"thetaDance\",10] ,2]\n");
        else if (num == 3)
            _commandWorker.newCommand("[\"animate\",[\"vzDance\",10] ,2]\n");
        else _commandWorker.newCommand("[\"animate\",[\"wave\",10] ,2]\n");
    }

    public void onMixedClick(View v) {
        Random ran = new Random();
        int num = ran.nextInt(1);
        Log.i(MIX, String.valueOf(num));
        if (num == 0)
            _commandWorker.newCommand("[\"animate\",[\"phiThetaMixed\",10] ,2]\n");
        else
            _commandWorker.newCommand("[\"animate\",[\"doublePhiThetaMixed\",10] ,2]\n");
    }

    public void landCommandClick(View v) {
        _commandWorker.newCommand("[\"land\",[],1]\n");
        Button land = (Button) findViewById(R.id.land_btn);
        Button takeOff = (Button) findViewById(R.id.takeoff_btn);
        land.setVisibility(View.INVISIBLE);
        takeOff.setVisibility(View.VISIBLE);

    }

    public void onConfigClick(View v) {
        Intent i = new Intent(this, Configs.class);
        startActivity(i);
    }


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
            /*linear_acceleration[0] = sensorEvent.values[0] - gravity[0];
            linear_acceleration[1] = sensorEvent.values[1] - gravity[1];
            linear_acceleration[2] = sensorEvent.values[2] - gravity[2];*/


            x = (float) ((gravity[0])/10f);
            y = (float) ((gravity[1])/10f);

            x= Math.round(x*10)/10f;
            y= Math.round(y*10)/10f;
//            Log.i("Sensor Listener", "gravity "+x + " y: " + y); //[0] =x, frente-tras ; [1] = y, esquerda-direita
//            Log.i("Sensor Listener", "linear_acceleration "+linear_acceleration[0]+" "+linear_acceleration[1]+" "+linear_acceleration[2]);
            if (_isControl) {
                if (x >= 0) {
                    _commandWorker.newCommand("[\"back\",[" + x + "],2]\n");
                    Log.i("Gravity", "back = " + x);
                } else {
                    x = Math.abs(x);
                    _commandWorker.newCommand("[\"front\",[" + x + "],2]\n");
                    Log.i("Gravity", "front = " + x);
                }

                if (y >= 0) {
                    _commandWorker.newCommand("[\"right\",[" + y + "],2]\n");
                    Log.i("Gravity", "Right = " + y);
                } else {
                    y = Math.abs(y);
                    _commandWorker.newCommand("[\"left\",[" + y + "],2]\n");
                    Log.i("Gravity", "left = " + y);
                }
            }


        }
    }

    private class MyJoystickMovedListener implements JoystickMovedListener {
        float _pan;
        float _tilt;

        @Override
        public void OnMoved(int pan, int tilt) {
            _pan = Math.round(pan) / 10;
            _pan = _pan > 0.7f ? 0.7f : _pan;
            _tilt = Math.round(tilt) / 10;
            _tilt = _tilt > 0.4f ? 0.4f : _tilt;
            Log.i("JOY-X", String.valueOf(_pan));
            _commandWorker.newCommand("[\"counterClockwise\",[" + (_pan) + "],2]\n");

            Log.i("JOY-Y", String.valueOf(_tilt));
            _commandWorker.newCommand("[\"down\",[" + (_tilt) + "],2]\n");

        }

        @Override
        public void OnReleased() {
            _commandWorker.newCommand("[\"stop\",[],1]\n");

        }

        @Override
        public void OnReturnedToCenter() {
            _commandWorker.newCommand("[\"stop\",[],1]\n");

        }
    }
}
