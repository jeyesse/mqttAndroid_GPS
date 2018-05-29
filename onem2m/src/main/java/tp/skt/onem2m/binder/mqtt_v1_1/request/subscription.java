package tp.skt.onem2m.binder.mqtt_v1_1.request;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

import tp.skt.onem2m.binder.mqtt_v1_1.Definitions;
import tp.skt.onem2m.common.MQTTConst;

@Root(name = "req")
@Namespace(prefix = "m2m", reference = "http://www.onem2m.org/xml/protocols")
@Default(DefaultType.FIELD)
public class subscription extends RequestBase {

    /**
     * @param builder
     */
    public subscription(Builder builder) {
        super(builder);
    }

    /**
     * remoteCSE Builder
     */
    public static class Builder extends RequestBase.Builder {

        private String targetID;

        private String containerName;
        /*
         * @param containerName
         */
        private String nu;
        private String enc;
        private String nct;

        public Builder(@Definitions.Operation int op) {
            super(op, Definitions.ResourceType.subscription);
        }

        public Builder enc(String enc) {
            this.enc = enc;
            return this;
        }

        public Builder nu(String nu) {
            this.nu = nu;
            return this;
        }

        public Builder nct(String nct) {
            this.nct = nct;
            return this;
        }

        public Builder containerName(String containerName) {
            this.containerName = containerName;
            return this;
        }
        public Builder targetID(String targetID) {
            this.targetID = targetID;
            return this;
        }
        public Builder nm(String nm) {
            this.nm = nm;
            return this;
        }

        public Builder dKey(String dKey) {
            this.dKey = dKey;
            return this;
        }

        public Builder uKey(String uKey) {
            this.uKey = uKey;
            return this;
        }

        @Override
        Content getContent() {
            Content content = new Content();
            content.sub = new Content.Sub(enc, nu, nct);
            return content;
        }

        @Override
        String getTo() {
            if (op == Definitions.Operation.Create) {
                return "{" + MQTTConst.CSEBASE_ID + "}/remoteCSE-{" + MQTTConst.RESOURCE_ID + "}/container-{" + MQTTConst.CONTAINER_NAME + "}";
            } else {
                return "{" + MQTTConst.CSEBASE_ID + "}/remoteCSE-{" + MQTTConst.RESOURCE_ID + "}/container-{" + MQTTConst.CONTAINER_NAME + "}/subscription-{" + nm + "}";
            }
        }

        @Override
        String getDefaultResourceName() {
            return null;
        }

        public subscription build() {
            if (op != Definitions.Operation.Create) {
                if (nm != null) {
                    this.to += nm;
                    this.nm = null;
                }
            }
            if (containerName != null) {
                this.to = to.replace("{" + MQTTConst.CONTAINER_NAME + "}", this.containerName);
                this.containerName = null;
            }
            if (targetID != null) {
                this.to = to.replace("{" + MQTTConst.RESOURCE_ID + "}", this.targetID);
            }

            subscription build = new subscription(this);
            return build;
        }
    }

}
