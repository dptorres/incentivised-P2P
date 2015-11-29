import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.vector.SingleValueHolder;

/**
 * Created by Danah Veronica Torres on 11/29/15.
 */
public class IncentiveProtocol extends SingleValueHolder implements CDProtocol {

    protected static final String PAR_QUOTA = "quota";
    private final double quota_value;
    protected double quota;

    public IncentiveProtocol(String prefix) {
        super(prefix);
        // get quota value from the config file. Default 1.
        quota_value = (Configuration.getInt(prefix + "." + PAR_QUOTA, 1));
        quota = quota_value;
    }

    // Resets quota
    protected void resetQuota() {
        this.quota = quota_value;
    }

    @Override
    public void nextCycle(Node node, int protocolID) {
        // Get ID of the protocol to be able to access the neighbors of the node
        int linkableID = FastConfig.getLinkable(protocolID);
        Linkable linkable = (Linkable) node.getProtocol(linkableID);

        if (this.quota == 0) {
            return; //Return cause limit has been exceeded.
        }

        IncentiveProtocol neighbor = null;
        double maxDiff = 0;

        // Access all neighbors to detect the most distant
        for (int i = 0; i < linkable.degree(); i++) {
            Node peer = linkable.getNeighbor(i);

            // Selected peer should be active
            if (peer.isUp())
                continue;

            IncentiveProtocol p = (IncentiveProtocol) peer.getProtocol(protocolID);
            if (p.quota == 0.0)
                continue;

            double d = Math.abs(value - p.value);
            if (d > maxDiff) {
                neighbor = p;
                maxDiff = d;
            }
        }
        if (neighbor == null) {
            return;
        }
        doTransfer(neighbor);

    }

    private void doTransfer(IncentiveProtocol neighbor) {
        double a1 = this.value;
        double a2 = neighbor.value;
        double maxTrans = Math.abs((a1 - a2) / 2);
        double trans = Math.min(maxTrans, quota);
        trans = Math.min(trans, neighbor.quota);
        if (a1 <= a2) {
            a1 += trans;
            a2 -= trans;
        } else {
            a1 -= trans;
            a2 += trans;
        }

        this.value = a1;
        this.quota -= trans;
        neighbor.value = a2;
        neighbor.quota -= trans;

    }

    public Object clone() {
        final IncentiveProtocol clone = (IncentiveProtocol) super.clone();
        clone.quota = this.quota;
        return clone;
    }
}
