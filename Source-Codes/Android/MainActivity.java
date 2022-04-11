package com.p4f.esp32camai;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.p4f.esp32camai.bahadir.rckontrol.MainActivity2;

import java.io.IOException;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener{
    private static final String TAG = "MainActivity";
    private String mSelectedCam = "";
    private String mSelectedCamPre = "";
    Bundle mSavedInstanceState;
    boolean mConnected = false;
    private Esp32CameraFragment mFragmentCam = null;
    private Menu mMenu = null;
    Esp32CameraFragment cameraFragment;

    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Button F_Button,B_Button,R_Button,L_Button,AutoOn_button,AutoOff_button, Brush_On, Brush_Off, otonom_button;


    @SuppressLint("ClickableViewAccessibility")
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSavedInstanceState = savedInstanceState;
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        cameraFragment = new Esp32CameraFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment, cameraFragment, "camera").commit();
            Intent newint = getIntent();
            address = newint.getStringExtra(MainActivity2.EXTRA_ADRESS);

            F_Button = (Button) findViewById(R.id.forward_button);
            B_Button = (Button) findViewById(R.id.backward_button);
            R_Button = (Button) findViewById(R.id.right_button);
            L_Button = (Button) findViewById(R.id.left_button);
            otonom_button = (Button) findViewById(R.id.otonom_btn);


            otonom_button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if(btSocket!=null)
                    {
                        switch (event.getAction())
                        {
                            case MotionEvent.ACTION_DOWN:
                                try
                                {
                                    btSocket.getOutputStream().write("A".getBytes());
                                }
                                catch (IOException e)
                                {

                                }
                                break;

                            case MotionEvent.ACTION_UP:
                                try
                                {
                                    btSocket.getOutputStream().write("A".getBytes());
                                }
                                catch(IOException e)
                                {

                                }
                                break;
                        }
                    }
                    return false;
                }
            });

            F_Button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if(btSocket!=null)
                    {
                        switch (event.getAction())
                        {
                            case MotionEvent.ACTION_DOWN:
                                try
                                {
                                    btSocket.getOutputStream().write("F".getBytes());
                                }
                                catch (IOException e)
                                {

                                }
                                break;

                            case MotionEvent.ACTION_UP:
                                try
                                {
                                    btSocket.getOutputStream().write("S".getBytes());
                                }
                                catch(IOException e)
                                {

                                }
                                break;
                        }
                    }
                    return false;
                }
            });
            R_Button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if(btSocket!=null)
                    {
                        switch (event.getAction())
                        {
                            case MotionEvent.ACTION_DOWN:
                                try
                                {
                                    btSocket.getOutputStream().write("R".getBytes());
                                }
                                catch (IOException e)
                                {

                                }
                                break;

                            case MotionEvent.ACTION_UP:
                                try
                                {
                                    btSocket.getOutputStream().write("S".toString().getBytes());
                                }
                                catch(IOException e)
                                {

                                }
                                break;
                        }
                    }
                    return false;
                }
            });
            L_Button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if(btSocket!=null)
                    {
                        switch (event.getAction())
                        {
                            case MotionEvent.ACTION_DOWN:
                                try
                                {
                                    btSocket.getOutputStream().write("L".toString().getBytes());
                                }
                                catch (IOException e)
                                {

                                }
                                break;

                            case MotionEvent.ACTION_UP:
                                try
                                {
                                    btSocket.getOutputStream().write("S".toString().getBytes());
                                }
                                catch(IOException e)
                                {

                                }
                                break;
                        }
                    }
                    return false;
                }
            });
            B_Button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if(btSocket!=null)
                    {
                        switch (event.getAction())
                        {
                            case MotionEvent.ACTION_DOWN:
                                try
                                {
                                    btSocket.getOutputStream().write("B".toString().getBytes());
                                }
                                catch (IOException e)
                                {

                                }
                                break;

                            case MotionEvent.ACTION_UP:
                                try
                                {
                                    btSocket.getOutputStream().write("S".toString().getBytes());
                                }
                                catch(IOException e)
                                {

                                }
                                break;
                        }
                    }
                    return false;
                }
            });
            new BTbaglan().execute();
        }else
            onBackStackChanged();
    }

    @Override

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        cameraFragment.onWindowFocusChanged();
    }

    @Override
    public void onBackStackChanged() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount()>0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Disconnect();
    }

    private void Disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                // msg(Error);
            }
        }
        finish();
    }
    private class BTbaglan extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(MainActivity.this, "Connecting", "Please Wait");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice cihaz = myBluetooth.getRemoteDevice(address);
                    btSocket = cihaz.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();

                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!ConnectSuccess) {
                Toast.makeText(getApplicationContext(), "Connection Error", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Connection is Success", Toast.LENGTH_SHORT).show();

                isBtConnected = true;
            }
            progress.dismiss();
        }


    }

}
