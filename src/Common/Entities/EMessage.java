package Common.Entities;

import java.io.Serializable;

import Common.Enums.EMessageType;

public class EMessage implements Serializable {

    private final EMessageType messageType;
    private final Object message;

    public EMessage(EMessageType messageType, Object message) {
        this.messageType = messageType;
        this.message = message;
    }

    public EMessageType getMessageType() {
        return this.messageType;
    }
    
    public Object getMessage() {
        return this.message;
    }
    
}
