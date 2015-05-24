package com.example.utilizador.ass5.mjpeg;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

/**
 * Created by MITI on 08/05/15.
 */
public class MjpegInputStream extends DataInputStream {
    private static final String TAG = "MjepegInputStream";
    private final byte[] SOI_MARKER = { (byte) 0xFF, (byte) 0xD8 };
    private final byte[] EOF_MARKER = { (byte) 0xFF, (byte) 0xD9 };
    private final String CONTENT_LENGTH = "Content-Length";
    private final static int HEADER_MAX_LENGTH = 100;
    private final static int FRAME_MAX_LENGTH = 40000 + HEADER_MAX_LENGTH;
    private int mContentLength = -1;


    public MjpegInputStream read(String url) {

        Log.i(TAG,"Creating input stream");

        MjpecClientFactory fact = new MjpecClientFactory(url);

        fact.start();
        try {
            fact.join();
            return fact.getStream();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return null;
    }

    public MjpegInputStream(InputStream iN) {
        super(new BufferedInputStream(iN, FRAME_MAX_LENGTH));
    }

    private int getEndOfSeqeunce(DataInputStream in, byte[] sequence) throws IOException {
        int seqIndex = 0;
        byte c;
        for(int i=0; i < FRAME_MAX_LENGTH; i++) {
            c = (byte) in.readUnsignedByte();
            if(c == sequence[seqIndex]) {
                seqIndex++;
                if(seqIndex == sequence.length) return i + 1;
            } else seqIndex = 0;
        }
        return -1;
    }

    private int getStartOfSequence(DataInputStream in, byte[] sequence) throws IOException {
        int end = getEndOfSeqeunce(in, sequence);
        return (end < 0) ? (-1) : (end - sequence.length);
    }

    private int parseContentLength(byte[] headerBytes) throws IOException, NumberFormatException {
        ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
        Properties props = new Properties();
        props.load(headerIn);
        return Integer.parseInt(props.getProperty(CONTENT_LENGTH));
    }

    public Bitmap readMjpegFrame() throws IOException {

        Log.i("IS", " reading single frame");

        mark(FRAME_MAX_LENGTH);
        int headerLen = getStartOfSequence(this, SOI_MARKER);
        reset();
        byte[] header = new byte[headerLen];
        readFully(header);
        try {
            mContentLength = parseContentLength(header);
        } catch (NumberFormatException nfe) {
            mContentLength = getEndOfSeqeunce(this, EOF_MARKER);
        }
        reset();
        byte[] frameData = new byte[mContentLength];
        skipBytes(headerLen);
        readFully(frameData);
        return BitmapFactory.decodeStream(new ByteArrayInputStream(frameData));
    }

    private class MjpecClientFactory extends Thread{

        private MjpegInputStream stream = null;
        String _url ;

        public MjpecClientFactory(String url){
            _url=url;
        }

        @Override
        public void run(){

            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpResponse res;
            try {
                res = httpclient.execute(new HttpGet(URI.create(_url)));
                //Log.i(TAG, res.toString()+" "+res.getEntity().toString()+" "+res.getEntity().getContent().toString());
                MjpegInputStream result = new MjpegInputStream(res.getEntity().getContent());
                stream = result;
                Log.i(TAG, "client created");
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }

        }

        public MjpegInputStream getStream(){
            return stream;
        }
    }


}