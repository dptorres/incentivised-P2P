import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

/**
 * Created by Danah Veronica Torres on 11/30/15.
 */
public class ResetQuota implements Control {

    private static final String PAR_PROT = "protocol";
    private final int protocolID;

    public ResetQuota(String prefix) {
        protocolID = Configuration.getPid(prefix + "." + PAR_PROT);
    }

    @Override
    public boolean execute() {
        for (int i = 0; i < Network.size(); ++i) {
            ((MediaStreamingProtocol) Network.get(i).getProtocol(protocolID))
                    .resetQuota();
        }
        return false;
    }
}
