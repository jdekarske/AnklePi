package com.jasondekarske.anklepi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.Boolean.TRUE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";
//    /**
//     * Preferences
//     */
//    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//    private String sharedPrefFile = "com.example.android.sharedprefs";
    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the bluetooth services
     */
    private BluetoothService mService = null;

    /**
     * Widgets
     */
    private TextView psi1;
    private TextView psi2;
    private EditText mpsi_out;
    private Button mSendButton;
    private ProgressBar gauge1;
    private ProgressBar gauge2;
//    private Integer mWalkPSI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        psi1 = (TextView) findViewById(R.id.psi1);
        psi2 = (TextView) findViewById(R.id.psi2);
        mpsi_out = (EditText) findViewById(R.id.psi_out);
        mSendButton = (Button) findViewById(R.id.button_send);
        gauge1 = (ProgressBar) findViewById(R.id.circle_progress_barcyl);
        gauge2 = (ProgressBar) findViewById(R.id.circle_progress_barres);

        mSendButton.setOnClickListener(new View.OnClickListener() {
                                           public void onClick(View v) {
                                               // Send a message using content of the number picker widget
                                               String message = mpsi_out.getText().toString();
                                               sendMessage(message);
                                           }
                                       }
        );

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        //bluetooth nonsense
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //if bluetooth isn't enabled let them know
        if (!mBluetoothAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Snackbar.make(findViewById(android.R.id.content), "Enable Bluetooth and restart app", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_monitor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_connect:
                connectDevice();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Initialize the BluetoothChatService to perform bluetooth connections
        Log.d(TAG, "start1");
        if (mService == null) {
            mService = new BluetoothService(mHandler);
            // Initialize the buffer for outgoing messages
            mOutStringBuffer = new StringBuffer("");
        } else {
            Log.d(TAG, "onstart mservice!=null");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "resuming");
        Log.d(TAG, " " + String.valueOf(mService.getState()));


//        if (mService != null) {
//            if (mService.getState() == BluetoothService.STATE_NONE) {
//                // Start the Bluetooth chat services
//                mService.start();
//            } else {
//                Log.d(TAG, "onresume mservice not statenone");
//            }
//        }
////        sendSettings();
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "stop " + String.valueOf(mService.getState()));


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (mService != null) {
//            mService.stop();
//        }

        Log.d(TAG, "destroy " + String.valueOf(mService.getState()));

    }

    private void sendSettings() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultwalking = sharedPref.getString(SettingsActivity.defaultwalking, "");
        String defaultsquat = sharedPref.getString(SettingsActivity.defaultsquat, "");
        String defaultdeadlift = sharedPref.getString(SettingsActivity.defaultdeadlift, "");
        String defaultpress = sharedPref.getString(SettingsActivity.defaultpress, "");
        Boolean loglift = sharedPref.getBoolean(SettingsActivity.loglift, false);
        Boolean logpressure = sharedPref.getBoolean(SettingsActivity.logpressure, false);

        String message = "s" + defaultwalking + "/" + defaultsquat + "/" + defaultdeadlift + "/" + defaultpress + "/"
                + loglift.toString() + "/" + logpressure.toString();
        sendMessage(message);
    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
            Snackbar.make(findViewById(android.R.id.content), "Connect to your ankle", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);

        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            Log.d(TAG, String.valueOf(msg.arg1));
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus(R.string.title_connected_to);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            psi1.setText("-");
                            psi2.setText("-");
                            gauge1.setProgress(0);
                            gauge2.setProgress(0);
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer

                    try {
                        String readMessage = new String(readBuf, 0, msg.arg1);
                        String cleaned = readMessage.substring(0, 9);
                        String parsed[] = cleaned.split("/");
                        psi1.setText(parsed[0]);
                        psi2.setText(parsed[1]);
                        Log.d(TAG,parsed[0]);
                        gauge1.setProgress(Math.round(Float.parseFloat(parsed[0]))); //really?
                        gauge2.setProgress(Math.round(Float.parseFloat(parsed[1])));
                    } catch (IndexOutOfBoundsException e) {
                        Log.e(TAG, "Receiving parse issue", e);
                    }
                    break;
//                case Constants.MESSAGE_DEVICE_NAME:
//                    // save the connected device's name
//                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
//                    if (null != activity) {
//                        Toast.makeText(activity, "Connected to "
//                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
//                    }
//                    break;
//                case Constants.MESSAGE_TOAST:
//                    if (null != activity) {
//                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
//                                Toast.LENGTH_SHORT).show();
//                    }
//                    break;
            }
        }
    };
    public void setStatus(int resId) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle(resId);
    }

    private void connectDevice() {
        // Get the device MAC address
        String address = "B8:27:EB:10:17:54";
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mService.connect(device, TRUE);
    }

}