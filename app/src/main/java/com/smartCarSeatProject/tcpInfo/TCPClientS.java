package com.smartCarSeatProject.tcpInfo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.smartCarSeatProject.activity.MainActivity;
import com.smartCarSeatProject.data.BaseVolume;
import com.smartCarSeatProject.data.DataAnalysisHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClientS {

    /**
     * single instance TcpClient
     * */
    private static TCPClientS mSocketClient = null;

    private Context context;
    private String deviceID;
    private String deviceName;
    private int isConnectCount = -1;
    private String ip;
    private int port;

    private TCPClientS(Context context){
        this.context = context;
    }

    public TCPClientS(String deviceID , String hostIp , int hostListenningPort ,Context context ) {
        this.ip = hostIp;
        this.port = hostListenningPort;
        this.context = context;
        doConnect();
    }


    public static TCPClientS getInstance(Context context){
        if(mSocketClient == null){
            synchronized (TCPClientS.class) {
                mSocketClient = new TCPClientS(context);
            }
        }
        return mSocketClient;
    }

    // 连接已断开
    public static final int TCP_CONNECT_STATE_DISCONNECT = -1;
    // 连接成功
    public static final int TCP_CONNECT_STATE_CONNECTED = 1;
    // 连接中
    public static final int TCP_CONNECT_STATE_CONNECTTING = 0;
    private int isConnection = TCP_CONNECT_STATE_DISCONNECT;

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    String TAG_log = "Socket";
    private Socket mSocket;

    private OutputStream mOutputStream;
    private InputStream mInputStream;

    private SocketThread mSocketThread;
    private boolean isStop = false;//thread flag

    /**
     * 128 - 数据按照最长接收，一次性
     * */
    private class SocketThread extends Thread {
        @Override
        public void run() {
            Log.e(TAG_log,"SocketThread start ");
            super.run();
            //connect ...
            try {
                if (mSocket != null) {
                    mSocket.close();
                    mSocket = null;
                }
                InetAddress ipAddress = InetAddress.getByName(ip);
                mSocket = new Socket(ipAddress, port);
                if(isConnect()){
                    mOutputStream = mSocket.getOutputStream();
                    mInputStream = mSocket.getInputStream();
                    isStop = false;
                    uiHandler.sendEmptyMessage(1);
                }
                /* 此处这样做没什么意义不大，真正的socket未连接还是靠心跳发送，等待服务端回应比较好，一段时间内未回应，则socket未连接成功 */
                else{
                    Message msg = new Message();
                    msg.what = -1;
                    msg.obj = "SocketThread connect fail";
                    uiHandler.sendMessage(msg);
                    Log.e(TAG_log,"SocketThread connect fail");
                    return;
                }
            }
            catch (IOException e) {
                Message msg = new Message();
                msg.what = -1;
                msg.obj = e.getMessage();
                uiHandler.sendMessage(msg);
                Log.e(TAG_log,"SocketThread connect io exception = "+e.getMessage());
                e.printStackTrace();
                return;
            }
            Log.d(TAG_log,"SocketThread connect over ");
            //read ...
            while (isConnect() && !isStop && !isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[1024];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);//null data -1 , zrd serial rule size default 10
                    if (size > 0) {
                        Message msg = new Message();
                        msg.what = 100;
                        Bundle bundle = new Bundle();
                        bundle.putByteArray("data",buffer);
                        bundle.putInt("size",size);
                        msg.setData(bundle);
                        uiHandler.sendMessage(msg);
                    }
                    Log.i(TAG_log, "SocketThread read listening");
                }
                catch (IOException e) {
                    Message msg = new Message();
                    msg.what = -1;
                    msg.obj = e.getMessage();
                    uiHandler.sendMessage(msg);
                    Log.e(TAG_log,"SocketThread read io exception = "+e.getMessage());
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    //==============================socket connect============================
    /**
     * connect socket in thread
     * Exception : android.os.NetworkOnMainThreadException
     * */
    public void doConnect(){
        mSocketThread = new SocketThread();
        mSocketThread.start();
    }

    /**
     * socket is connect
     * */
    public boolean isConnect(){
        boolean flag = false;
        if (mSocket != null) {
            flag = mSocket.isConnected();
        }
        return flag;
    }

    /**
     * socket disconnect
     * */
    public void closeTCPSocket() {
        Log.e("closeConnect", "断开连接");
        isStop = true;
        isConnection = TCP_CONNECT_STATE_DISCONNECT;
        try {
            if (mOutputStream != null) {
                mOutputStream.close();
            }
            if (mInputStream != null) {
                mInputStream.close();
            }
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mSocketThread != null) {
            mSocketThread.interrupt();
        }
    }

    /**
     * send byte[] cmd
     * Exception : android.os.NetworkOnMainThreadException
     * */
    public void sendByteCmd(final byte[] mBuffer) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mOutputStream != null) {
                        mOutputStream.write(mBuffer);
                        mOutputStream.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * send string cmd to serial
     */
    public void sendHexText(String cmd) {
        Log.e("发送数据", "发送数据："+cmd);
        byte[] mBuffer = cmd.getBytes();
        sendByteCmd(mBuffer);
    }


    Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                //connect error
                case -1:
                    String strError = (String) msg.obj;
                    if (null != onDataReceiveListener) {
                        onDataReceiveListener.onConnectFail();
                        closeTCPSocket();
                    }
                    isConnection = TCP_CONNECT_STATE_DISCONNECT;
                    // 局域网离线啦！
                    context.sendBroadcast(new Intent(BaseVolume.Companion.getBROADCAST_TCP_INFO())
                            .putExtra(BaseVolume.Companion.getBROADCAST_TYPE(),BaseVolume.Companion.getBROADCAST_TCP_CONNECT_CALLBACK())
                            .putExtra(BaseVolume.Companion.getBROADCAST_TCP_STATUS(),false)
                            .putExtra(BaseVolume.Companion.getBROADCAST_MSG(),strError));
                    break;

                //connect success
                case 1:
                    if (null != onDataReceiveListener) {
                        onDataReceiveListener.onConnectSuccess();
                    }
                    isConnection = TCP_CONNECT_STATE_CONNECTED;
                    isConnectCount = 0;
                    strOldBuffer = "";
                    context.sendBroadcast(new Intent(BaseVolume.Companion.getBROADCAST_TCP_INFO())
                            .putExtra(BaseVolume.Companion.getBROADCAST_TYPE(),BaseVolume.Companion.getBROADCAST_TCP_CONNECT_CALLBACK())
                            .putExtra(BaseVolume.Companion.getBROADCAST_TCP_STATUS(),true));
                    break;

                //receive data
                case 100:
                    Bundle bundle = msg.getData();
                    byte[] buffer = bundle.getByteArray("data");
                    int size = bundle.getInt("size");
                    int mequestCode = bundle.getInt("requestCode");
                    byte[] data = new byte[size];
                    System.arraycopy(buffer, 0, data, 0, size);
                    String strHexData = BaseVolume.Companion.bytesToHexString(data);
                    Log.e("接收数据", "接收局域网，数据：" + strHexData);
                    checkData(strHexData);
                    break;
            }
        }
    };

    private String strOldBuffer = "";
    private void checkData(String data) {
        strOldBuffer = strOldBuffer + data;
        while ((strOldBuffer.indexOf(BaseVolume.Companion.getCOMMAND_END())) > 0) {
            // 发现有数据尾\r\n，则说明存在有效数据
            int iEndIndex = strOldBuffer.indexOf(BaseVolume.Companion.getCOMMAND_END());
            if (iEndIndex >= 0) {
                String strGood = strOldBuffer.substring(0,iEndIndex+2);// +2,是为了截取带包尾的完整数据
                Log.e("数据", "有效数据：$strGood");
                // 解析数据
                DataAnalysisHelper.Companion.getInstance(context).startDataAnalysis(strGood);
                strOldBuffer = strOldBuffer.substring(iEndIndex+2);
            }
        }
    }


    /**
     * socket response data listener
     * */
    private OnDataReceiveListener onDataReceiveListener = null;
    public interface OnDataReceiveListener {
        public void onConnectSuccess();
        public void onConnectFail();
        public void onDataReceive(String strData, int size, int requestCode);
    }
    public void setOnDataReceiveListener(
            OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }

}
