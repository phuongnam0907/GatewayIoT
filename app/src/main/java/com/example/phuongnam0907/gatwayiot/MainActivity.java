package com.example.phuongnam0907.gatwayiot;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.example.phuongnam0907.gatwayiot.MVVM.VM.NPNHomeViewModel;
import com.example.phuongnam0907.gatwayiot.MVVM.View.NPNHomeView;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

    private String result ="";

    private static final String url = "192.168.0.10/";

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
            result = "id=" + Integer.toString(buffer[0]) + "&des=" + Integer.toString(buffer[1]) + "&val=" + value + "%";
            Log.d("Result from sensor", result);
            updateData(Integer.toString(buffer[0]),Integer.toString(buffer[1]),value);
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

    private void updateData(final String gateway, final String sensor, final Float value){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Date date = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-ddaHH:mm:s");
                    String newDate = format.format(date);
                    Details details = new Details(gateway, newDate, new SensorData(sensor,value));

                    URL url = new URL("192.168.0.12/");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());

                    os.writeBytes(details.toString());

                    os.flush();
                    os.close();

                    conn.disconnect();
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
