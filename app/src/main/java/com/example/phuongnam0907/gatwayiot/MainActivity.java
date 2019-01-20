package com.example.phuongnam0907.gatwayiot;

import android.app.Activity;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import com.example.phuongnam0907.gatwayiot.MVVM.VM.NPNHomeViewModel;
import com.example.phuongnam0907.gatwayiot.MVVM.View.NPNHomeView;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity implements NPNHomeView {
    private UartDevice uartDevice;
    private static final String UART_DEVICE_NAME = "UART0";
    private static final String TAG = MainActivity.class.getSimpleName();


    private String header_1 ="[{\"gateway\":\"1\",\"sensor\":[";
    private String result = header_1;
    Timer updateTimer;
    Timestamp timestamp;

    private static final String url = "192.168.1.100/";

    private NPNHomeViewModel mHomeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHomeViewModel = new NPNHomeViewModel();
        mHomeViewModel.attach(this, this);

        try {
            PeripheralManager manager = PeripheralManager.getInstance();
            List<String> deviceList = manager.getUartDeviceList();
            if (deviceList.isEmpty()) {
                Log.i(TAG, "No UART port available on this device.");
            } else {
                Log.i(TAG, "List of available devices: " + deviceList);
            }
            uartDevice = manager.openUartDevice(UART_DEVICE_NAME);
            configureUartFrame(uartDevice);
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (uartDevice != null) {
            try {
                uartDevice.close();
                uartDevice = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close UART device", e);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Begin listening for interrupt events
        try {
            uartDevice.registerUartDeviceCallback(mUartCallback);
            updatetimer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Interrupt events no longer necessary
        uartDevice.unregisterUartDeviceCallback(mUartCallback);
    }

    private UartDeviceCallback mUartCallback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            // Read available data from the UART device
            try {
                readUartBuffer(uart);
            } catch (IOException e) {
                Log.w(TAG, "Unable to access UART device", e);
            }

            // Continue listening for more interrupts
            return true;
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            Log.w(TAG, uart + ": Error event " + error);
        }
    };

    private void readUartBuffer(UartDevice uart) throws IOException {
        // Maximum amount of data to read at one time
        final int maxCount = 100;
        byte[] buffer = new byte[maxCount];

        int count;
        while ((count = uart.read(buffer, buffer.length)) > 3) {
            Log.d(TAG, "Read " + count + " bytes from peripheral");
            Integer valueInt = (buffer[2]<<8&0xFF00) ^ (buffer[3]&0x00FF);
            Float value = Float.parseFloat(String.valueOf(valueInt))*100/1024;
            String temp = "id=" + Integer.toString(buffer[0]) + "&des=" + Integer.toString(buffer[1]) + "&val=" + value + "%";
            Log.d("Result from sensor", temp);
            DecimalFormat df = new DecimalFormat("0.00000");
            result += "{\"id\": \""+ Integer.toString(buffer[0]-1) +"\",\"value\": "+ df.format(value)+"},";
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void configureUartFrame(UartDevice uart) throws IOException {
        // Configure the UART port
        uart.setBaudrate(9600);
        uart.setDataSize(8);
        uart.setParity(UartDevice.PARITY_NONE);
        uart.setStopBits(1);
    }

    public void connectWifi(){
        String txtUserName = "UTS_709_IoT";
        String txtPassWord = "ust709iot";
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", txtUserName);
        wifiConfig.preSharedKey = String.format("\"%s\"", txtPassWord);

        //txtConsole.setText("Connecting...");
        Log.d("WifiServer: ","Connecting to Network");

        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        //remember id
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
        Log.d("WifiServer: ","Connected to Network");
        //txtConsole.setText("Connected!!!!");
    }

    public void updatetimer(){
        updateTimer = new Timer();
        TimerTask update = new TimerTask() {
            @Override
            public void run() {

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateData();
                    }
                });
            }
        };
        updateTimer.schedule(update,10000,15000);
    }

    private void updateData(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Date date = new Date();
                    result = result.substring(0,result.length()-1);
                    result += "],\"time\":" + date.getTime()/1000 + "}]";

                    //URL url = new URL("https://studytutorial.in/post.php");
//                    URL url = new URL("http://192.168.1.12:80/server/data.php");
                    URL url = new URL("http://192.168.1.100:80/backend/data.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());

                    os.writeBytes(result.toString());
                    Log.d("json: ", result);
                    os.flush();
                    os.close();

                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    Log.d("phuongnam0907 response",sb.toString());

                    conn.disconnect();

                    result = "";
                    result = header_1;

                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public void onSuccessUpdateServer(String message) {

    }

    @Override
    public void onErrorUpdateServer(String message) {

    }
}
