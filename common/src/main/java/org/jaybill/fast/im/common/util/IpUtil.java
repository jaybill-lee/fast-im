package org.jaybill.fast.im.common.util;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;

@Slf4j
public class IpUtil {
    private static String LOCAL_IP;
    static {
        try {
            NetworkInterface networkInterface = NetworkInterface.getByName("eth0");
            if (networkInterface != null) {
                LOCAL_IP = networkInterface.getInetAddresses().nextElement().getHostAddress();
                log.info("eth0 IP Address: {}", LOCAL_IP);
            } else {
                log.info("eth0 interface not found");
                LOCAL_IP = InetAddress.getLocalHost().getHostAddress();
            }
        } catch (Exception e) {
            log.error("fail to get local ip address");
        }
    }

    public static String getLocalIp() {
        return LOCAL_IP;
    }
}
