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

    protected static final String PAR_CHUNKS = "chunks";
    private final double totalChunks;

    private static final double DOWNLOAD_QUOTA = 20.0;
    private static final double UPLOAD_QUOTA = 5.0;

    private double downloadQuota;
    private double uploadQuota;

    public MediaStreamingProtocol(String prefix) {
        super(prefix);
        totalChunks = Configuration.getDouble(prefix + "." + PAR_CHUNKS, 100.0);
        // TODO: add this to configuration
        downloadQuota = DOWNLOAD_QUOTA;
        uploadQuota = UPLOAD_QUOTA;
    }

    @Override
    public void nextCycle(Node node, int protocolID) {

        // The node already got needed chunks
        if (isFinished()) {
            return;
        }

        // Get ID of the protocol to be able to access the neighbors of the node
        final int linkableID = FastConfig.getLinkable(protocolID);
        final Linkable linkable = (Linkable) node.getProtocol(linkableID);

        // Access all neighbors to detect the most distant
        for (int i = 0; i < linkable.degree(); i++) {

            final Node peer = linkable.getNeighbor(i);

            // Selected peer should be active
            if (!peer.isUp()) {
                continue;
            }

            if (!canDownload()) {
                return;
            }

            final MediaStreamingProtocol p = (MediaStreamingProtocol) peer.getProtocol(protocolID);
            if (shouldDownload(p)) {
                doTransfer(p);
            }

            // The node already got needed chunks
            if (isFinished()) {
                return;
            }
        }

    }

    public void resetQuota() {
        downloadQuota = DOWNLOAD_QUOTA;
        uploadQuota = UPLOAD_QUOTA;
    }

    private boolean isFinished() {
        return value == totalChunks;
    }

    private boolean canDownload() {
        return downloadQuota > 0;
    }

    private boolean shouldDownload(MediaStreamingProtocol neighbor) {
        final boolean peerHasChunk = neighbor.value > value;
        final boolean peerCanUpload = neighbor.uploadQuota > 0;
        return peerHasChunk && peerCanUpload;
    }

    private void doTransfer(MediaStreamingProtocol neighbor) {
        // Determine maximum trasnfer possible
        double maxTrans = Math.min(downloadQuota, neighbor.uploadQuota);
        maxTrans = Math.min(maxTrans, neighbor.value - value);

        value += maxTrans;
        downloadQuota -= maxTrans;
        neighbor.uploadQuota -= maxTrans;
    }
}
