package tp.skt.onem2m.api;

import android.util.Log;
import android.util.Pair;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tp.skt.onem2m.binder.mqtt_v1_1.Definitions;
import tp.skt.onem2m.binder.mqtt_v1_1.request.container;
import tp.skt.onem2m.binder.mqtt_v1_1.request.contentInstance;
import tp.skt.onem2m.binder.mqtt_v1_1.request.execInstance;
import tp.skt.onem2m.binder.mqtt_v1_1.request.latest;
import tp.skt.onem2m.binder.mqtt_v1_1.request.mgmtCmd;
import tp.skt.onem2m.binder.mqtt_v1_1.request.node;
import tp.skt.onem2m.binder.mqtt_v1_1.request.remoteCSE;
import tp.skt.onem2m.binder.mqtt_v1_1.request.subscription;
import tp.skt.onem2m.binder.mqtt_v1_1.response.containerResponse;
import tp.skt.onem2m.binder.mqtt_v1_1.response.contentInstanceResponse;
import tp.skt.onem2m.binder.mqtt_v1_1.response.execInstanceResponse;
import tp.skt.onem2m.binder.mqtt_v1_1.response.latestResponse;
import tp.skt.onem2m.binder.mqtt_v1_1.response.mgmtCmdResponse;
import tp.skt.onem2m.binder.mqtt_v1_1.response.nodeResponse;
import tp.skt.onem2m.binder.mqtt_v1_1.response.remoteCSEResponse;
import tp.skt.onem2m.binder.mqtt_v1_1.response.subscriptionResponse;
import tp.skt.onem2m.common.MQTTConst;
import tp.skt.onem2m.net.mqtt.MQTTCallback;
import tp.skt.onem2m.net.mqtt.MQTTUtils;

/**
 * ThingPlug oneM2M MQTT API
 * <p>
 * Copyright (C) 2017. SK Telecom, All Rights Reserved.
 * Written 2017, by SK Telecom
 */
public class oneM2MAPI {
    /**
     * oneM2MAPI 객체
     **/
    private static oneM2MAPI oneM2MApi;
    /**
     * Formatted data
     **/
    private static String formattedData = "";

    /**
     * oneM2MAPI Constructor
     */
    public oneM2MAPI() {
    }

    /**
     * get oneM2MAPI
     *
     * @return generated oneM2MAPI object
     */
    public static oneM2MAPI getInstance() {
        if (oneM2MApi == null) {
            oneM2MApi = new oneM2MAPI();
        }
        return oneM2MApi;
    }

    /**
     * 장치를 등록한다.
     * (node 와 remoteCSE 를 등록한다.)
     *
     * @param mqttService        MQTT service
     * @param clientId           client ID
     * @param passcode           passcode
     * @param cseType            cseType
     * @param requestRechability requestRechability
     * @param callback           response callback
     */
    public void tpRegisterDevice(final IMQTT mqttService, final String clientId, final String passcode,
                                 final String cseType, final String requestRechability, final MQTTCallback callback) {
        try {
            MQTTUtils.checkNull(mqttService, "IMQTT is null!");
            MQTTUtils.checkNull(callback, "MQTTCallback is null!");

            // node CREATE
            node nodeCreate = new node.Builder(Definitions.Operation.Create).
                    mga("MQTT|{" + MQTTConst.CLIENT_ID + "}").fr(clientId).
                    ni(clientId).nm(clientId).build();

            mqttService.publish(nodeCreate, new MQTTCallback<nodeResponse>() {
                @Override
                public void onResponse(nodeResponse response) {
                    int responseCode = Definitions.ResponseStatusCode.BAD_REQUEST;

                    if (response.rsc != null) {
                        responseCode = Integer.valueOf(response.rsc);
                    }

                    if (responseCode == Definitions.ResponseStatusCode.CREATED ||
                            responseCode == Definitions.ResponseStatusCode.CONFLICT) {
                        try {
                            // remoteCSE CREATE
                            remoteCSE remoteCSECreate = new remoteCSE.Builder(Definitions.Operation.Create).
                                    passCode(passcode).
                                    cst(cseType).fr(clientId).nm(clientId).csi(clientId).
//                                    poa("MQTT|{" + MQTTConst.RESOURCE_ID + "}").
        rr(requestRechability).
                                            nl(response.getRi()).build();

                            mqttService.publish(remoteCSECreate, new MQTTCallback<remoteCSEResponse>() {
                                @Override
                                public void onResponse(remoteCSEResponse response) {
                                    int responseCode = Definitions.ResponseStatusCode.BAD_REQUEST;

                                    if (response.rsc != null) {
                                        responseCode = Integer.valueOf(response.rsc);
                                    }
                                    if (responseCode == Definitions.ResponseStatusCode.CREATED ||
                                            responseCode == Definitions.ResponseStatusCode.CONFLICT) {
                                        callback.onResponse(response);
                                    } else {
                                        this.onFailure(responseCode, response.RSM);
                                    }
                                }

                                @Override
                                public void onFailure(int errorCode, String message) {
                                    callback.onFailure(errorCode, message);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            callback.onFailure(Definitions.ResponseStatusCode.INTERNAL_SDK_ERROR, e.getMessage());
                        }
                    } else {
                        this.onFailure(responseCode, response.RSM);
                    }
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    callback.onFailure(errorCode, message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure(Definitions.ResponseStatusCode.INTERNAL_SDK_ERROR, e.getMessage());
        }
    }

    /**
     * 센서를 등록한다.
     * (container 를 등록한다.)
     *
     * @param mqttService   MQTT service
     * @param containerName containerName
     * @param deviceKey     deviceKey
     * @param callback      response callback
     */
    public void tpRegisterContainer(final IMQTT mqttService, final String containerName, final String deviceKey,
                                    final MQTTCallback callback) {
        try {
            MQTTUtils.checkNull(mqttService, "IMQTT is null!");
            MQTTUtils.checkNull(callback, "MQTTCallback is null!");

            container containerCreate = new container.Builder(Definitions.Operation.Create).
                    nm(containerName).
                    dKey(deviceKey).build();

            mqttService.publish(containerCreate, new MQTTCallback<containerResponse>() {
                @Override
                public void onResponse(containerResponse response) {
                    callback.onResponse(response);
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    callback.onFailure(errorCode, message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure(Definitions.ResponseStatusCode.INTERNAL_SDK_ERROR, e.getMessage());
        }
    }

    /**
     * 구독신청을 한다.
     *
     * @param mqttService    MQTT service
     * @param deviceID       deviceID
     * @param targetDeviceID targetDeviceID
     * @param containerName  containerName
     * @param userKey        userKey
     * @param callback       response callback
     */
    public void tpSubscription(final IMQTT mqttService, final String deviceID, final String targetDeviceID,
                               final String containerName, final String userKey, final MQTTCallback callback) {
        try {
            MQTTUtils.checkNull(mqttService, "IMQTT is null!");
            MQTTUtils.checkNull(callback, "MQTTCallback is null!");

            subscription subscription = new subscription.Builder(Definitions.Operation.Create).targetID(targetDeviceID).
                    containerName(containerName).nm(deviceID + "_" + containerName).uKey(userKey).rss("1").
                    nu("MQTT|" + deviceID).nct("2").fr(deviceID).build();

            mqttService.publish(subscription, new MQTTCallback<subscriptionResponse>() {
                @Override
                public void onResponse(subscriptionResponse response) {
                    callback.onResponse(response);
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    callback.onFailure(errorCode, message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure(Definitions.ResponseStatusCode.INTERNAL_SDK_ERROR, e.getMessage());
        }
    }


    /**
     * 제어를 등록한다.
     * (mgmtCmd 를 등록한다.)
     *
     * @param mqttService MQTT service
     * @param mgmtCmdName mgmtCmdName
     * @param deviceKey   deviceKey
     * @param cmdType     cmdType
     * @param execEnable  execEnable
     * @param execTarget  execTarget
     * @param callback    response callback
     */
    public void tpRegisterMgmtCmd(final IMQTT mqttService, final String mgmtCmdName, final String deviceKey,
                                  final String cmdType, final String execEnable, final String execTarget,
                                  final MQTTCallback callback) {
        try {
            MQTTUtils.checkNull(mqttService, "IMQTT is null!");
            MQTTUtils.checkNull(callback, "MQTTCallback is null!");

            mgmtCmd mgmtCmdCreate = new mgmtCmd.Builder(Definitions.Operation.Create).
                    nm(mgmtCmdName).
                    dKey(deviceKey).
                    cmt(cmdType).
                    exe(execEnable).
                    ext(execTarget).build();

            mqttService.publish(mgmtCmdCreate, new MQTTCallback<mgmtCmdResponse>() {
                @Override
                public void onResponse(mgmtCmdResponse response) {
                    callback.onResponse(response);
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    callback.onFailure(errorCode, message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure(Definitions.ResponseStatusCode.INTERNAL_SDK_ERROR, e.getMessage());
        }
    }

    /**
     * 센서정보를 추가한다.
     * (contentInstance 의 content(con) 에 담을 정보를 추가한다.)
     *
     * @param data ttv data
     */
    public void tpAddData(String data) {
        this.formattedData += data;
    }

    /**
     * 센서정보를 등록한다.
     * (contentInstance 를 등록한다.)
     *
     * @param mqttService MQTT service
     * @param deviceKey   deviceKey
     * @param contentInfo contentInfo
     * @param content     content
     * @param callback    response callback
     */
    public void tpReport(final IMQTT mqttService, final String containerName, final String deviceKey, final String contentInfo,
                         final String content, final boolean useAddedData, final MQTTCallback callback) {
        try {
            MQTTUtils.checkNull(mqttService, "IMQTT is null!");
            MQTTUtils.checkNull(callback, "MQTTCallback is null!");

            String con = null;
            if (useAddedData == true) {
                con = formattedData;
                formattedData = "";
            } else {
                con = content;
            }

            contentInstance contentInstanceCreate = new contentInstance.Builder(Definitions.Operation.Create).
                    containerName(containerName).
                    dKey(deviceKey).
                    cnf(contentInfo).
                    con(con).build();

            mqttService.publish(contentInstanceCreate, new MQTTCallback<contentInstanceResponse>() {
                @Override
                public void onResponse(contentInstanceResponse response) {
                    callback.onResponse(response);
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    callback.onFailure(errorCode, message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure(Definitions.ResponseStatusCode.INTERNAL_SDK_ERROR, e.getMessage());
        }
    }

    /**
     * 제어결과를 업데이트한다.
     * (execInstance 를 업데이트한다.)
     *
     * @param mqttService MQTT service
     * @param mgmtCmdName mgmtCmdName
     * @param deviceKey   deviceKey
     * @param resourceId  resourceId
     * @param execResult  execResult
     * @param execStatus  execStatus
     * @param callback    response callback
     */
    public void tpResult(final IMQTT mqttService, final String mgmtCmdName, final String deviceKey,
                         final String resourceId, final String execResult, final String execStatus,
                         final MQTTCallback callback) {
        try {
            MQTTUtils.checkNull(mqttService, "IMQTT is null!");
            MQTTUtils.checkNull(callback, "MQTTCallback is null!");

            execInstance execInstanceUpdate = new execInstance.Builder(Definitions.Operation.Update).
                    nm(mgmtCmdName).
                    dKey(deviceKey).
                    resourceId(resourceId).
                    exr(execResult).
                    exs(execStatus).build();

            mqttService.publish(execInstanceUpdate, new MQTTCallback<execInstanceResponse>() {
                @Override
                public void onResponse(execInstanceResponse response) {
                    callback.onResponse(response);
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    callback.onFailure(errorCode, message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure(Definitions.ResponseStatusCode.INTERNAL_SDK_ERROR, e.getMessage());
        }
    }

    /**
     * 가장 최근의 instance값을 호출한다.
     *
     * @param mqttService    MQTT service
     * @param targetDeviceID target Device ID
     * @param containerName  container Name
     * @param userKey        user Key
     * @param callback       response callback
     */
    private Pair<String, ArrayList> latestData;
    public ArrayList<Pair> latestDataList = new ArrayList<>(1000);

    public void tpLatest(IMQTT mqttService, String targetDeviceID,
                         String containerName, String userKey, final MQTTCallback callback) {

        try {
            MQTTUtils.checkNull(mqttService, "IMQTT is null!");
            MQTTUtils.checkNull(callback, "MQTTCallback is null!");

            final latest latestRetrieve = new latest.Builder(Definitions.Operation.Retrieve).targetID(targetDeviceID).
                    containerName(containerName).uKey(userKey).build();
            ArrayList list = new ArrayList();
            list.add(targetDeviceID);
            list.add(containerName);
            //Log.i("request list", list.toString());

            /**
             * latestData가 1천개가 축적되면 5백개를 지운다.
             * if(size(latestData) >= 1000) {
             * latestData.remove(가장 먼저 저장된 값 5백개);
             * }
             */
            latestData = new Pair<>(latestRetrieve.getRequestIdentifier(), list);
            if (latestDataList.size() >= 1000) {
                for (int i = 0; i < 500; i++)
                    latestDataList.remove(i);
            }
            latestDataList.add(latestData);

            MQTTCallback callback1 = new MQTTCallback<latestResponse>() {
                @Override
                public void onResponse(latestResponse response) {
                    callback.onResponse(response);
                }

                @Override
                public void onFailure(int errorCode, String message) {
                    callback.onFailure(errorCode, message);
                    Log.e("latest publish", "ri : " + latestRetrieve.getRequestIdentifier() + " id : " + latestRetrieve.hashCode());
                }
            };

            mqttService.publish(latestRetrieve, callback1);

        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure(Definitions.ResponseStatusCode.INTERNAL_SDK_ERROR, e.getMessage());
        }
    }
}
