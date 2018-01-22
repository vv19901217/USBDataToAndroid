package cn.wiky.serial_port;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wiky.usbdatatoandroid.R;

public class Serial_portActivity extends AppCompatActivity {

    @Bind(R.id.et_main_input)
    EditText etMainInput;
    @Bind(R.id.btn_main_send)
    Button btnMainSend;
    //    @Bind(R.id.tv_main_show)
//    static TextView tvMainShow;
    @Bind(R.id.btn_main_close)
    Button btnMainClose;
    @Bind(R.id.listview)
    ListView listview;

    static DataAdapter dataAdapter;
   static List<String> list=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_port);
        ButterKnife.bind(this);
        dataAdapter=new DataAdapter(list,this);
        listview.setAdapter(dataAdapter);
//        tvMainShow = findViewById(R.id.tv_main_show);
        SerialPortUtil.openSrialPort();

    }

//    @OnClick(R.id.btn_main_send)
//    public void onViewClicked() {
//        onSend();
//
//    }


    /**
     * 发送串口数据
     */
    private void onSend() {
        String testString = "{$CCTXA,2097151,1,2,A4313233414243BADCBAC3*72\r\n}";
        etMainInput.setText(stringToAscii(testString));
        String input = etMainInput.getText().toString();

        if (TextUtils.isEmpty(input)) {
            etMainInput.setError("要发送的数据不能为空");
            return;
        }
        SerialPortUtil.sendSerialPort(testString);
    }
    public static String asciiToString(String value)
    {
        StringBuffer sbu = new StringBuffer();
        String[] chars = value.split(",");
        for (int i = 0; i < chars.length; i++) {
            sbu.append((char) Integer.parseInt(chars[i]));
        }
        return sbu.toString();
    }

    public static String stringToAscii(String value)
    {
        StringBuffer sbu = new StringBuffer();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if(i != chars.length - 1)
            {
                sbu.append((int)chars[i]).append(",");
            }
            else {
                sbu.append((int)chars[i]);
            }
        }
        return sbu.toString();
    }

    /**
     * 刷新UI界面
     * @param data 接收到的串口数据
     */
    public static void refreshTextView(String data) {
//        tvMainShow.setText(tvMainShow.getText().toString() + data);
        list.add(data);
        dataAdapter.refresh(list);
    }

    @OnClick({R.id.btn_main_send, R.id.btn_main_close})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_main_send:
                onSend();
                break;
            case R.id.btn_main_close:
                onclose();
                break;
        }
    }

    int i = 1;

    private void onclose() {
        if (i == 1) {
            btnMainClose.setText("关闭");
            SerialPortUtil.closeSerialPort();
            i = 0;
            btnMainClose.setText("打开");
        } else if (i == 0) {
            btnMainClose.setText("打开");
            SerialPortUtil.openSrialPort();
            i = 1;
            btnMainClose.setText("关闭");
        }

    }

}
