package tp.skt.onem2m.binder.mqtt_v1_1.response;

import com.google.gson.annotations.Expose;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import tp.skt.onem2m.binder.mqtt_v1_1.Definitions;
import tp.skt.onem2m.net.mqtt.MQTTUtils;

@Root(strict = false)
public class subscriptionResponse extends ResponseBase {

    @Element(required = false)
    private Pc pc;

    public String getTy() {
        return pc.sub.ty;
    }

    public String getRi() {
        return pc.sub.ri;
    }

    public String getRn() {
        return pc.sub.rn;
    }

    public String getPi() {
        return pc.sub.pi;
    }

    public String getCt() {
        return pc.sub.ct;
    }

    public String getLt() {
        return pc.sub.lt;
    }

    public String getLbl() {
        return pc.sub.lbl;
    }

    public String getEnc() {
        return pc.sub.enc;
    }

    public String getNu() {
        return pc.sub.nu;
    }

    public String getNct() {
        return pc.sub.nct;
    }

    public String getRss() {
        return pc.sub.rss;
    }


    @Override
    public String getRequestIdentifier() {
        return null;
    }

    private static class Pc {
        @Element(required = false)
        private Sub sub;

        @Root
        private static class Sub {
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
            private String lbl;

            @Expose
            @Element(required = false)
            private String enc;

            @Expose
            @Element(required = false)
            private String nu;

            @Expose
            @Element(required = false)
            private String nct;

            @Expose
            @Element(required = false)
            private String rss;

        }
    }

    public void print() {
        MQTTUtils.log("[" + Definitions.getResourceName(Definitions.ResourceType.subscription) + "]");
        super.print();
        if (pc.sub == null) return;
        MQTTUtils.log("ty : "+pc.sub.ty);
        MQTTUtils.log("ri : "+pc.sub.ri);
        MQTTUtils.log("rn : "+pc.sub.rn);
        MQTTUtils.log("ct : "+pc.sub.ct);
        MQTTUtils.log("lt : "+pc.sub.lt);
        MQTTUtils.log("lbl : "+pc.sub.lbl);
        MQTTUtils.log("enc : "+pc.sub.enc);
        MQTTUtils.log("nu : "+pc.sub.nu);
        MQTTUtils.log("nct : "+pc.sub.nct);
        MQTTUtils.log("rss : "+pc.sub.rss);
    }
}
