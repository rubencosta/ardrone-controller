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
import android.widget.TabHost;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
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
    private Sensor _accSensor;

    private AccelerometerSensorListener acc_listener = new AccelerometerSensorListener();

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
    Button _config;

    String serverIPAdress;
    int serverPort;


    byte[] response = new byte[256];

    @Override
    protected void onResume() {
        super.onResume();
        connectSocket();
        serverIPAdress = Settings.get(Settings.SERVER_IP);
        serverPort = Integer.parseInt(Settings.get(Settings.SERVER_PORT));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Settings.set_sharedPref(getApplicationContext().getSharedPreferences("ConfigIp", 0));
        Settings.loadConfig();

        serverIPAdress = Settings.get(Settings.SERVER_IP);

        serverPort = Integer.parseInt(Settings.get(Settings.SERVER_PORT));

        _sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        _accSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (_accSensor != null)
            _sensorManager.registerListener(acc_listener, _accSensor, SensorManager.SENSOR_DELAY_NORMAL);

        //Tab Creation
        _tab = (TabHost) findViewById(R.id.tabHost);
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


        connectSocket();

        //<Rotate Left>
        _rotateLeft = (Button) findViewById(R.id.left_btn);
        _rotateLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    _isLeft = true;
                    onLeft();

                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    _isLeft = false;
                    onLeft();
                }
                return false;
            }
        });
        //</Rotate Left>

        //<Rotate Right>
        _rotateRight = (Button) findViewById(R.id.right_btn);
        _rotateRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                   _isRight = true;
                    onRight();
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                   _isRight = false;
                    onRight();
                }
                return false;
            }
        });
        //</Rotate Right>

        //<Up>
        _up = (Button) findViewById(R.id.up_btn);
        _up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    _isUp = true;
                    onUp();

                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    _isUp = false;
                   onUp();
                }
                return false;
            }
        });
        //</Up>

        //<Down>
        _down = (Button) findViewById(R.id.down_btn);
        _down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    _isDown = true;
                    onDown();
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    _isDown =false;
                    onDown();
                }
                return false;
            }
        });
        //</Down>

        //<Control>
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

        //</Control>

    }

    private void onLeft() {
           if(_isLeft == true) {
               new CommandWorkerThread("[\"Clockwise\",[0.7],1]\n").start();
               Log.i("Button", "Clockwise");
           }
           else {
                new CommandWorkerThread("[\"stop\",[],1]\n").start();
                Log.i("Button", "counterClockwise-stop");
           }
    }

    public void connectSocket(){
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

    public void onRight() {
        if(_isRight == true) {
            new CommandWorkerThread("[\"counterclockwise\",[0.7],1]\n").start();
            Log.i("Button", "counterClockwise");
        }
        else {
            new CommandWorkerThread("[\"stop\",[],1]\n").start();
            Log.i("Button", "counterClockwise-stop");
        }
    }

    public void onUp(){
        if(_isUp) {
            new CommandWorkerThread("[\"up\",[0.7],2]\n").start();
            Log.i("Button", "up");
        }
        else {
            new CommandWorkerThread("[\"stop\",[],1]\n").start();
            Log.i("Button", "up stop");
        }

    }
    public void onDown(){
        if(_isDown) {
            new CommandWorkerThread("[\"down\",[0.7],2]\n").start();
            Log.i("Button", "down");
        }
        else {
            new CommandWorkerThread("[\"stop\",[],1]\n").start();
            Log.i("Button", "down stop");
        }

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

    public void onConfigClick(View v){
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


}
