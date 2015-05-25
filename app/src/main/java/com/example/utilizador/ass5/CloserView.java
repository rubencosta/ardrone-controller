package com.example.utilizador.ass5;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.MotionEvent;


/**
 * Created by Ruben on 24/05/2015.
 */
public class CloserView extends View {
    private static final String TAG = "CloserView";
    private float _currentX;
    private float _currentY;
    private long _lastCommandTime;
    private CommandWorker _commandWorker;
    private ScaleGestureDetector _scaleGestureDetector;

    public CloserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        _commandWorker = new CommandWorker();
        _scaleGestureDetector = new ScaleGestureDetector(context, new MyScaleDetector());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        _scaleGestureDetector.onTouchEvent(event);

        if (_scaleGestureDetector.isInProgress()) return false;

        if(System.currentTimeMillis() - _lastCommandTime < 500) return false;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                break;
            case MotionEvent.ACTION_UP:
                stopTouch();
                break;
            case MotionEvent.ACTION_OUTSIDE:
                stopTouch();
                break;
            case MotionEvent.ACTION_CANCEL:
                stopTouch();
                break;
        }
        return true;
    }

    private void moveTouch(float x, float y) {
        float diffX = x - _currentX;
        float diffY = y - _currentY;

        float velX = (Math.round(diffX / 50)*10)/10;
        float velY = (Math.round(diffY / 50)*10)/10;
        if (Math.abs(diffX) >= 5 || Math.abs(diffY) >= 5) {
            Log.i(TAG, "diffX:" + diffX);
            Log.i(TAG, "diffY:" + diffY);

            _commandWorker.newCommand("[\"left\",[" + velX + "],2]\n");
            _commandWorker.newCommand("[\"up\",[" + velY + "],2]\n");

        } else {
            _commandWorker.newCommand("[\"stop\",[],1]\n");
        }
    }

    private void startTouch(float x, float y) {
        _commandWorker.newCommand("[\"stop\",[],1]\n");
        _currentX = x;
        _currentY = y;
    }

    private void stopTouch() {
        _commandWorker.newCommand("[\"stop\",[],1]\n");
    }

    private class MyScaleDetector implements ScaleGestureDetector.OnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {

            if (_scaleGestureDetector.getScaleFactor() > 1) {
                _commandWorker.newCommand("[\"front\",[0.7],2]\n");
            } else {
                _commandWorker.newCommand("[\"back\",[0.7],2]\n");

            }
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            Log.i(TAG, "Scaling started");
            _commandWorker.newCommand("[\"stop\",[],1]\n");
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            Log.i(TAG, "Scaling ended");
            _commandWorker.newCommand("[\"stop\",[],1]\n");
            _lastCommandTime = System.currentTimeMillis();
        }
    }
}
