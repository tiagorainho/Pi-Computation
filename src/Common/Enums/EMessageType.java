package Common.Enums;

public enum EMessageType {

    // server
    ComputationRequest,
    ComputationResult,
    ComputationRejection,

    // service registry
    RegisterServiceRegistry,
    ResponseServiceRegistry,
    RequestUpdateServiceRegistry,
    ResponseUpdateServiceRegistry,
    TopologyChange,
    Heartbeat

}
