package com.smartCarSeatProject.tcpInfo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.smartCarSeatProject.data.BaseVolume;
import com.smartCarSeatProject.data.DataAnalysisHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TaskCenter {

    public int iConnectState = BaseVolume.TCP_CONNECT_STATE_DISCONNECT;

    private static TaskCenter instance;
    private static final String TAG = "TaskCenter";
    //    Socket
    private Socket socket;
    //    IP地址
    private String ipAddress;
    //    端口号
    private int port;
    private Thread thread;
    //    Socket输出流
    private OutputStream outputStream;
    //    Socket输入流
    private InputStream inputStream;
    //    连接回调
    private OnServerConnectedCallbackBlock connectedCallback;
    //    断开连接回调(连接失败)
    private OnServerDisconnectedCallbackBlock disconnectedCallback;
    //    接收信息回调
    private OnReceiveCallbackBlock receivedCallback;

    private Context mContext;

    //    构造函数私有化
    private TaskCenter(Context context) {
        super();
        this.mContext = context;
    }
    //    提供一个全局的静态方法
    public static TaskCenter sharedCenter(Context mContext) {
        if (instance == null) {
            synchronized (TaskCenter.class) {
                if (instance == null) {
                    instance = new TaskCenter(mContext);
                }
            }
        }
        return instance;
    }
    /**
     * 通过IP地址(域名)和端口进行连接
     *
     * @param ipAddress  IP地址(域名)
     * @param port       端口
     */
    public void connect(final String ipAddress, final int port) {
        iConnectState = BaseVolume.TCP_CONNECT_STATE_CONNECTTING;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(ipAddress, port);
//                    socket.setSoTimeout (10 * 1000 );//设置超时时间
                    if (isConnected()) {
                        TaskCenter.sharedCenter(mContext).ipAddress = ipAddress;
                        TaskCenter.sharedCenter(mContext).port = port;
                        if (connectedCallback != null) {
                            connectedCallback.callback();
                        }
                        outputStream = socket.getOutputStream();
                        inputStream = socket.getInputStream();
                        Log.e(TAG,"连接成功");
                        iConnectState = BaseVolume.TCP_CONNECT_STATE_CONNECTED;
                        strOldBuffer = "";
                        mContext.sendBroadcast(new Intent(BaseVolume.BROADCAST_TCP_INFO)
                                .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_TCP_CONNECT_CALLBACK)
                                .putExtra(BaseVolume.BROADCAST_TCP_STATUS,true));
                        receive();


                    }else {
                        Log.e(TAG,"连接失败");
                        iConnectState = BaseVolume.TCP_CONNECT_STATE_DISCONNECT;
                        // 局域网离线啦！
                        mContext.sendBroadcast(new Intent(BaseVolume.BROADCAST_TCP_INFO)
                                .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_TCP_CONNECT_CALLBACK)
                                .putExtra(BaseVolume.BROADCAST_TCP_STATUS,false)
                                .putExtra(BaseVolume.BROADCAST_MSG,"连接失败"));
                        if (disconnectedCallback != null) {
                            disconnectedCallback.callback(new IOException("连接失败"));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG,"连接异常");
                    iConnectState = BaseVolume.TCP_CONNECT_STATE_DISCONNECT;
                    // 局域网离线啦！
                    mContext.sendBroadcast(new Intent(BaseVolume.BROADCAST_TCP_INFO)
                            .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_TCP_CONNECT_CALLBACK)
                            .putExtra(BaseVolume.BROADCAST_TCP_STATUS,false)
                            .putExtra(BaseVolume.BROADCAST_MSG,e.getMessage()));
                    if (disconnectedCallback != null) {
                        disconnectedCallback.callback(e);
                    }
                }
            }
        });
        thread.start();
    }
    /**
     * 判断是否连接
     */
    public boolean isConnected() {
        if (socket == null) {
            return false;
        }
        return socket.isConnected();
    }
    /**
     * 连接
     */
    public void connect() {
        connect(ipAddress,port);
    }
    /**
     * 断开连接
     */
    public void disconnect() {
        if (isConnected()) {
            try {
                iConnectState = BaseVolume.TCP_CONNECT_STATE_DISCONNECT;
                if (outputStream != null) {
                    outputStream.close();
                }
                socket.close();
                if (socket.isClosed()) {
                    iConnectState = BaseVolume.TCP_CONNECT_STATE_DISCONNECT;
                    if (disconnectedCallback != null) {
                        disconnectedCallback.callback(new IOException("断开连接"));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 接收数据
     */
    public void receive() {
        while (isConnected() && iConnectState == BaseVolume.TCP_CONNECT_STATE_CONNECTED ) {
            int size;
            try {
                /**得到的是16进制数，需要进行解析*/
                byte[] buffer = new byte[1024];
//                获取接收到的字节和字节数
                size = inputStream.read(buffer);//null data -1 , zrd serial rule size default 10
                if (size > 0) {
                    byte[] data = new byte[size];
                    System.arraycopy(buffer, 0, data, 0, size);
                    String str = new String(data);
                    Log.e(TAG, "接收局域网，数据：" + str);
                    checkData(str);
                }
                else {
//                    Log.e(TAG, "接收局域网，size："+size);
                }
            }
            catch (IOException e) {
                Log.e(TAG,"连接异常e:"+e.getMessage());
                // 局域网离线啦！
                mContext.sendBroadcast(new Intent(BaseVolume.BROADCAST_TCP_INFO)
                        .putExtra(BaseVolume.BROADCAST_TYPE,BaseVolume.BROADCAST_TCP_CONNECT_CALLBACK)
                        .putExtra(BaseVolume.BROADCAST_TCP_STATUS,false)
                        .putExtra(BaseVolume.BROADCAST_MSG,e.getMessage()));
            }
        }
    }

    private String strOldBuffer = "";
    private void checkData(String data) {
        strOldBuffer = strOldBuffer + data;
        while ((strOldBuffer.indexOf(BaseVolume.COMMAND_END)) > 0) {
            // 发现有数据尾\r\n，则说明存在有效数据
            int iEndIndex = strOldBuffer.indexOf(BaseVolume.COMMAND_END);
            if (iEndIndex >= 0) {
                String strGood = strOldBuffer.substring(0,iEndIndex+2);// +2,是为了截取带包尾的完整数据
//                Log.e("数据", "有效数据："+strGood);
                // 解析数据
                DataAnalysisHelper.Companion.getInstance(mContext).startDataAnalysis(strGood);
                strOldBuffer = strOldBuffer.substring(iEndIndex+2);
            }
        }
    }


    String strNowSendData = "";

    /**
     * 发送数据
     *
     * @param data  数据
     */
    /**
     * send string cmd to serial
     */
    public void sendHexText(String cmd) {
        Log.e(TAG, "发送数据："+cmd);
        strNowSendData = cmd;
        byte[] mBuffer = cmd.getBytes();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (socket != null) {
                    try {
                        outputStream.write(mBuffer);
                        outputStream.flush();
                        strNowSendData = "";
                        Log.e(TAG,"发送成功");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG,"发送失败e:"+e.getMessage());
                        // 通道已经被断开
                        if (e.getMessage().equals("Broken pipe")) {
                            disconnect();
                            connect();
                        }
                    }
                } else {
                    connect();
                }
            }
        }).start();

    }
    /**
     * 回调声明
     */
    public interface OnServerConnectedCallbackBlock {
        void callback();
    }
    public interface OnServerDisconnectedCallbackBlock {
        void callback(IOException e);
    }
    public interface OnReceiveCallbackBlock {
        void callback(String receicedMessage);
    }

    public void setConnectedCallback(OnServerConnectedCallbackBlock connectedCallback) {
        this.connectedCallback = connectedCallback;
    }

    public void setDisconnectedCallback(OnServerDisconnectedCallbackBlock disconnectedCallback) {
        this.disconnectedCallback = disconnectedCallback;
    }

    public void setReceivedCallback(OnReceiveCallbackBlock receivedCallback) {
        this.receivedCallback = receivedCallback;
    }
    /**
     * 移除回调
     */
    private void removeCallback() {
        connectedCallback = null;
        disconnectedCallback = null;
        receivedCallback = null;
    }

}
