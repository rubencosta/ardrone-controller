package com.example.utilizador.ass5;

/**
 * Created by Ruben on 24/05/2015.
 */
public interface JoystickMovedListener {
    public void OnMoved(int pan, int tilt);
    public void OnReleased();
    public void OnReturnedToCenter();
}
