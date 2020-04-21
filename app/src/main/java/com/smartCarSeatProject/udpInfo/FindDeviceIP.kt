package com.smartCarSeatProject.udpInfo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.DhcpInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log

import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class FindDeviceIP {

    private var con: Context? = null
    private val DEFAULT_PORT = 988
    private var udpSocket: DatagramSocket? = null
    private val buffer = ByteArray(MAX_DATA_PACKET_LENGTH)


    private//将整型转换为IP
    val udpip: String
        get() {
            @SuppressLint("WrongConstant") val my_wifiManager = con!!.getSystemService("wifi") as WifiManager
            val dhcpInfo = my_wifiManager.dhcpInfo
            val netmask = dhcpInfo.netmask
            val I = dhcpInfo.ipAddress
            val B = I and netmask or netmask.inv()
            return intToIp(B)
        }

    fun startFindCommand(context: Context, handler: Handler) {
        con = context
        Thread(Runnable {
            var dataPacket: DatagramPacket? = null
            //		    		String udpIP = "192.168.16.255";

            var udpIP = getUdpServiceIP(con)
            if (udpIP == null)
                udpIP = "255.255.255.255"
            Log.e("FindDeviceIP", "UDP的IP：$udpIP")
            try {
                if (udpSocket == null)
                    udpSocket = DatagramSocket()
                var broadcastAddr: InetAddress? = null
                val order = "hlk222"
                Log.e("FindDeviceIP", "UDP发送数据：$order")
                val data = order.toByteArray(charset("utf8"))
                broadcastAddr = InetAddress.getByName(udpIP)
                dataPacket = DatagramPacket(data, data.size, broadcastAddr,
                        DEFAULT_PORT)
                udpSocket!!.send(dataPacket)
                while (true) {
                    val dataReceive = ByteArray(1024)
                    val packetReceive = DatagramPacket(dataReceive,
                            dataReceive.size)
                    udpSocket!!.soTimeout = 1000 * 5
                    udpSocket!!.receive(packetReceive)
                    val udpresult = String(packetReceive.data,
                            packetReceive.offset, packetReceive.length)
                    val resultIP = packetReceive.address.hostAddress
                    Log.e("FindDeviceIP", "返回数据：$udpresult+$resultIP")

                    val msg = Message()
                    msg.what = FindDeivceIPReuslt
                    val bundle = Bundle()
                    bundle.putString("IP", resultIP)
                    bundle.putString("Mac", "Mac")
                    bundle.putString("Name", "Name")
                    msg.data = bundle
                    handler.sendMessage(msg)


                }

            } catch (e: IOException) {

                e.printStackTrace()

            }
        }).start()

    }

    private fun getUdpServiceIP(context: Context?): String? {
        var udpServiceIP = ""
        val ip = getIP(context!!)
        if (ip == null)
            return ip
        if (ip != null) {
            val strarray = ip.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            for (i in 0 until strarray.size - 1) {
                udpServiceIP += strarray[i] + "."
            }
            udpServiceIP += "255"
        }
        return udpServiceIP
    }

    private fun getIP(context: Context): String? {
        val wifiService = context.getSystemService(WIFI_SERVICE) as WifiManager
        val wifiinfo = wifiService.connectionInfo
        val wifiAd = wifiinfo.ipAddress
        return if (wifiAd == 0) null else intToIp(wifiAd)
    }

    private fun intToIp(i: Int): String {
        return ((i and 0xFF).toString() + "." + (i shr 8 and 0xFF) + "." + (i shr 16 and 0xFF)
                + "." + (i shr 24 and 0xFF))
    }

    companion object {


        val FindDeivceIPReuslt = 6688
        private val MAX_DATA_PACKET_LENGTH = 40
    }


}
