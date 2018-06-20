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
public class latest extends RequestBase {
    /**
     * @param builder
     */
    public latest(Builder builder) {
        super(builder);
    }

    public static class Builder extends RequestBase.Builder {

        private String targetID;
        private String containerName;

        public Builder(@Definitions.Operation int op) {
            super(op, Definitions.ResourceType.request);
        }

        public Builder containerName(String containerName) {
            this.containerName = containerName;
            return this;
        }

        public Builder targetID(String targetID) {
            this.targetID = targetID;
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

        public Builder fr(String fr) {
            this.fr = fr;
            return this;
        }

        @Override
        Content getContent() {
            Content content = new Content();
            return content;
        }

        @Override
        String getTo() {
            if (op == Definitions.Operation.Retrieve) {
                return "{" + MQTTConst.CSEBASE_ID + "}/remoteCSE-{" + MQTTConst.RESOURCE_ID + "}/container-{" + MQTTConst.CONTAINER_NAME + "}/latest";
            } else
                return null;
        }

        @Override
        String getDefaultResourceName() {
            return null;
        }

        public latest build() {
            if (targetID != null) {
                this.to = to.replace("{" + MQTTConst.RESOURCE_ID + "}", this.targetID);
                this.targetID = null;
            }
            if (containerName != null) {
                this.to = to.replace("{" + MQTTConst.CONTAINER_NAME + "}", this.containerName);
                this.containerName = null;
            }
            latest build = new latest(this);
            return build;
        }
    }
}
