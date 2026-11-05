import java.time.LocalDateTime;




public class ImageMessage {
    private String busID;
    private String tag;
    private LocalDateTime timestamp;
    private byte[] image;

    public ImageMessage(String busID, String tag, LocalDateTime timestamp, byte[] image){
        this.busID = busID;
        this.tag = tag;
        this.timestamp = timestamp;
        this.image = image;
    }

    public void printInfo(){
        System.out.println("BusID " + this.busID + " Tag: " + this.tag + " timestamp: " + this.timestamp);
    }

    // public void makeString() {
    //     str_busID = String.parseString(this.busID);
    //     str_tag = String.parseString(this.tag);
    //     str_timestamp = String.parseString(this.timestamp);
    //     str_image = String.parseString(this.image);
    // }

    public String getBusID(){
        return busID;
    }

    public String getTag(){
        return tag;
    }

    public LocalDateTime getTimestamp(){
        return timestamp;
    }

    public byte[] getImage(){
        return image;
    }

}