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
        }
        return true;
    }

    private void moveTouch(float x, float y) {
        float diffX = x - _currentX;
        float diffY = y - _currentY;

        float velX = diffX / 50;
        float velY = diffY / 50;
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
        float _scaleFactor = 1f;
        float _velocity;

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {

            _velocity = (scaleGestureDetector.getCurrentSpan() - scaleGestureDetector.getPreviousSpan()) * 0.1f;

            _commandWorker.newCommand("[\"front\",[" + _velocity + "],2]\n");

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
        }
    }
}
