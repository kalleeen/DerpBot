
package fi.derpnet.derpbot.bean;

public class MatrixMessage implements Message {
    private final String message;
    private String roomId = null;
    
    public MatrixMessage(String message){
        this.message = message;
    }
    
    public MatrixMessage(String message, String roomId){
        this.message = message;
        this.roomId = roomId;
    }
    
    public String getRoomId(){
        return this.roomId;
    }
    
    public String toString(){
        return message;
    }
}
