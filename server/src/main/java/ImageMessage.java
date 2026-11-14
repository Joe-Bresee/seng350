import java.time.LocalDateTime;

public class ImageMessage {
    private final String busID;
    private final String tag;
    private final LocalDateTime timestamp;
    private final byte[] image;

    public ImageMessage(String busID, String tag, LocalDateTime timestamp, byte[] image) {
        this.busID = busID;
        this.tag = tag;
        this.timestamp = timestamp;
        this.image = image;
    }

    public String getBusID() {
        return busID;
    }

    public String getTag() {
        return tag;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public byte[] getImage() {
        return image;
    }
}