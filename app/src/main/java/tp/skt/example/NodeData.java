package tp.skt.example;

public class NodeData {
    String id = "";
    String smoke = "";
    String fire = "";
    String address = "";
    String latitude = "";
    String longitude = "";
    String time = "";
    String battery = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSmoke() {
        return smoke;
    }

    public void setSmoke(String smoke) {
        this.smoke = smoke;
    }

    public String getFire() {
        return fire;
    }

    public void setFire(String fire) {
        this.fire = fire;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID : ").append(id);
        sb.append("\nsmoke : ").append(smoke);
        sb.append(" fire : ").append(fire);
        sb.append("\naddress : ").append(address);
        sb.append("\nlatitude : ").append(latitude);
        sb.append(" longitude : ").append(longitude);
        sb.append("\ntime : ").append(time);
        sb.append("\nbattery : ").append(battery);
        return String.valueOf(sb);
    }
}
