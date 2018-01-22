package cn.wiky.usbdatatoandroid;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wiky.usbdatatoandroid.utils.T;
import cn.wiky.usbdatatoandroid.utils.clsPublic;
import logger.Logger;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.getUsbDevice)
    Button getUsbDevice;
    @Bind(R.id.getUsbdata)
    Button getUsbdata;
    @Bind(R.id.postDataToandroid)
    Button postDataToandroid;
    @Bind(R.id.textView)
    TextView textView;
    private UsbManager manager;   //USB管理器
    private UsbDevice mUsbDevice;  //找到的USB设备
    private UsbInterface mInterface;
    private UsbDeviceConnection mDeviceConnection;
    private byte[] Sendbytes;    //发送信息字节
    private byte[] Receiveytes;  //接收信息字节
    ArrayList<String> USBDeviceList = new ArrayList<String>(); // 存放USB设备的数量
    private StringBuffer mStringBuffer = new StringBuffer();

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            textView.setText(mStringBuffer);
        }
    };



    private void openUsbDevice(){
        //before open usb device
        //should try to get usb permission
        tryGetUsbPermission();
    }

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private void tryGetUsbPermission(){
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if (manager == null) {
            return;
        } else {
            Logger.e( "usb设备：" + String.valueOf(manager.toString()));
        }
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbPermissionActionReceiver, filter);

        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        //here do emulation to ask all connected usb device for permission
        for (final UsbDevice usbDevice : manager.getDeviceList().values()) {
            //add some conditional check if necessary
            //if(isWeCaredUsbDevice(usbDevice)){
            if(manager.hasPermission(usbDevice)){
                afterGetUsbPermission(usbDevice);
            }else{
                //this line will let android popup window, ask user whether to allow this app to have permission to operate this usb device
                manager.requestPermission(usbDevice, mPermissionIntent);
            }
            //}
        }
    }


    private void afterGetUsbPermission(UsbDevice usbDevice){
        //call method to set up device communication
        Logger.e( String.valueOf("Got permission for usb device: " + usbDevice));
        T.showShort(this, String.valueOf("Found USB device: VID=" + usbDevice.getVendorId() + " PID=" + usbDevice.getProductId()));
        Logger.e( "usb设备liebiao：" + String.valueOf("Found USB device: VID=" + usbDevice.getVendorId() + " PID=" + usbDevice.getProductId()));

        doYourOpenUsbDevice(usbDevice);
    }

    private void doYourOpenUsbDevice(UsbDevice usbDevice){
        //now follow line will NOT show: User has not given permission to device UsbDevice
        UsbDeviceConnection connection = manager.openDevice(usbDevice);
        //add your operation code here
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        openUsbDevice();

        // 获取USB设备
//        manager = (UsbManager) getSystemService(Context.USB_SERVICE);




    }

    @OnClick({R.id.getUsbDevice, R.id.getUsbdata, R.id.postDataToandroid})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.getUsbDevice:
                USBDeviceList.clear();
                T.showShort(this,"获取USB设备");
                HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                Logger.e( "usb设备：" +deviceList.toString());
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                while (deviceIterator.hasNext()) {
                    UsbDevice device = deviceIterator.next();

                    USBDeviceList.add(String.valueOf(device.getVendorId()));
                    USBDeviceList.add(String.valueOf(device.getProductId()));

                    // 在这里添加处理设备的代码
                    if (device.getVendorId() == 3034 && device.getProductId() == 46880) {
                        mUsbDevice = device;
                        Logger.e( "找到设备");
                        mDeviceConnection = manager.openDevice(mUsbDevice);
                        findIntfAndEpt(mUsbDevice);


                    }
                }
                textView.setText(USBDeviceList.toString());
                break;
            case R.id.getUsbdata:
                T.showShort(this,"获取数据");
                getUSBData();
                break;
            case R.id.postDataToandroid:
                T.showShort(this,"向USB串口发送数据");
                senddata();

                break;
        }
    }

    private void getUSBData() {
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Logger.e( "usb设备：" + String.valueOf(deviceList.size()));
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();

            USBDeviceList.add(String.valueOf(device.getVendorId()));
            USBDeviceList.add(String.valueOf(device.getProductId()));

            // 在这里添加处理设备的代码
            if (device.getVendorId() == 3034 && device.getProductId() == 46880) {
                mUsbDevice = device;
                Logger.e( "找到设备");
                mDeviceConnection = manager.openDevice(mUsbDevice);

            }
        }
//        initCommunication(mUsbDevice);
        findIntfAndEpt(mUsbDevice);
        loopReceiverMessage();

    }
    private UsbEndpoint epOut;
    private UsbEndpoint epIn;
    private void initCommunication(UsbDevice device) {
        textView.append("initCommunication in\n");
        if(3034 == device.getVendorId() && 46880 == device.getProductId()) {
            textView.append("initCommunication in right device\n");
            int interfaceCount = device.getInterfaceCount();
            Logger.e(interfaceCount+"=Q==");//8

            for (int interfaceIndex = 0; interfaceIndex < interfaceCount; interfaceIndex++) {

                UsbInterface usbInterface = device.getInterface(interfaceIndex);

                if ((UsbConstants.USB_CLASS_CDC_DATA != usbInterface.getInterfaceClass())
                        && (UsbConstants.USB_CLASS_COMM != usbInterface.getInterfaceClass())) {

                    continue;
                }
                for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                    Logger.e(usbInterface.getEndpointCount()+"===");

                    UsbEndpoint ep = usbInterface.getEndpoint(i);
                    if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                            epIn = ep;
                        } else {
                            epOut = ep;
                        }
                    }
                }

                if ((null == epIn) || (null == epIn)) {
                    textView.append("endpoint is null\n");
                    epIn = null;
                    epOut = null;
                    mInterface = null;
                } else {
                    textView.append("\nendpoint out: " + epOut + ",endpoint in: " +
                            epIn.getAddress()+"\n");
                    mInterface = usbInterface;
                    mDeviceConnection = manager.openDevice(device);
                    break;
                }
            }
        }
//        loopReceiverMessage();
    }

    /**
     * 接受消息线程 , 此线程在设备(手机)初始化完成后 , 就一直循环接受消息
     */
    private void loopReceiverMessage() {
        Receiveytes=new byte[512];     //这里的64是设备定义的，不是我随便乱写，大家要根据设备而定

        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(1000);
                Logger.e("AAA");
                while (true){

                    if (mDeviceConnection==null){
                        Logger.e("DEVICE"+null);

                    }
                    if (epIn==null){
                        Logger.e("epIN"+null);

                    }
                    if (mDeviceConnection!=null&&epIn!=null){
                        int i =mDeviceConnection.bulkTransfer(epIn,Receiveytes,Receiveytes.length,1000);
Logger.e(i+"");
                        if (i > 0) {
                            mStringBuffer.append(new String(Receiveytes, 0, i) + "\n");
                            handler.sendEmptyMessage(1);
                        }
                    }
                }

            }
        }).start();
    }


    private void senddata() {
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Logger.e( "usb设备：" + String.valueOf(deviceList.size()));
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();

            USBDeviceList.add(String.valueOf(device.getVendorId()));
            USBDeviceList.add(String.valueOf(device.getProductId()));

            // 在这里添加处理设备的代码
            if (device.getVendorId() == 3034 && device.getProductId() == 46880) {
                mUsbDevice = device;
                Logger.e( "找到设备");
                mDeviceConnection = manager.openDevice(mUsbDevice);

            }
        }
//        initCommunication(mUsbDevice);
        findIntfAndEpt(mUsbDevice);
        int ret = -100;
        String testString = "$CFGSYS";
        Sendbytes = clsPublic.HexString2Bytes(testString);

        // 1,发送准备命令
        ret = mDeviceConnection.bulkTransfer(epOut, Sendbytes, Sendbytes.length, 5000);
        Logger.e("已经发送!");

        // 2,接收发送成功信息
        Receiveytes=new byte[512];     //这里的64是设备定义的，不是我随便乱写，大家要根据设备而定
        ret = mDeviceConnection.bulkTransfer(epIn, Receiveytes, Receiveytes.length, 10000);
        Logger.e("接收返回值:" + String.valueOf(ret));
        if(ret != 64) {
            Logger.e("接收返回值"+String.valueOf(ret));
            return;
        }
        else {
            //查看返回值
            Logger.e(clsPublic.Bytes2HexString(Receiveytes));
        }

    }
    // 寻找接口和分配结点
    private void findIntfAndEpt(UsbDevice mUsbDevice) {
        if (mUsbDevice == null) {
            Logger.i("没有找到设备");
            return;
        }
        for (int i = 0; i < mUsbDevice.getInterfaceCount();) {
            // 获取设备接口，一般都是一个接口，你可以打印getInterfaceCount()方法查看接
            // 口的个数，在这个接口上有两个端点，OUT 和 IN
            UsbInterface intf = mUsbDevice.getInterface(i);
            Logger.d( i + " " + intf);
            mInterface = intf;
            break;
        }

        if (mInterface != null) {
            UsbDeviceConnection connection = null;
            // 判断是否有权限
            if(manager.hasPermission(mUsbDevice)) {
                // 打开设备，获取 UsbDeviceConnection 对象，连接设备，用于后面的通讯
                connection = manager.openDevice(mUsbDevice);
                if (connection == null) {
                    return;
                }
                if (connection.claimInterface(mInterface, true)) {
                    Logger.i("找到接口");
                    mDeviceConnection = connection;
                    //用UsbDeviceConnection 与 UsbInterface 进行端点设置和通讯
                    getEndpoint(mDeviceConnection,mInterface);
                } else {
                    connection.close();
                }
            } else {
                Logger.i("没有权限");
            }
        }
        else {
            Logger.i("没有找到接口");
        }
    }

    //用UsbDeviceConnection 与 UsbInterface 进行端点设置和通讯
    private void getEndpoint(UsbDeviceConnection connection, UsbInterface intf) {
        if (intf.getEndpoint(1) != null) {
            epOut = intf.getEndpoint(1);
        }
        if (intf.getEndpoint(0) != null) {
            epIn = intf.getEndpoint(0);
        }
    }

    private final BroadcastReceiver mUsbPermissionActionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice usbDevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    Logger.e("广播接收的信息："+String.valueOf(usbDevice));
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        //user choose YES for your previously popup window asking for grant perssion for this usb device
                        if(null != usbDevice){
                            afterGetUsbPermission(usbDevice);
                        }
                    }
                    else {
                        //user choose NO for your previously popup window asking for grant perssion for this usb device
                        T.showShort(context, String.valueOf("Permission denied for device" + usbDevice));
                    }
                }
            }
        }
    };
}
