package com.example.utilizador.ass5;

/**
 * Created by Ruben on 24/05/2015.
 */
import android.os.AsyncTask;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class CommandWorker{
    Socket socket;
    DataOutputStream dataOutputStream;
    DataInputStream dataInputStream;

    String serverIPAdress = Settings.get(Settings.SERVER_IP);
    int serverPort = Integer.parseInt(Settings.get(Settings.SERVER_PORT));
    byte[] response = new byte[256];
    private static boolean _last_command_stop = false;

    public CommandWorker()
    {
        initialize();
    }

    public void initialize()
    {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                try {
                    socket = new Socket(serverIPAdress, serverPort);
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    dataInputStream = new DataInputStream(socket.getInputStream());
                }catch(Exception e){
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public void newCommand(String command) {
        if(socket == null || !socket.isConnected() || dataInputStream == null || dataOutputStream == null)
            initialize();

        if(command.contains("stop")) {
            if(!_last_command_stop) {
                new CommandWorkerThread(command).start();
                _last_command_stop = true;
            }
        }
        else {
            new CommandWorkerThread(command).start();
            _last_command_stop = false;
        }
    }

    public void closeConnection() {
        try {
            dataInputStream.close();
            dataOutputStream.close();
            socket.close();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private class CommandWorkerThread extends Thread {
        private String _command;
        private boolean first = true;


        public CommandWorkerThread(String command) {
            _command = command;
        }

        @Override
        public void run() {
            try {
                dataOutputStream.writeBytes(_command);
                //dataInputStream.readFully(response);    //com isto, enche a stack nao rebenta o server ||| sem isto rebenta o server, mas nao enche a stack
                //dataOutputStream.flush();
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
}
