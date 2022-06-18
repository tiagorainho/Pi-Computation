package Common.Entities;

import java.util.List;

import Common.Enums.EMessageType;

public class EMessageRegistry extends EMessage {

    public EMessageRegistry(String serviceName, Integer port, List<String> dependencies) {
        super(
            EMessageType.RegisterServiceRegistry,
            new Object[] {serviceName, port, dependencies}
        );
    }

    public String getServiceName() {
        return (String) ((Object[])super.getMessage())[0];
    }

    public Integer getPort() {
        return (Integer) ((Object[])super.getMessage())[1];
    }

    public List<String> getDependencies() {
        return (List<String>) ((Object[])super.getMessage())[2];
    }

    

}
