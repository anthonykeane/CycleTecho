package com.example.arduino_serial;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A sample Activity demonstrating USB-Serial support.
 *
 * @author mike wakerly (opensource@hoho.com)
 */
public class ArduinoSerial extends Activity {

    private final String TAG = ArduinoSerial.class.getSimpleName();

    /**
     * The device currently in use, or {@code null}.
     */
    private UsbSerialDriver mSerialDevice;

    /**
     * The system's USB service.
     */
    private UsbManager mUsbManager;

    private GaugeView gSpeedo;
    private GaugeView gCadence;
    private GaugeView gBattery;
    private GaugeView gThrottle;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private SerialInputOutputManager mSerialIoManager;

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    ArduinoSerial.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArduinoSerial.this.updateReceivedData(data);
                        }
                    });
                }
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        gSpeedo = (GaugeView) findViewById(R.id.vgSpeedo);
        gSpeedo.setTargetValue(60.0f);
        gSpeedo.isInEditMode();

        gCadence = (GaugeView) findViewById(R.id.vgCadence);
        gCadence.setTargetValue(120.0f);
        gCadence.isInEditMode();

        gBattery = (GaugeView) findViewById(R.id.vgBattery);
        gBattery.setTargetValue(28.0f);
        gBattery.isInEditMode();

        gThrottle = (GaugeView) findViewById(R.id.vgThrottle);
        gThrottle.setTargetValue(28.0f);
        gThrottle.isInEditMode();

        //todo  this is JSON testing bit
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject("{'speed': 0.00,'pedaling': 0,'bat': 43.5,'AssistLevelV': 0.76,'ThrottleV': 1.31,'CadenceRPM': 0}");
            gSpeedo.setTargetValue(Float.parseFloat(jsonObj.getString("speed")));
            gCadence.setTargetValue((Float.parseFloat(jsonObj.getString("CadenceRPM"))));
            gBattery.setTargetValue((Float.parseFloat(jsonObj.getString("bat"))));
            gThrottle.setTargetValue((Float.parseFloat(jsonObj.getString("ThrottleV"))));
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        stopIoManager();
        if (mSerialDevice != null) {
            try {
                mSerialDevice.close();
            } catch (IOException e) {
                // Ignore.
            }
            mSerialDevice = null;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

        RelativeLayout.LayoutParams layoutParams =(RelativeLayout.LayoutParams) gSpeedo.getLayoutParams();
        if (layoutParams != null) {
            if(newConfig.orientation!=2)
            {
                //layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
                //layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                //layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

            }
            else
            {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                //layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                //layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                //layoutParams.removeRule(RelativeLayout.CENTER_HORIZONTAL);
            }

            gSpeedo.setLayoutParams(layoutParams);


        }


        layoutParams =(RelativeLayout.LayoutParams) gCadence.getLayoutParams();
        if (layoutParams != null) {
            if(newConfig.orientation!=2)
            {
                layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
                layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

            }
            else
            {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                layoutParams.removeRule(RelativeLayout.CENTER_HORIZONTAL);
            }

            gSpeedo.setLayoutParams(layoutParams);


        }



        layoutParams =(RelativeLayout.LayoutParams) gBattery.getLayoutParams();
        if (layoutParams != null) {
            if(newConfig.orientation!=2)
            {
                layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                layoutParams.removeRule(RelativeLayout.CENTER_HORIZONTAL);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                //layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            }
            else
            {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                //layoutParams.removeRule(RelativeLayout.CENTER_VERTICAL);
            }

            gBattery.setLayoutParams(layoutParams);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        mSerialDevice = UsbSerialProber.acquire(mUsbManager);
        Log.d(TAG, "Resumed, mSerialDevice=" + mSerialDevice);
        if (mSerialDevice == null) {
            //gSpeedo.setTargetValue(0.0f);
        } else {
            try {
                mSerialDevice.open();
            } catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                //mTitleTextView.setText("Error opening device: " + e.getMessage());
                try {
                    mSerialDevice.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                mSerialDevice = null;
                return;
            }
            //mTitleTextView.setText("Serial device: " + mSerialDevice);
        }
        onDeviceStateChange();
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (mSerialDevice != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(mSerialDevice, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    public void appendLog(String text)
    {
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        File logFile = new File(path, "arduino-serial.log");

        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.flush();
            buf.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    private void updateReceivedData(byte[] data) {


        try {
            String decoded = new String(data, "UTF-8");
            JSONObject jsonObj = new JSONObject(decoded);

            final String message = String.format("%s\n",decoded);

            //jsonObj = new JSONObject("{'speed': 0.00,'pedaling': 0,'bat': 43.5,'AssistLevelV': 0.76,'ThrottleV': 1.31,'CadenceRPM': 0}");
            gSpeedo.setTargetValue(Float.parseFloat(jsonObj.getString("speed")));
            gCadence.setTargetValue((Float.parseFloat(jsonObj.getString("CadenceRPM"))));
            gBattery.setTargetValue((Float.parseFloat(jsonObj.getString("bat"))));
            gThrottle.setTargetValue((Float.parseFloat(jsonObj.getString("ThrottleV"))));

            appendLog(message);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}