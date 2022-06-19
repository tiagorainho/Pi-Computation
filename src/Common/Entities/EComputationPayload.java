package Common.Entities;

import java.io.Serializable;

import Common.Interfaces.IDeepCopyable;

public class EComputationPayload implements IDeepCopyable, Serializable {

    private final Integer clientID;
    private Integer requestID;
    private final Integer iteractions;
    private final Integer deadline;
    private Integer code;

    // bellow variables will be zero on the request
    private Integer serverID;
    private Double pi;
   
    public EComputationPayload(Integer code, Integer clientID, Integer requestID, Integer iteractions, Integer deadline) {
        this.code = code;
        this.clientID = clientID;
        this.iteractions = iteractions;
        this.deadline = deadline;
        this.requestID = requestID;
    }

    public void setPI(Double pi) { this.pi = pi; }
    public void setServerID(Integer id) { this.serverID = id; }
    public void setCode(Integer code) { this.code = code; }
    
    public Integer getClientID() { return this.clientID; }
    public Integer getRequestID() { return this.requestID; }
    public Integer getIteractions() { return this.iteractions; }
    public Integer getDeadline() { return this.deadline; }
    public Integer getCode() { return this.code; }
    public Integer getServerID() { return this.serverID; }
    public Double getPI() { return this.pi; }

}
