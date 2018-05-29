package tp.skt.example;

/**
 * Configuration
 *
 * Copyright (C) 2017. SK Telecom, All Rights Reserved.
 * Written 2017, by SK Telecom
 */
public class Configuration {
    public static final String MQTT_HOST = "tcp://mqtt.sktiot.com";
    public static final String MQTT_SECURE_HOST = "ssl://mqtt.sktiot.com";

    public static final String URL_SEARCH_DEFAULT = "https://thingplugtest.sktiot.com:9443";
    public static final String URL_SEARCH_DEVICE = "/ThingPlug?division=searchDevice&function=myDevice&startIndex=1&countPerPage=50";

    public static final String CSEBASE = "thingplug";
    public static final String ONEM2M_TO = "/thingplug/v1_0";
    public static final String ONEM2M_NODEID = "sjs1210_android";
    public static final String ONEM2M_PASSCODE = "123456";
    public static final String ONEM2M_TARGET_DEVICE = "sjs1210_IoT_EM";

    public static final String CONTAINER_NAME_LATITUDE = "latitude";
    public static final String CONTAINER_NAME_LONGITUDE ="longitude";
    public static final String MGMTCMD_NAME = ONEM2M_NODEID + "_DevReset";
    public static final String AREANWKINFO_NAME = ONEM2M_NODEID + "_areaNwkInfo_01";
    public static final String LOCATIONPOLICY_NAME = ONEM2M_NODEID + "_locationPolicy_01";
    public static final String AE_NAME = ONEM2M_NODEID + "_AE_01";

    public static final String CMT_DEVRESET = "DevReset";
    public static final String CMT_REPPERCHANGE = "RepPerChange";
    public static final String CMT_REPIMMEDIATE = "RepImmediate";
    public static final String CMT_TAKEPHOTO = "TakePhoto";
    public static final String CMT_LEDCONTROL = "LEDControl";

    public static final String ACCOUNT_ID = "sjs1210";
    public static final String UKEY = "WEZsQytadnBaOGNWandsMzEvK0ZYVlpMbm5QVXNsYmhGb0ZvU20vbVVGTFlCUEVZNGdFZDdhaHRoTDNTeHpYWA==";
}
