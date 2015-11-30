import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.vector.SingleValueHolder;

/**
 * Created by Danah Veronica Torres on 11/29/15.
 */
public class MediaStreamingProtocol extends SingleValueHolder implements CDProtocol {

    protected static final String TOTAL_CHUNK_STRING = "total_chunk";
    private final double total_chunk;

    public MediaStreamingProtocol(String prefix) {
        super(prefix);
        total_chunk = Configuration.getDouble(prefix + "." + TOTAL_CHUNK_STRING, 100.00);
    }

    @Override
    public void nextCycle(Node node, int protocolID) {
        // Get ID of the protocol to be able to access the neighbors of the node
        int linkableID = FastConfig.getLinkable(protocolID);
        Linkable linkable = (Linkable) node.getProtocol(linkableID);

        // Access all neighbors to detect the most distant
        for (int i = 0; i < linkable.degree(); i++) {
            Node peer = linkable.getNeighbor(i);

            // Selected peer should be active
//            if (!peer.isUp())

            // The node already got needed chunks
            if (total_chunk == this.value)
                return;

            MediaStreamingProtocol p = (MediaStreamingProtocol) peer.getProtocol(protocolID);
            if (p.value > this.value) {
                doTransfer(p);
            }
        }

    }

    private void doTransfer(MediaStreamingProtocol neighbor) {
        double a1 = this.value;
        double a2 = neighbor.value;
        if ((a2 - a1) > 20) {
            // Transfer the greatest amount of chunks that can be transfered (20)
            a1 += 20;
        } else {
            // Transfer only what can be transfered
            a1 += (a2 - a1);
        }

        this.value = a1;
    }

}
