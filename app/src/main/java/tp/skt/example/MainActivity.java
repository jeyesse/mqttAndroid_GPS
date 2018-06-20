package tp.skt.example;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.simpleframework.xml.stream.NodeMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import google.map.MapsActivity;
import tp.skt.onem2m.api.IMQTT;
import tp.skt.onem2m.api.MQTTProcessor;
import tp.skt.onem2m.api.oneM2MAPI;
import tp.skt.onem2m.binder.mqtt_v1_1.Binder;
import tp.skt.onem2m.binder.mqtt_v1_1.Definitions;
import tp.skt.onem2m.binder.mqtt_v1_1.Definitions.Operation;
import tp.skt.onem2m.binder.mqtt_v1_1.control.execInstanceControl;
import tp.skt.onem2m.binder.mqtt_v1_1.request.AE;
import tp.skt.onem2m.binder.mqtt_v1_1.request.CSEBase;
import tp.skt.onem2m.binder.mqtt_v1_1.request.areaNwkInfo;
import tp.skt.onem2m.binder.mqtt_v1_1.request.container;
import tp.skt.onem2m.binder.mqtt_v1_1.request.contentInstance;
import tp.skt.onem2m.binder.mqtt_v1_1.request.execInstance;
import tp.skt.onem2m.binder.mqtt_v1_1.request.locationPolicy;
import tp.skt.onem2m.binder.mqtt_v1_1.request.mgmtCmd;
import tp.skt.onem2m.binder.mqtt_v1_1.request.node;
import tp.skt.onem2m.binder.mqtt_v1_1.request.remoteCSE;
import tp.skt.onem2m.binder.mqtt_v1_1.response.AEResponse;
import tp.skt.onem2m.binder.mqtt_v1_1.response.CSEBaseResponse;
import tp.skt.onem2m.binder.mqtt_v1_1.response.ResponseBase;
import tp.skt.onem2m.binder.mqtt_v1_1.response.areaNwkInfoResponse;
import tp.skt.onem2m.binder.mqtt_v1_1.response.containerResponse;
import tp.skt.onem2m.binder.mqtt_v1_1.response.contentInstanceResponse;
import tp.skt.onem2m.binder.mqtt_v1_1.response.execInstanceResponse;
import tp.skt.onem2m.binder.mqtt_v1_1.response.latestResponse;
import tp.skt.onem2m.binder.mqtt_v1_1.response.locationPolicyResponse;
import tp.skt.onem2m.binder.mqtt_v1_1.response.mgmtCmdResponse;
import tp.skt.onem2m.binder.mqtt_v1_1.response.nodeResponse;
import tp.skt.onem2m.binder.mqtt_v1_1.response.remoteCSEResponse;
import tp.skt.onem2m.binder.mqtt_v1_1.response.subscriptionResponse;
import tp.skt.onem2m.net.mqtt.MQTTCallback;
import tp.skt.onem2m.net.mqtt.MQTTClient;
import tp.skt.onem2m.net.mqtt.MQTTConfiguration;

import static tp.skt.example.Configuration.UKEY;
import static tp.skt.example.Configuration.URL_SEARCH_DEFAULT;
import static tp.skt.example.Configuration.URL_SEARCH_DEVICE;

/**
 * MainActivity
 * <p>
 * Copyright (C) 2017. SK Telecom, All Rights Reserved.
 * Written 2017, by SK Telecom
 */
public class MainActivity extends Activity {
    private MQTTClient MQTTClient;

    private IMQTT mqttService;
    MQTTConfiguration config;

    Application app = getApplication();

    // values
    private nodeResponse nodeRes;
    private remoteCSEResponse device;
    private areaNwkInfoResponse areaNwkInfoRes;
    private containerResponse sensor;
    private contentInstanceResponse sensorInfo;
    private mgmtCmdResponse control;
    private locationPolicyResponse locationPolicyRes;
    private AEResponse AERes;

    /* subscription data */
    private String subPath;
    private String[] subPathData;
    private String subDeviceID;
    private String subContainer;
    HashMap<String, NodeData> nodeMap = new HashMap<String, NodeData>();
    HashMap<String, ArrayList> latestData = new HashMap<>();

    private final String TAG = "TP_SDK_SAMPLE_APP";

    private long mLastClickTime;

    // status
    private final int DISCONNECTED = 0;
    private final int SUBSCRIBED = 1;
    private final int REGISTERED = 2;
    private int mStatus = DISCONNECTED;
    boolean showTextView = true;

    // client id
    private String mClientID = "";

    // binder
    private Binder mBinder;

    /**
     * @return 0 ~ 99999
     */
    private int getRandomNumber() {
        Random id = new Random();
        id.setSeed(System.currentTimeMillis());
        return id.nextInt(100000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mClientID = Configuration.ACCOUNT_ID + "_" + getRandomNumber();
        mClientID = Configuration.ONEM2M_NODEID;
        MQTTClient.Builder builder = new MQTTClient.Builder(MainActivity.this)
                .baseUrl(Configuration.MQTT_SECURE_HOST)
                .clientId(mClientID)
                .userName(Configuration.ACCOUNT_ID)
                .password(UKEY)
                .setLog(true);

        MQTTClient = builder.build();
        TextView textView = findViewById(R.id.status);
        textView.setVisibility(View.INVISIBLE);
    }

    public void onClick(View view) {
        // prevent double tap
        if (SystemClock.elapsedRealtime() - mLastClickTime < 3000 || MQTTClient == null) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        int viewId = view.getId();

        switch (viewId) {
            case R.id.log:
                TextView textView = findViewById(R.id.status);
                if (showTextView == true) {
                    textView.setVisibility(view.INVISIBLE);
                    showTextView = false;
                } else {
                    textView.setVisibility(view.VISIBLE);
                    showTextView = true;
                }
                break;
            case R.id.location:
                Intent intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
                break;
            case R.id.disconnect:
                if (mStatus > DISCONNECTED) {
                    MQTTClient.disconnect();
                }
                break;
//            case R.id.update:
//                if (mStatus == REGISTERED) {
//                    update();
//                }
//                break;
            case R.id.connect:
                if (mStatus > DISCONNECTED) {
                    return;
                }
                config = new MQTTConfiguration(Configuration.CSEBASE,
                        Configuration.ONEM2M_TO,
                        Configuration.ONEM2M_NODEID,
                        mClientID);
                mBinder = new Binder();
                mqttService = MQTTClient.connect(IMQTT.class, config, mBinder, new MQTTProcessor.MQTTListener() {
                            @Override
                            public void onPush(execInstanceControl control) {
                                StringBuilder message = new StringBuilder();
                                message.append("sr : ").append(control.getSr()).append("\n").
                                        append("con : ").append(control.getCon());
                                Log.i("GET SR&CON", message.toString());
                                /*
                                * susbscription 사용 시, 정보를 저장하는 부분
                                *
                                showToast(message.toString(), Toast.LENGTH_LONG);
                                controlResult(control.getNm(), control.getRi());
                                subPath = control.getSr();
                                subPathData = subPath.split("/");
                                int index = subPathData[3].indexOf("-");
                                subDeviceID = subPathData[3].substring(index + 1);
                                index = subPathData[4].indexOf("-");
                                subContainer = subPathData[4].substring(index + 1);
                                NodeData nodeData;
                                if (nodeMap.get(subDeviceID) == null) {
                                    nodeData = new NodeData();
                                } else {
                                    nodeData = nodeMap.get(subDeviceID);
                                }
                                switch (subContainer) {
                                    case "Geolocation_latitude":
                                        nodeData.setLatitude(control.getCon());
                                        break;
                                    case "Geolocation_longitude":
                                        nodeData.setLongitude(control.getCon());
                                        break;
                                    case "Smoke":
                                        nodeData.setSmoke(control.getCon());
                                        break;
                                }
                                nodeData.setTime(control.getLt());
                                nodeMap.put(subDeviceID, nodeData);
                                for (HashMap.Entry node : nodeMap.entrySet()) {
                                    NodeData nodeData1 = (NodeData) node.getValue();
                                    //Log.i("node data Map", node.getKey() + " , " + nodeData1.toString());
                                    setStatus("node Data : \n" + nodeData1.toString());
                                }
                                */
                            }

                            @Override
                            public void onDisconnected() {
                                setStatus(DISCONNECTED);
                                showToast("disconnected!", Toast.LENGTH_SHORT);
                            }

                            @Override
                            public void onDisconnectFailure() {
                                showToast("disconnect fail!", Toast.LENGTH_SHORT);
                            }

                            @Override
                            public void onSubscribed() {
                                setStatus(SUBSCRIBED);
                                showToast("subscribed!", Toast.LENGTH_SHORT);
                                getLatestInstance();
                            }

                            @Override
                            public void onSubscribeFailure() {
                                MQTTClient.disconnect();
                                showToast("subscribe fail!", Toast.LENGTH_SHORT);
                            }

                            @Override
                            public void onConnected() {
                                showToast("connected!", Toast.LENGTH_SHORT);
                                registerDevice();
                            }

                            @Override
                            public void onConnectFailure() {
                                showToast("connect fail!", Toast.LENGTH_SHORT);
                            }

                            @Override
                            public void onConnectionLost() {
                                setStatus(DISCONNECTED);
                                showToast("connection lost!", Toast.LENGTH_SHORT);
                            }

                            @Override
                            public void onDelivered() {

                            }
                        }
                );
                break;
        }
    }

    private class DeviceListTask extends AsyncTask<Void, Void, List<DeviceInfo>> {

        @Override
        protected List<DeviceInfo> doInBackground(Void... voids) {
            // search device list
            ArrayList<DeviceInfo> deviceList = searchDevice(UKEY);
            return deviceList;
        }
    }

    private ArrayList<DeviceInfo> searchDevice(String ukey) {
        ArrayList<DeviceInfo> diviceList = new ArrayList<DeviceInfo>();
        try {
            URL url = new URL(URL_SEARCH_DEFAULT + URL_SEARCH_DEVICE);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.setRequestMethod("GET");
            request.setRequestProperty("uKey", ukey);
            request.setRequestProperty("locale", "ko");

            int responseCode = request.getResponseCode();
            Log.i(TAG, "[" + url.toString() + "]" + "responseCode : " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream is = request.getInputStream();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] byteBuffer = new byte[1024];
                byte[] byteData = null;
                int nLength = 0;
                while ((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                    baos.write(byteBuffer, 0, nLength);
                }
                byteData = baos.toByteArray();
                String response = new String(byteData);

                try {
                    XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
                    XmlPullParser xmlParser = xmlFactoryObject.newPullParser();
                    xmlParser.setInput(new StringReader(response));
                    String tagName = null;
                    String device_id = "";
                    String device_name = "";
                    while (xmlParser.getEventType() != XmlPullParser.END_DOCUMENT) {
                        if (xmlParser.getEventType() == XmlPullParser.START_TAG) {
                            if (xmlParser.getName().equalsIgnoreCase("device_Id") == true
                                    || xmlParser.getName().equalsIgnoreCase("device_Name") == true) {
                                tagName = xmlParser.getName();
                            }
                        } else if (xmlParser.getEventType() == XmlPullParser.TEXT) {
                            if (tagName != null) {
                                if (tagName.equalsIgnoreCase("device_Id") == true) {
                                    device_id = xmlParser.getText();
                                } else if (tagName.equalsIgnoreCase("device_Name") == true) {
                                    device_name = xmlParser.getText();
                                }
                                if (!device_id.isEmpty() && !device_name.isEmpty()) {
                                    DeviceInfo deviceInfo = new DeviceInfo(device_id);
                                    deviceInfo.setDeviceName(device_name);
                                    diviceList.add(deviceInfo);
                                    device_id = "";
                                    device_name = "";
                                }
                                tagName = "";
                            }
                        }
                        xmlParser.next();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                request.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return diviceList;
    }

    private void displayDeviceList(List<DeviceInfo> deviceList) {
        TextView textViewStatus = (TextView) findViewById(R.id.status);
        StringBuilder stringBuilder = new StringBuilder();
        for (DeviceInfo info : deviceList) stringBuilder.append(info.deviceId + "\n");
        textViewStatus.setText(stringBuilder);
    }

    public class DeviceInfo {
        private String deviceId;
        private String deviceName;

        public DeviceInfo(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * @param response
     */
    private void showResponseMessage(String title, ResponseBase response) {
        StringBuilder message = new StringBuilder();
        message.append(title == null ? "[response]\n" : "[" + title + "]\n").
                append("ri : ").append(response.ri).append("\n").
                append("rsc : ").append(response.rsc);
        if (TextUtils.isEmpty(response.RSM) == false) {
            message.append("\nRSM : ").append(response.RSM);
        }
        showToast(message.toString(), Toast.LENGTH_LONG);
    }

    /**
     * show toast
     *
     * @param message
     */
    private void showToast(String message, int duration) {
        Toast toast = Toast.makeText(this, message, duration);
        toast.show();
    }

    private void setStatus(int status) {
        mStatus = status;

        String text = "";
        switch (status) {
            case SUBSCRIBED:
                String subscribeTopics[] = {"/oneM2M/resp/" + Configuration.ONEM2M_NODEID + "/+", "/oneM2M/req/+/" + Configuration.ONEM2M_NODEID};
                StringBuilder topics = new StringBuilder();
                topics.append("SUBSCRIBED\n").
                        append("ST : ").append(Arrays.toString(subscribeTopics)).append("\n").
                        append("PT : ").append("/oneM2M/req/" + Configuration.ONEM2M_NODEID + "/ThingPlug");
                text = topics.toString();
                break;
            case REGISTERED:
                text = "REGISTERED(nl : " + device.getNl() + ")";
                break;
            default:
                text = "DISCONNECTED";
                break;
        }
        setStatus(text);
    }

    private void setStatus(String text) {
        TextView display = findViewById(R.id.status);
        display.setText(text);
    }

//    private void setDisplay(ResponseBase base) {
//        String xml = mBinder.serialization(base);
//        TextView display = (TextView) findViewById(R.id.display);
//        display.setText(xml);
//    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Simple API guide
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void unregisterDevice() {
        if (device == null) return;
        try {
            node nodeDelete = new node.Builder(Operation.Delete)
                    .dKey(device.dKey).build();

            mqttService.publish(nodeDelete, new MQTTCallback<nodeResponse>() {
                @Override
                public void onResponse(nodeResponse response) {
                    showResponseMessage("node DELETE", response);
                    int responseCode = 0;
                    if (response.rsc != null) {
                        responseCode = Integer.valueOf(response.rsc);
                    }
                    if (responseCode == Definitions.ResponseStatusCode.DELETED) {
                        remoteCSE remoteCSEDelete = new remoteCSE.Builder(Operation.Delete)
                                .dKey(device.dKey).build();
                        try {
                            mqttService.publish(remoteCSEDelete, new MQTTCallback<remoteCSEResponse>() {
                                @Override
                                public void onResponse(remoteCSEResponse response) {
                                    int responseCode = 0;
                                    if (response.rsc != null) {
                                        responseCode = Integer.valueOf(response.rsc);
                                    }
                                    if (responseCode == Definitions.ResponseStatusCode.DELETED) {
                                        setStatus(SUBSCRIBED);
                                    }
                                    showResponseMessage("remoteCSE DELETE", response);
                                }

                                @Override
                                public void onFailure(int errorCode, String message) {
                                    Log.e(TAG, message);
                                    showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                                }
                            });
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    } else {
                        this.onFailure(responseCode, response.RSM);
                    }
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, errorCode + " : " + message);
                    showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (control != null) {
            try {
                mgmtCmd mgmtCmdDelete = new mgmtCmd.Builder(Operation.Delete).
                        nm(control.getRn()).
                        dKey(device.dKey).build();

                mqttService.publish(mgmtCmdDelete, new MQTTCallback<mgmtCmdResponse>() {
                    @Override
                    public void onResponse(mgmtCmdResponse response) {
                        showResponseMessage("mgmtCmd DELETE", response);
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        Log.e(TAG, errorCode + " : " + message);
                        showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * register device
     */
    private void registerDevice() {
        if (mqttService == null) return;
        oneM2MAPI.getInstance().tpRegisterDevice(mqttService, mClientID, Configuration.ONEM2M_PASSCODE,
                "3", "true", new MQTTCallback<remoteCSEResponse>() {
                    @Override
                    public void onResponse(remoteCSEResponse response) {
                        MainActivity.this.device = response;

                        Log.i("node&remoteCSE CREATE", response.toString());
                        //showResponseMessage("node & remoteCSE CREATE", response);
//                        setDisplay(response);
                        //registerSensor();
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        Log.e(TAG, errorCode + " : " + message);
                        //showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                    }
                });
    }


    /**
     * registerSensor
     */
    private void registerSensor() {
        if (device == null) return;
        oneM2MAPI.getInstance().tpRegisterContainer(mqttService, Configuration.CONTAINER_NAME_LATITUDE,
                device.dKey, new MQTTCallback<containerResponse>() {
                    @Override
                    public void onResponse(containerResponse response) {
                        MainActivity.this.sensor = response;
                        showResponseMessage("container CREATE", response);
                        registerControl();
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        Log.e(TAG, errorCode + " : " + message);
                        showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                    }
                });
        oneM2MAPI.getInstance().tpRegisterContainer(mqttService, Configuration.CONTAINER_NAME_LONGITUDE,
                device.dKey, new MQTTCallback<containerResponse>() {

                    @Override
                    public void onResponse(containerResponse response) {
                        MainActivity.this.sensor = response;
                        showResponseMessage("container CREATE", response);
                        registerControl();
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        Log.e(TAG, errorCode + " : " + message);
                        showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                    }
                });

    }

    /*
     * subscribe device
     * ThingPlug에서 Subscription은 2개까지만 허용
     * 게이트웨이용
     */
    private void subscribeDevice() {
        Log.i(TAG, "subscribeDevice called, mqttService=" + mqttService);
        if (mqttService == null) return;
        try {
            final List<DeviceInfo> deviceList = new DeviceListTask().execute().get();

            // display device list
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayDeviceList(deviceList);
                    for (DeviceInfo info : deviceList) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        oneM2MAPI.getInstance().tpSubscription(mqttService, mClientID, info.deviceId,
                                Configuration.CONTAINER_NAME_LATITUDE, UKEY, new MQTTCallback<subscriptionResponse>() {
                                    @Override
                                    public void onResponse(subscriptionResponse response) {
                                        Log.i(TAG, "subscribeDevice::onResponse = " + response);
                                        Log.i("sub Latitude CREATE", response.toString());
                                    }

                                    @Override
                                    public void onFailure(int errorCode, String message) {
                                        Log.e(TAG, "subscribeDevice::onFailure(Latitude) " + errorCode + " : " + message);
                                        //showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                                    }
                                });
                        oneM2MAPI.getInstance().tpSubscription(mqttService, mClientID, info.deviceId,
                                Configuration.CONTAINER_NAME_LONGITUDE, UKEY, new MQTTCallback<subscriptionResponse>() {
                                    @Override
                                    public void onResponse(subscriptionResponse response) {
                                        Log.i(TAG, "subscribeDevice::onResponse = " + response);
                                        Log.i("sub Longitude CREATE", response.toString());
                                    }

                                    @Override
                                    public void onFailure(int errorCode, String message) {
                                        Log.e(TAG, "subscribeDevice::onFailure(Longitude) " + errorCode + " : " + message);
                                        //showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                                    }
                                });
                        oneM2MAPI.getInstance().tpSubscription(mqttService, mClientID, info.deviceId,
                                Configuration.CONTAINER_NAME_SMOKE, UKEY, new MQTTCallback<subscriptionResponse>() {
                                    @Override
                                    public void onResponse(subscriptionResponse response) {
                                        Log.i(TAG, "subscribeDevice::onResponse = " + response);
                                        Log.i("sub Smoke CREATE", response.toString());
                                    }

                                    @Override
                                    public void onFailure(int errorCode, String message) {
                                        Log.e(TAG, "subscribeDevice::onFailure(Longitude) " + errorCode + " : " + message);
                                        //showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                                    }
                                });
                    }
                }
            });

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /*
     * report
     */
    private void report() {
        if (device == null && sensor != null) return;

        oneM2MAPI api = oneM2MAPI.getInstance();

        api.tpAddData(String.valueOf(MapsActivity.latitude));
        api.tpReport(mqttService, Configuration.CONTAINER_NAME_LATITUDE,
                device.dKey, "text", null, true, new MQTTCallback<contentInstanceResponse>() {
                    @Override
                    public void onResponse(contentInstanceResponse response) {
                        MainActivity.this.sensorInfo = response;
                        showResponseMessage("contentInstance CREATE", response);
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        Log.e(TAG, errorCode + " : " + message);
                        showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                    }
                });

        api.tpAddData(String.valueOf(MapsActivity.longitude));
        api.tpReport(mqttService, Configuration.CONTAINER_NAME_LONGITUDE,
                device.dKey, "text", null, true, new MQTTCallback<contentInstanceResponse>() {

                    @Override
                    public void onResponse(contentInstanceResponse response) {

                    }

                    @Override
                    public void onFailure(int errorCode, String message) {

                    }
                });
    }


    /**
     * register control
     */
    private void registerControl() {
        if (device == null) return;

        oneM2MAPI.getInstance().tpRegisterMgmtCmd(mqttService, Configuration.MGMTCMD_NAME,
                device.dKey, Configuration.CMT_DEVRESET, "true", device.getNl(), new MQTTCallback<mgmtCmdResponse>() {
                    @Override
                    public void onResponse(mgmtCmdResponse response) {
                        MainActivity.this.control = response;
                        showResponseMessage("mgmtCmd CREATE", response);
                        setStatus(REGISTERED);
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        Log.e(TAG, errorCode + " : " + message);
                        showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                    }
                });
    }

    /**
     * control result
     */
    private void controlResult(String mgmtCmdName, String resourceId) {
        if (device == null) return;

        oneM2MAPI.getInstance().tpResult(mqttService, mgmtCmdName,
                device.dKey, resourceId, "0", "3", new MQTTCallback<execInstanceResponse>() {

                    @Override
                    public void onResponse(execInstanceResponse response) {
                        showResponseMessage("execInstance UPDATE", response);
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        Log.e(TAG, errorCode + " : " + message);
                        showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                    }
                });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // other API guide
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * CSEBase Retrieve
     */
    private void CSEBaseRetrieve() {
        if (mqttService == null) return;

        try {
            CSEBase CSEBaseRetrieve = new CSEBase.Builder(Operation.Retrieve).build();

            mqttService.publish(CSEBaseRetrieve, new MQTTCallback<CSEBaseResponse>() {
                @Override
                public void onResponse(CSEBaseResponse response) {
                    nodeCreate();
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * node CREATE
     */
    private void nodeCreate() {
        if (mqttService == null) return;

        try {
            node nodeCreate = new node.Builder(Operation.Create).
                    mga("MQTT|" + Configuration.ONEM2M_NODEID).build();

            mqttService.publish(nodeCreate, new MQTTCallback<nodeResponse>() {
                @Override
                public void onResponse(nodeResponse response) {
                    nodeRes = response;
                    nodeRetrieve();
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * node Retrieve
     */
    private void nodeRetrieve() {
        if (mqttService == null) return;

        try {
            node nodeRetrieve = new node.Builder(Operation.Retrieve)
                    .uKey(UKEY).build();

            mqttService.publish(nodeRetrieve, new MQTTCallback<nodeResponse>() {
                @Override
                public void onResponse(nodeResponse response) {
                    remoteCSECreate();
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, errorCode + " : " + message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * remoteCSE CREATE
     */
    private void remoteCSECreate() {
        if (nodeRes == null) return;

        try {
            remoteCSE remoteCSECreate = new remoteCSE.Builder(Operation.Create).
                    passCode(Configuration.ONEM2M_PASSCODE).
                    cst("3").
                    poa("MQTT|" + Configuration.ONEM2M_NODEID).
                    rr("true").
                    nl(nodeRes.getRi()).build();

            mqttService.publish(remoteCSECreate, new MQTTCallback<remoteCSEResponse>() {
                @Override
                public void onResponse(remoteCSEResponse response) {
                    device = response;
                    remoteCSERetrieve();
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * remoteCSE RETRIEVE
     */
    private void remoteCSERetrieve() {
        if (nodeRes == null) return;

        remoteCSE remoteCSERetrieve = new remoteCSE.Builder(Operation.Retrieve)
                .uKey(UKEY).build();
        try {
            mqttService.publish(remoteCSERetrieve, new MQTTCallback<remoteCSEResponse>() {
                @Override
                public void onResponse(remoteCSEResponse response) {
                    AECreate();
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, message);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * AE CREATE
     */
    private void AECreate() {
        if (device == null) return;

        try {
            AE AECreate = new AE.Builder(Operation.Create).
                    dKey(device.dKey).
                    api(Configuration.ONEM2M_NODEID).
                    apn(Configuration.AE_NAME).build();

            mqttService.publish(AECreate, new MQTTCallback<AEResponse>() {
                @Override
                public void onResponse(AEResponse response) {
                    AERes = response;
                    AERetrieve();
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * AE RETRIEVE
     */
    private void AERetrieve() {
        if (AERes == null) return;

        try {
            AE AERetrieve = new AE.Builder(Operation.Retrieve).
                    nm(AERes.getRn()). //Configuration.AE_NAME).
                    uKey(UKEY).build();

            mqttService.publish(AERetrieve, new MQTTCallback<AEResponse>() {
                @Override
                public void onResponse(AEResponse response) {
                    containerCreate();
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, errorCode + " : " + message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * container CREATE
     */
    private void containerCreate() {
        if (device == null) return;

        try {
            container containerCreate = new container.Builder(Operation.Create).
                    nm(Configuration.CONTAINER_NAME_LATITUDE).
                    dKey(device.dKey).
                    lbl("con").build();

            mqttService.publish(containerCreate, new MQTTCallback<containerResponse>() {
                @Override
                public void onResponse(containerResponse response) {
                    sensor = response;
                    containerRetrieve();
//                    areaNwkInfoCreate();
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, errorCode + " : " + message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            container containerCreate = new container.Builder(Operation.Create).
                    nm(Configuration.CONTAINER_NAME_LONGITUDE).
                    dKey(device.dKey).
                    lbl("con").build();

            mqttService.publish(containerCreate, new MQTTCallback<containerResponse>() {
                @Override
                public void onResponse(containerResponse response) {
                    sensor = response;
                    containerRetrieve();
//                    areaNwkInfoCreate();
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, errorCode + " : " + message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * container RETRIEVE
     */
    private void containerRetrieve() {
        if (device == null) return;

        try {
            container containerRetrieve = new container.Builder(Operation.Retrieve).
                    nm(Configuration.CONTAINER_NAME_LATITUDE).
                    uKey(UKEY).build();

            mqttService.publish(containerRetrieve, new MQTTCallback<containerResponse>() {
                @Override
                public void onResponse(containerResponse response) {
                    //
                    mgmtCmdCreate();
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            container containerRetrieve = new container.Builder(Operation.Retrieve).
                    nm(Configuration.CONTAINER_NAME_LONGITUDE).
                    uKey(UKEY).build();

            mqttService.publish(containerRetrieve, new MQTTCallback<containerResponse>() {
                @Override
                public void onResponse(containerResponse response) {
                    //
                    mgmtCmdCreate();
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * mgmtCmd CREATE
     */
    private void mgmtCmdCreate() {
        if (device == null) return;

        try {
            mgmtCmd mgmtCmdCreate = new mgmtCmd.Builder(Operation.Create).
                    nm(Configuration.ONEM2M_NODEID + "_" + Configuration.CMT_DEVRESET).
                    dKey(device.dKey).
                    cmt(Configuration.CMT_DEVRESET).
                    exe("true").
                    ext(device.getNl()).
                    lbl("con").build();

            mqttService.publish(mgmtCmdCreate, new MQTTCallback<mgmtCmdResponse>() {
                @Override
                public void onResponse(mgmtCmdResponse response) {
                    control = response;
                    mgmtCmdRetrieve();
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, errorCode + " : " + message);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * mgmtCmd RETRIEVE
     */
    private void mgmtCmdRetrieve() {
        if (device == null) return;

        try {
            mgmtCmd mgmtCmdRetrieve = new mgmtCmd.Builder(Operation.Retrieve).
                    nm(Configuration.ONEM2M_NODEID + "_" + Configuration.CMT_DEVRESET).
                    uKey(UKEY).build();

            mqttService.publish(mgmtCmdRetrieve, new MQTTCallback<mgmtCmdResponse>() {
                @Override
                public void onResponse(mgmtCmdResponse response) {
                    //
                    areaNwkInfoCreate();
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, errorCode + " : " + message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * contentInstace CREATE
     */
    private void contentInstanceCreate() {
        if (device == null) return;

        try {
            contentInstance contentInstanceCreate = new contentInstance.Builder(Operation.Create).
                    containerName(Configuration.CONTAINER_NAME_LATITUDE).
                    dKey(device.dKey).
                    cnf("text").
                    con("45").build();

            mqttService.publish(contentInstanceCreate, new MQTTCallback<contentInstanceResponse>() {
                @Override
                public void onResponse(contentInstanceResponse response) {
                    sensorInfo = response;
//                    contentInstanceRD(Operation.Retrieve, "oldest");
                    contentInstanceRD(Operation.Retrieve, "contentInstance-" + response.getRi());

//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    contentInstanceRD(Operation.Delete, "contentInstance-" + response.getRi());
//                    contentInstanceRD(Operation.Retrieve, "latest");
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, errorCode + " : " + message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            contentInstance contentInstanceCreate = new contentInstance.Builder(Operation.Create).
                    containerName(Configuration.CONTAINER_NAME_LONGITUDE).
                    dKey(device.dKey).
                    cnf("text").
                    con("45").build();

            mqttService.publish(contentInstanceCreate, new MQTTCallback<contentInstanceResponse>() {
                @Override
                public void onResponse(contentInstanceResponse response) {
                    sensorInfo = response;
//                    contentInstanceRD(Operation.Retrieve, "oldest");
                    contentInstanceRD(Operation.Retrieve, "contentInstance-" + response.getRi());
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, errorCode + " : " + message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * contentInstance RETRIEVE
     */
    private void contentInstanceRD(@Definitions.Operation int op, String suffix) {
        if (device == null) return;

        try {
            contentInstance contentInstanceRetrieve = new contentInstance.Builder(op).
                    containerName(Configuration.CONTAINER_NAME_LATITUDE).
                    uKey(UKEY).
                    dKey(device.dKey).
                    nm(suffix).build();

            mqttService.publish(contentInstanceRetrieve, new MQTTCallback<contentInstanceResponse>() {
                @Override
                public void onResponse(contentInstanceResponse response) {
                    //
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, errorCode + " : " + message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            contentInstance contentInstanceRetrieve = new contentInstance.Builder(op).
                    containerName(Configuration.CONTAINER_NAME_LONGITUDE).
                    uKey(UKEY).
                    dKey(device.dKey).
                    nm(suffix).build();

            mqttService.publish(contentInstanceRetrieve, new MQTTCallback<contentInstanceResponse>() {
                @Override
                public void onResponse(contentInstanceResponse response) {
                    //
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, errorCode + " : " + message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * areaNwkInfo CREATE
     */
    private void areaNwkInfoCreate() {
        if (device == null) return;

        try {
            areaNwkInfo areaNwkInfoCreate = new areaNwkInfo.Builder(Operation.Create).
                    nm(Configuration.AREANWKINFO_NAME).
                    dKey(device.dKey).
                    mgd("1004").
                    ant("type").
                    ldv("").build();

            mqttService.publish(areaNwkInfoCreate, new MQTTCallback<areaNwkInfoResponse>() {
                @Override
                public void onResponse(areaNwkInfoResponse response) {
                    areaNwkInfoRes = response;
                    areaNwkInfoRetrieve();
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, errorCode + " : " + message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * areaNwkInfo Retrieve
     */
    private void areaNwkInfoRetrieve() {
        if (device == null) return;

        try {
            areaNwkInfo areaNwkInfoRetrieve = new areaNwkInfo.Builder(Operation.Retrieve).
                    nm(Configuration.AREANWKINFO_NAME).
                    uKey(UKEY).build();

            mqttService.publish(areaNwkInfoRetrieve, new MQTTCallback<areaNwkInfoResponse>() {
                @Override
                public void onResponse(areaNwkInfoResponse response) {
                    locationPolicyCreate();
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, errorCode + " : " + message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * locationPolicy CREATE
     */
    private void locationPolicyCreate() {
        if (device == null) return;

        try {
            locationPolicy locationPolicyCreate = new locationPolicy.Builder(Operation.Create).
                    nm(Configuration.LOCATIONPOLICY_NAME).
                    dKey(device.dKey).
                    los("2").build();

            mqttService.publish(locationPolicyCreate, new MQTTCallback<locationPolicyResponse>() {
                @Override
                public void onResponse(locationPolicyResponse response) {
                    locationPolicyRes = response;
                    locationPolicyRetrieve();
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, errorCode + " : " + message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * locationPolicy Retrieve
     */
    private void locationPolicyRetrieve() {
        if (device == null) return;

        try {
            locationPolicy locationPolicyRetrieve = new locationPolicy.Builder(Operation.Retrieve).
                    nm(Configuration.LOCATIONPOLICY_NAME).
                    uKey(UKEY).build();

            mqttService.publish(locationPolicyRetrieve, new MQTTCallback<locationPolicyResponse>() {
                @Override
                public void onResponse(locationPolicyResponse response) {
                    contentInstanceCreate();
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, errorCode + " : " + message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * execInstance UPDATE
     */
    private void execInstanceUpdate(String nm, String ri) {
        if (device == null) return;

        try {
            execInstance execInstanceUpdate = new execInstance.Builder(Operation.Update).
                    nm(nm).
                    resourceId(ri).
                    exr("0").
                    exs("3").
                    dKey(device.dKey).build();

            mqttService.publish(execInstanceUpdate, new MQTTCallback<execInstanceResponse>() {
                @Override
                public void onResponse(execInstanceResponse response) {
                    //
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    Log.e(TAG, errorCode + " : " + message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void update() {
        if (sensor != null) {
            try {
                container containerUpdate = new container.Builder(Operation.Update).
                        nm(sensor.getRn()).
                        lbl("event").
                        dKey(device.dKey).build();

                mqttService.publish(containerUpdate, new MQTTCallback<containerResponse>() {
                    @Override
                    public void onResponse(containerResponse response) {
                        showResponseMessage("container Update", response);
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        Log.e(TAG, errorCode + " : " + message);
                        showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (control != null) {
            try {
                mgmtCmd mgmtCmdUpdate = new mgmtCmd.Builder(Operation.Update).
                        nm(control.getRn()).
                        exe("true").
                        uKey(UKEY).build();

                mqttService.publish(mgmtCmdUpdate, new MQTTCallback<mgmtCmdResponse>() {
                    @Override
                    public void onResponse(mgmtCmdResponse response) {
                        showResponseMessage("mgmtCmd Update", response);
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        Log.e(TAG, errorCode + " : " + message);
                        showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (AERes != null) {
            try {
                AE AEUpdate = new AE.Builder(Operation.Update).
                        nm(AERes.getRn()).
                        apn(Configuration.ONEM2M_NODEID + "_AE_02").
                        dKey(device.dKey).build();

                mqttService.publish(AEUpdate, new MQTTCallback<AEResponse>() {
                    @Override
                    public void onResponse(AEResponse response) {
                        showResponseMessage("AE Update", response);
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        Log.e(TAG, errorCode + " : " + message);
                        showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (device != null) {
            remoteCSE remoteCSEUpdate = new remoteCSE.Builder(Operation.Update)
                    .rr("false")
                    .dKey(device.dKey).build();
            try {
                mqttService.publish(remoteCSEUpdate, new MQTTCallback<remoteCSEResponse>() {
                    @Override
                    public void onResponse(remoteCSEResponse response) {
                        showResponseMessage("remoteCSE Update", response);
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        Log.e(TAG, message);
                        showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        if (nodeRes != null) {
            try {
                node nodeUpdate = new node.Builder(Operation.Update)
                        .mga("HTTP|" + Configuration.ONEM2M_NODEID)
                        .dKey(device.dKey).build();

                mqttService.publish(nodeUpdate, new MQTTCallback<nodeResponse>() {
                    @Override
                    public void onResponse(nodeResponse response) {

                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        Log.e(TAG, errorCode + " : " + message);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (areaNwkInfoRes != null) {
            try {
                areaNwkInfo areaNwkInfoUpdate = new areaNwkInfo.Builder(Operation.Update).
                        nm(areaNwkInfoRes.getRn()).
                        ant("type2").
                        ldv("1").
                        dKey(device.dKey).build();

                mqttService.publish(areaNwkInfoUpdate, new MQTTCallback<areaNwkInfoResponse>() {
                    @Override
                    public void onResponse(areaNwkInfoResponse response) {
                        showResponseMessage("areaNwkInfo Update", response);
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        Log.e(TAG, errorCode + " : " + message);
                        showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (locationPolicyRes != null) {
            try {
                locationPolicy locationPolicyUpdate = new locationPolicy.Builder(Operation.Update).
                        nm(locationPolicyRes.getRn()).
                        los("3").
                        dKey(device.dKey).build();

                mqttService.publish(locationPolicyUpdate, new MQTTCallback<locationPolicyResponse>() {
                    @Override
                    public void onResponse(locationPolicyResponse response) {
                        showResponseMessage("locationPolicy Update", response);
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        Log.e(TAG, errorCode + " : " + message);
                        showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void unregister() {
        if (sensorInfo != null) {
            sensorInfo.print();
            try {
                contentInstance contentInstanceDelete = new contentInstance.Builder(Operation.Delete).
                        nm(sensorInfo.getRn()).
                        containerName(Configuration.CONTAINER_NAME_LATITUDE).
                        dKey(device.dKey).build();

                mqttService.publish(contentInstanceDelete, new MQTTCallback<contentInstanceResponse>() {
                    @Override
                    public void onResponse(contentInstanceResponse response) {
                        showResponseMessage("contentInstance DELETE", response);
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        Log.e(TAG, errorCode + " : " + message);
                        showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                contentInstance contentInstanceDelete = new contentInstance.Builder(Operation.Delete).
                        nm(sensorInfo.getRn()).
                        containerName(Configuration.CONTAINER_NAME_LONGITUDE).
                        dKey(device.dKey).build();

                mqttService.publish(contentInstanceDelete, new MQTTCallback<contentInstanceResponse>() {
                    @Override
                    public void onResponse(contentInstanceResponse response) {
                        showResponseMessage("contentInstance DELETE", response);
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        Log.e(TAG, errorCode + " : " + message);
                        showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (sensor != null) {
            try {
                container containerDelete = new container.Builder(Operation.Delete).
                        nm(sensor.getRn()).
                        dKey(device.dKey).build();

                mqttService.publish(containerDelete, new MQTTCallback<containerResponse>() {
                    @Override
                    public void onResponse(containerResponse response) {
                        showResponseMessage("container DELETE", response);
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        Log.e(TAG, errorCode + " : " + message);
                        showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (AERes != null) {
            try {
                AE AEDelete = new AE.Builder(Operation.Delete).
                        nm(AERes.getRn()).
                        dKey(device.dKey).build();

                mqttService.publish(AEDelete, new MQTTCallback<AEResponse>() {
                    @Override
                    public void onResponse(AEResponse response) {
                        showResponseMessage("AE DELETE", response);
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        Log.e(TAG, errorCode + " : " + message);
                        showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (areaNwkInfoRes != null) {
            try {
                areaNwkInfo areaNwkInfoDelete = new areaNwkInfo.Builder(Operation.Delete).
                        nm(areaNwkInfoRes.getRn()).
                        dKey(device.dKey).build();

                mqttService.publish(areaNwkInfoDelete, new MQTTCallback<areaNwkInfoResponse>() {
                    @Override
                    public void onResponse(areaNwkInfoResponse response) {
                        showResponseMessage("areaNwkInfo DELETE", response);
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        Log.e(TAG, errorCode + " : " + message);
                        showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (locationPolicyRes != null) {
            try {
                locationPolicy locationPolicyDelete = new locationPolicy.Builder(Operation.Delete).
                        nm(locationPolicyRes.getRn()).
                        dKey(device.dKey).build();

                mqttService.publish(locationPolicyDelete, new MQTTCallback<locationPolicyResponse>() {
                    @Override
                    public void onResponse(locationPolicyResponse response) {
                        showResponseMessage("locationPolicy DELETE", response);
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        Log.e(TAG, errorCode + " : " + message);
                        showToast("fail - " + errorCode + ":" + message, Toast.LENGTH_LONG);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        unregisterDevice();

    }

    private void getLatestInstance() {
        if (mqttService == null) return;
        try {
            final List<DeviceInfo> deviceList = new DeviceListTask().execute().get();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayDeviceList(deviceList);
                    for (final DeviceInfo info : deviceList) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        oneM2MAPI.getInstance().tpLatest(mqttService, info.deviceId, Configuration.CONTAINER_NAME_LONGITUDE
                                , UKEY, new MQTTCallback<latestResponse>() {
                                    @Override
                                    public void onResponse(latestResponse response) {
                                        Log.i("get latest longitude", response.toString());
                                        setNode(response.ri, response);
                                    }

                                    @Override
                                    public void onFailure(int errorCode, String message) {
                                        Log.e("get latest long err", errorCode + " : " + message);

                                    }
                                });
                        oneM2MAPI.getInstance().tpLatest(mqttService, info.deviceId, Configuration.CONTAINER_NAME_LATITUDE
                                , UKEY, new MQTTCallback<latestResponse>() {
                                    @Override
                                    public void onResponse(latestResponse response) {
                                        Log.i("get latest latitude", response.toString());
                                        setNode(response.ri, response);
                                    }

                                    @Override
                                    public void onFailure(int errorCode, String message) {
                                        Log.e("get latest latitude err", errorCode + " : " + message);

                                    }
                                });
                    }
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    void setNode(String ri, latestResponse response) {
        Application app = getApplication();
        final MyApp myApp = (MyApp) app;
        latestData = oneM2MAPI.getInstance().latestData;
        ArrayList list = latestData.get(ri);
        if (response.getCon() == null) return;
        list.add(response.getCon());
        list.add(response.getLt());
        latestData.put(ri, list);
        NodeData nodeData;
        String deviceId = (String) list.get(0);
        if (nodeMap.get(deviceId) == null) {
            nodeData = new NodeData();
        } else {
            nodeData = nodeMap.get(deviceId);
        }
        switch ((String) list.get(1)) {
            case "Geolocation_latitude":
                nodeData.setLatitude((String) list.get(2));
                break;
            case "Geolocation_longitude":
                nodeData.setLongitude((String) list.get(2));
                break;
            case "Smoke":
                nodeData.setSmoke((String) list.get(2));
                break;
        }
        nodeData.setTime((String) list.get(3));
        nodeMap.put(deviceId, nodeData);
        Log.i("set Node list", list.toString());
        Log.i("set Node nodeMap", nodeMap.toString());
        myApp.setNodeMap(nodeMap);
    }
}