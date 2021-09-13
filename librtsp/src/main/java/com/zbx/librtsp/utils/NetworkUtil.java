package com.zbx.librtsp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import androidx.annotation.RequiresPermission;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Locale;

public class NetworkUtil {
    public static final String TAG = "NetWork";

    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    public static String displayIpAddress(Context context) {
        Context applicationContext = context.getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isAvailable()) {
            int type = activeNetworkInfo.getType();
            switch (type) {
                case ConnectivityManager.TYPE_ETHERNET:
                    return getIpFromEthernet();
                case ConnectivityManager.TYPE_WIFI:
                    WifiManager wifiManager = (WifiManager) applicationContext.getSystemService(Context.WIFI_SERVICE);
                    return getIpFromWifi(wifiManager);
                case ConnectivityManager.TYPE_MOBILE:
                    return getIpFromMobile(true);
            }
        }
        return "";
    }

    public static String getIpFromEthernet() {
        try {
            // 获取本地设备的所有网络接口
            Enumeration<NetworkInterface> enumerationNi = NetworkInterface.getNetworkInterfaces();
            while (enumerationNi.hasMoreElements()) {
                NetworkInterface networkInterface = enumerationNi.nextElement();
                String interfaceName = networkInterface.getDisplayName();
                LogUtil.d(TAG, "网络名字: " + interfaceName);

                // 如果是有限网卡
                if (interfaceName.equals("eth0")) {
                    Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses();
                    while (enumIpAddr.hasMoreElements()) {
                        // 返回枚举集合中的下一个IP地址信息
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        // 不是回环地址，并且是ipv4的地址
                        if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                            LogUtil.d(TAG, inetAddress.getHostAddress());
                            String ipAddress = inetAddress.getHostAddress();
                            LogUtil.d("tag", "IP: " + ipAddress);
                            return ipAddress;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    @RequiresPermission(android.Manifest.permission.ACCESS_WIFI_STATE)
    public static String getIpFromWifi(WifiManager wifiManager) {
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info != null && info.getNetworkId() > -1) {
            int i = info.getIpAddress();
            return String.format(Locale.ENGLISH, "%d.%d.%d.%d", i & 0xff, i >> 8 & 0xff, i >> 16 & 0xff, i >> 24 & 0xff);
        }
        return "";
    }

    public static String getIpFromMobile(final boolean useIPv4) {
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            LinkedList<InetAddress> adds = new LinkedList<>();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                // To prevent phone of xiaomi return "10.0.2.15"
                if (!ni.isUp() || ni.isLoopback()) continue;
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    adds.addFirst(addresses.nextElement());
                }
            }
            for (InetAddress add : adds) {
                if (!add.isLoopbackAddress()) {
                    String hostAddress = add.getHostAddress();
                    boolean isIPv4 = hostAddress.indexOf(':') < 0;
                    if (useIPv4) {
                        if (isIPv4) return hostAddress;
                    } else {
                        if (!isIPv4) {
                            int index = hostAddress.indexOf('%');
                            return index < 0
                                    ? hostAddress.toUpperCase()
                                    : hostAddress.substring(0, index).toUpperCase();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }
}
