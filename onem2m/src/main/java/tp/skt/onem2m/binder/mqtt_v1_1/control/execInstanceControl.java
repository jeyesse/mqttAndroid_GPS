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
        return pc.cin.ty;
    }

    /**
     * get resource id
     *
     * @return ri
     */
    public String getRi() {
        return pc.cin.ri;
    }

    /**
     * get resource name
     *
     * @return rn
     */
    public String getRn() {
        return pc.cin.rn;
    }

    /**
     * get parent id
     *
     * @return pi
     */
    public String getPi() {
        return pc.cin.pi;
    }

    /**
     * get creation time
     *
     * @return ct
     */
    public String getCt() {
        return pc.cin.ct;
    }

    /**
     * get last modified time
     *
     * @return lt
     */
    public String getLt() {
        return pc.cin.lt;
    }

    /**
     * get expirationTime
     *
     * @return et
     */
    public String getEt() {
        return pc.cin.et;
    }

    public String getSt() {
        return pc.cin.st;
    }

    public String getCr() {
        return pc.cin.cr;
    }

    public String getCnf() {
        return pc.cin.cnf;
    }

    public String getCs() {
        return pc.cin.cs;
    }

    public String getSr() {
        return pc.cin.sr;
    }

    public String getCon() {
        return pc.cin.con;
    }

    @Root
    private static class Pc {
        /*
        @Expose
        @Element(required = false)
        private Exin exin;
        */
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
        /*
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
        */
    }

    @Override
    public String getRequestIdentifier() {
        return rqi;
    }

    public void print() {
        MQTTUtils.log("[" + Definitions.getResourceName(Definitions.ResourceType.execInstance) + "(control)]");
        if (pc.cin == null) return;
        MQTTUtils.log("ty : " + pc.cin.ty);
        MQTTUtils.log("ri : " + pc.cin.ri);
        MQTTUtils.log("rn : " + pc.cin.rn);
        MQTTUtils.log("pi : " + pc.cin.pi);
        MQTTUtils.log("ct : " + pc.cin.ct);
        MQTTUtils.log("lt : " + pc.cin.lt);
        MQTTUtils.log("sr : " + pc.cin.sr);
        MQTTUtils.log("et : " + pc.cin.et);
        MQTTUtils.log("st : " + pc.cin.st);
        MQTTUtils.log("cr : " + pc.cin.cr);
        MQTTUtils.log("cnf : " + pc.cin.cnf);
        MQTTUtils.log("cs : " + pc.cin.cs);
        MQTTUtils.log("con : " + pc.cin.con);
    }
}
