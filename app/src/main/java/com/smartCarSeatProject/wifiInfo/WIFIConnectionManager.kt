package com.smartCarSeatProject.wifiInfo

import android.content.Context
import android.net.wifi.*
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import com.smartCarSeatProject.data.BaseVolume

import java.util.ArrayList
import java.util.HashMap

class WIFIConnectionManager(private val mContext: Context) {
    var wifiManager: WifiManager? = null
        private set
    private var wifiInfo: WifiInfo? = null
    private val networkId: Int = 0
    private var wifiConfigurationList: List<WifiConfiguration>? = null

    /**
     * 获取当前连接的Wifi名称
     * @return
     */
    val nowConnectWifi: String
        get() {
            wifiInfo = wifiManager!!.connectionInfo
            var nowSSID = wifiInfo!!.ssid
            if (!nowSSID.equals(""))
                nowSSID = nowSSID.substring(1,nowSSID.length - 1)
            return nowSSID
        }

    /**
     * 获取附件的热点集合
     * @return
     */
    //开始扫描AP
    val allWifiList: ArrayList<ScanResult>
        get() {
            wifiManager!!.startScan()
            val scanWifiList = wifiManager!!.scanResults
            val wifiList = ArrayList<ScanResult>()
            if (scanWifiList != null && scanWifiList.size > 0) {
                val signalStrength = HashMap<String, Int>()
                for (i in scanWifiList.indices) {
                    val scanResult = scanWifiList[i]
                    Log.e("MainActivity", "搜索的wifi-ssid:" + scanResult.SSID)
                    if (!scanResult.SSID.isEmpty() && scanResult.SSID.indexOf(BaseVolume.WIFI_SIGN) >= 0) {
                        val key = scanResult.SSID + " " + scanResult.capabilities
                        if (!signalStrength.containsKey(key)) {
                            signalStrength[key] = i
                            wifiList.add(scanResult)
                        }
                    }
                }
            } else {
                Log.e("MainActivity", "没有搜索到wifi")
            }
            return wifiList
        }


    /**
     * 获取本机的ip地址
     */
    val localIp: String?
        get() = convertIp(wifiManager!!.connectionInfo.ipAddress)


    init {
        wifiManager = mContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    }

    /**
     * 连接wifi
     */
    fun startConnect(SSID: String, PASSW: String) {
        var netId = -1
        if (removeWifi(SSID)) {
            Log.e("sin", "移除,新建config")
            netId = wifiManager!!.addNetwork(createWifiInfo(SSID, PASSW))
        } else {
            if (getExitsWifiConfig(SSID) != null) {
                Log.e("sin", "这个wifi是连接过")
                netId = getExitsWifiConfig(SSID)!!.networkId
            } else {
                Log.e("sin", "没连接过的，新建一个wifi配置 ")
                netId = wifiManager!!.addNetwork(createWifiInfo(SSID, PASSW))
            }
        }

        Log.e("sin", "netId: $netId")

        val b = wifiManager!!.enableNetwork(netId, true)  //  无论咋这都会返回true；
        Log.e("sin", "ssss: $b")
        if (!b) {
            Log.e("SSSSSSSSSSSS", "-0.0-")
            //  如果这里失败，再从新获取manager  重新配置config；
            wifiManager = mContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            // 配置config

        } else {
            Log.e("SIN", "success")
        }


    }

    /**
     * 存在过的wifiConfiguration
     */
    fun getExitsWifiConfig(SSID: String): WifiConfiguration? {
        wifiConfigurationList = wifiManager!!.configuredNetworks
        for (wifiConfiguration in wifiConfigurationList!!) {
            if (wifiConfiguration.SSID == "\"" + SSID + "\"") {
                return wifiConfiguration
            }
        }
        return null
    }


    fun removeWifi(netId: Int): Boolean {
        //
        return wifiManager!!.removeNetwork(netId)
    }

    /**
     * config里存在； 在mWifiManager移除；
     */
    fun removeWifi(SSID: String): Boolean {
        return if (getExitsWifiConfig(SSID) != null) {
            removeWifi(getExitsWifiConfig(SSID)!!.networkId)
        } else {
            false
        }
    }


    /**
     * CHUANJian
     *
     * @param SSID
     * @param password
     * @return
     */
    fun createWifiInfo(SSID: String, password: String): WifiConfiguration? {
        val config = WifiConfiguration()

        if (config != null) {
            config.allowedAuthAlgorithms.clear()
            config.allowedGroupCiphers.clear()
            config.allowedKeyManagement.clear()
            config.allowedPairwiseCiphers.clear()
            config.allowedProtocols.clear()
            config.SSID = "\"" + SSID + "\""

            //如果有相同配置的，就先删除
            val tempConfig = getExitsWifiConfig(SSID)
            if (tempConfig != null) {
                wifiManager!!.removeNetwork(tempConfig.networkId)
                wifiManager!!.saveConfiguration()
            }
            config.preSharedKey = "\"" + password + "\""
            config.hiddenSSID = true
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            config.status = WifiConfiguration.Status.ENABLED

            Log.e("sin", "config: " + config.SSID + "      config: " + config.toString())
            return config
        } else {

            Log.e("SSS", "WOCAO   NULL  ！！！")
            return null
        }

    }

    /**
     * 是否已连接指定wifi
     */
//    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    fun isConnected(ssid: String): Boolean {
        val wifiInfo = wifiManager!!.connectionInfo ?: return false
        when (wifiInfo.supplicantState) {
            SupplicantState.AUTHENTICATING, SupplicantState.ASSOCIATING, SupplicantState.ASSOCIATED, SupplicantState.FOUR_WAY_HANDSHAKE, SupplicantState.GROUP_HANDSHAKE, SupplicantState.COMPLETED -> return wifiInfo.ssid.replace("\"", "") == ssid
            else -> return false
        }
    }

    /**
     * 打开WiFi
     * @return
     */
    fun openWifi(): Boolean {
        var opened = true
        if (!wifiManager!!.isWifiEnabled) {
            opened = wifiManager!!.setWifiEnabled(true)
        }
        return opened
    }

    /**
     * 关闭wifi
     * @return
     */
    fun closeWifi(): Boolean {
        var closed = true
        if (wifiManager!!.isWifiEnabled) {
            closed = wifiManager!!.setWifiEnabled(false)
        }
        return closed
    }

    private fun convertIp(ipAddress: Int): String? {
        return if (ipAddress == 0) null else (ipAddress and 0xff).toString() + "." + (ipAddress shr 8 and 0xff) + "." + (ipAddress shr 16 and 0xff) + "." + (ipAddress shr 24 and 0xff)
    }

    companion object {

        private var sInstance: WIFIConnectionManager? = null

        fun getInstance(context: Context?): WIFIConnectionManager? {
            if (sInstance == null) {
                synchronized(WIFIConnectionManager::class.java) {
                    if (sInstance == null) {
                        sInstance = WIFIConnectionManager(context!!)
                    }
                }
            }
            return sInstance
        }
    }


}
