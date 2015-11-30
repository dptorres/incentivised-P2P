import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.vector.SingleValueHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Danah Veronica Torres on 11/29/15.
 */
public class MediaStreamingProtocol extends SingleValueHolder implements CDProtocol, Comparable<MediaStreamingProtocol> {

    protected static final String PAR_CHUNKS = "chunks";
    private final double totalChunks;

    private static final double DOWNLOAD_QUOTA = 20.0;
    private static final double UPLOAD_QUOTA = 5.0;

    private double downloadQuota;
    private double uploadQuota;

    private double download = 0.0;
    private double upload = 0.0;

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
        List<MediaStreamingProtocol> sortedNodes = new ArrayList<MediaStreamingProtocol>();

        // Sort neighbors
        for (int i = 0; i < linkable.degree(); i++) {
            Node n = linkable.getNeighbor(i);
            MediaStreamingProtocol p = (MediaStreamingProtocol) n.getProtocol(protocolID);
            sortedNodes.add(p);
        }

        Collections.sort(sortedNodes);

        // Access all neighbors to detect the most distant
        for (int i = 0; i < linkable.degree(); i++) {

            final Node peer = linkable.getNeighbor(i);

            // Selected peer should be inactive
            if (!peer.isUp()) {
                continue;
            }

            if (!canUpload()) {
                return;
            }

            final MediaStreamingProtocol p = (MediaStreamingProtocol) peer.getProtocol(protocolID);
            if (shouldUpload(p)) {
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

    private boolean canUpload() {
        return upload > 0;
    }

    private boolean shouldUpload(MediaStreamingProtocol neighbor) {
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

    private double getScore() {
        return upload/download;
    }

    @Override
    public int compareTo(MediaStreamingProtocol o) {
        double currScore = getScore();
        double comScore = o.getScore();

        if (currScore == comScore) {
            return 0;
        } else {
            return currScore > comScore ? 1 : -1;
        }
    }
}
