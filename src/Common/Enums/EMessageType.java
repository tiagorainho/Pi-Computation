package Common.Enums;

public enum EMessageType {

    // Load Balancer
    SyncronizePendingRequests,

    // Computation Server
    ComputationRequest,
    ComputationResult,
    ComputationRejection,

    // Service Registry
    RegisterServiceRegistry,
    ResponseServiceRegistry,
    RequestUpdateServiceRegistry,
    ResponseUpdateServiceRegistry,
    TopologyChange,
    Heartbeat

}
