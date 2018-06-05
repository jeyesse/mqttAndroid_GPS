package tp.skt.onem2m.binder.mqtt_v1_1.control;


import com.google.gson.annotations.Expose;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.NamespaceList;
import org.simpleframework.xml.Root;

import tp.skt.onem2m.api.oneM2MResource;
import tp.skt.onem2m.binder.mqtt_v1_1.Definitions;
import tp.skt.onem2m.net.mqtt.MQTTUtils;

/**
 * execInstance control
 * <p>
 * Copyright (C) 2017. SK Telecom, All Rights Reserved.
 * Written 2017, by SK Telecom
 */
@NamespaceList({
        @Namespace(prefix = "xsi", reference = "http://www.w3.org/2001/XMLSchema-instance")
})
@Root(strict = false)
public class execInstanceControl extends oneM2MResource {

    @Attribute(name = "schemaLocation")
    @Namespace(prefix = "xsi")
    protected String schemaLocation;

    @Element
    private Pc pc;

    @Element
    private String rqi;

    /**
     * get resource type
     *
     * @return ty
     */
    public String getTy() {
        return pc.exin.ty;
    }

    /**
     * get resource id
     *
     * @return ri
     */
    public String getRi() {
        return pc.exin.ri;
    }

    /**
     * get resource name
     *
     * @return rn
     */
    public String getRn() {
        return pc.exin.rn;
    }

    /**
     * get parent id
     *
     * @return pi
     */
    public String getPi() {
        return pc.exin.pi;
    }

    /**
     * get creation time
     *
     * @return ct
     */
    public String getCt() {
        return pc.exin.ct;
    }

    /**
     * get last modified time
     *
     * @return lt
     */
    public String getLt() {
        return pc.exin.lt;
    }

    /**
     * get expirationTime
     *
     * @return et
     */
    public String getEt() {
        return pc.exin.et;
    }

    /**
     * get labels
     *
     * @return lbl
     */
    public String getLbl() {
        return pc.exin.lbl;
    }

    /**
     * get execStatus
     *
     * @return exs
     */
    public String getExs() {
        return pc.exin.exs;
    }

    /**
     * get cmdType
     *
     * @return cmt
     */
    public String getCmt() {
        return pc.exin.cmt;
    }

    /**
     * get execTarget
     *
     * @return ext
     */
    public String getExt() {
        return pc.exin.ext;
    }

    /**
     * get execRegArgs
     *
     * @return
     */
    public String getExra() {
        return pc.exin.exra;
    }

    /**
     * get mgmtCmd name
     *
     * @return nm
     */
    public String getNm() {
        return pc.exin.nm;
    }

    public String getSr() {
        return pc.cin.sr;
    }

    public String getCon() {
        return pc.cin.con;
    }

    @Root
    private static class Pc {
        @Expose
        @Element(required = false)
        private Exin exin;
        @Expose
        @Element(required = false)
        private Cin cin;

        @Root(strict = false)
        private static class Cin {

            @Expose
            @Element(required = false)
            private String ty;

            @Expose
            @Element(required = false)
            private String ri;

            @Expose
            @Element(required = false)
            private String rn;

            @Expose
            @Element(required = false)
            private String pi;

            @Expose
            @Element(required = false)
            private String ct;

            @Expose
            @Element(required = false)
            private String lt;

            @Expose
            @Element(required = false)
            private String sr;

            @Expose
            @Element(required = false)
            private String et;

            @Expose
            @Element(required = false)
            private String st;

            @Expose
            @Element(required = false)
            private String cr;

            @Expose
            @Element(required = false)
            private String cnf;

            @Expose
            @Element(required = false)
            private String cs;

            @Expose
            @Element(required = false)
            private String con;

        }

        @Root(strict = false)
        private static class Exin {

            @Expose
            @Element(required = false)
            private String nm;

            @Expose
            @Element(required = false)
            private String ty;

            @Expose
            @Element(required = false)
            private String ri;

            @Expose
            @Element(required = false)
            private String rn;

            @Expose
            @Element(required = false)
            private String pi;

            @Expose
            @Element(required = false)
            private String ct;

            @Expose
            @Element(required = false)
            private String lt;

            @Expose
            @Element(required = false)
            private String et;

            @Expose
            @Element(required = false)
            private String lbl;

            @Expose
            @Element(required = false)
            private String exs;

            @Expose
            @Element(required = false)
            private String cmt;

            @Expose
            @Element(required = false)
            private String ext;

            @Expose
            @Element(required = false)
            private String exra;
        }
    }

    @Override
    public String getRequestIdentifier() {
        return rqi;
    }

    public void print() {
        MQTTUtils.log("[" + Definitions.getResourceName(Definitions.ResourceType.execInstance) + "(control)]");
        if (pc.exin == null & pc.cin == null) return;
//        MQTTUtils.log("ty : " + pc.exin.ty);
//        MQTTUtils.log("ri : " + pc.exin.ri);
//        MQTTUtils.log("rn : " + pc.exin.rn);
//        MQTTUtils.log("ct : " + pc.exin.ct);
//        MQTTUtils.log("lt : " + pc.exin.lt);
//        MQTTUtils.log("et : " + pc.exin.et);
//        MQTTUtils.log("lbl : " + pc.exin.lbl);
//        MQTTUtils.log("exs : " + pc.exin.exs);
//        MQTTUtils.log("exr : " + pc.exin.cmt);
//        MQTTUtils.log("ext : " + pc.exin.ext);
//        MQTTUtils.log("exra : " + pc.exin.exra);
//        MQTTUtils.log("nm : " + pc.exin.nm);
        MQTTUtils.log("con : " + pc.cin.con);
        MQTTUtils.log("sr : " + pc.cin.sr);

    }
}
