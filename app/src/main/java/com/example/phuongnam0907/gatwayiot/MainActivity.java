package com.example.phuongnam0907.gatwayiot;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.IOException;
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
public class MainActivity extends Activity {
    private UartDevice uartDevice;
    private static final String UART_DEVICE_NAME = "UART0";
    private static final String TAG = MainActivity.class.getSimpleName();

    private String result ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        while ((count = uart.read(buffer, buffer.length)) > 0) {
            //Log.d(TAG, "Read " + count + " bytes from peripheral");
            String str = new String(buffer, "UTF-8"); // chuyen du lieu tu byte sang String
            String c = str.substring(0, count); // xu ly du lieu cho dung chuan
            Log.d(TAG, "Read " + count + " bytes from peripheral");
            Integer valueInt = (buffer[2]<<8&0xFF00) ^ (buffer[3]&0x00FF);
            //for (int i = 0; i< 4; i++) Log.d(TAG,Integer.toString(buffer[i]));
            //if (valueInt < 0) valueInt += 1280;
            Float value = Float.parseFloat(String.valueOf(valueInt))*100/1024;
            result = "id=" + Integer.toString(buffer[0]) + "&des=" + Integer.toString(buffer[1]) + "&val=" + value + "%";
            Log.d("Result from sensor", result);
            //for(int i = 0; i< count; i++) Log.d(TAG,Integer.toString(buffer[i]));
            /*result += c;
            if (result.indexOf("%") > 4) {
                Log.d(TAG, "Read data from sensor: " + result);
                result = "";
            }*/
        }

    }

    public void configureUartFrame(UartDevice uart) throws IOException {
        // Configure the UART port
        uart.setBaudrate(9600);
        uart.setDataSize(8);
        uart.setParity(UartDevice.PARITY_NONE);
        uart.setStopBits(1);
    }

}
