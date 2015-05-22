package com.example.utilizador.ass5;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class Configs extends ActionBarActivity {

    private EditText _ip;
    private EditText _port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configs);

        _ip = (EditText)findViewById(R.id.ip_text);
        _port = (EditText)findViewById(R.id.port_text);

        _ip.setText(Settings.get(Settings.SERVER_IP));
        _port.setText(Settings.get(Settings.SERVER_PORT));
    }

    public void handleCancel (View v){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    public void handleSave (View v){

        Settings.set(Settings.SERVER_IP, _ip.getText().toString().replace(" ", ""));
        Settings.set(Settings.SERVER_PORT, _port.getText().toString());
        Settings.saveConfig();

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }


}
