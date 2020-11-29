package ftn.diplomskiRad.SignalingServer.model;

public class SignalMessage {
    private String type;
    private String dest;
    private String source;
    private String roomID;
    private Object data;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "SignalMessage{" +
                "type='" + type + '\'' +
                ", dest='" + dest + '\'' +
                ", source='" + source + '\'' +
                ", roomID='" + roomID + '\'' +
                '}';
    }
}
