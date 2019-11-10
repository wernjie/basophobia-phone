package com.limwernjie.basophobiaphone;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.HashSet;

public class MovementDetector implements SensorEventListener {

    protected final String TAG = getClass().getSimpleName();

    private SensorManager sensorMan;
    private Sensor accelerometer;

    private MovementDetector() {
    }

    private static MovementDetector mInstance;

    public static MovementDetector getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MovementDetector();
            mInstance.init(context);
        }
        return mInstance;
    }

    //////////////////////
    private HashSet<MotionListener> mListeners = new HashSet<MovementDetector.MotionListener>();
    private HashSet<ActivityListener> aListeners = new HashSet<MovementDetector.ActivityListener>();

    private void init(Context context) {
        sensorMan = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void start() {
        sensorMan.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void stop() {
        sensorMan.unregisterListener(this);
    }

    public void addListener(MotionListener listener) {
        mListeners.add(listener);
    }

    public void addListener(ActivityListener listener) {
        aListeners.add(listener);
    }

    /* (non-Javadoc)
     * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float diff = (float) Math.sqrt(x * x + y * y + z * z);

            for (ActivityListener listener : aListeners) {
                listener.onMotionActivityUpdated(event, diff);
            }

            if (diff > 0.5) {

            } else {
                for (MotionListener listener : mListeners) {
                    listener.onMotionDetected(event, diff);
                }
            }
        }

    }

    /* (non-Javadoc)
     * @see android.hardware.SensorEventListener#onAccuracyChanged(android.hardware.Sensor, int)
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }

    public interface MotionListener {
        void onMotionDetected(SensorEvent event, float acceleration);
    }

    public interface ActivityListener {
        void onMotionActivityUpdated(SensorEvent event, float acceleration);
    }
}
