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

    private double downloadedChunks;
    private double uploadedChunks;

    public MediaStreamingProtocol(String prefix) {
        super(prefix);
        totalChunks = Configuration.getDouble(prefix + "." + PAR_CHUNKS, 100.0);
        // TODO: add this to configuration
        downloadQuota = DOWNLOAD_QUOTA;
        uploadQuota = UPLOAD_QUOTA;
    }

    @Override
    public void nextCycle(Node node, int protocolID) {

        if (!canUpload()) {
            // upload quota reached
            return;
        }

        // Get ID of the protocol to be able to access the neighbors of the node
        final int linkableID = FastConfig.getLinkable(protocolID);
        final Linkable linkable = (Linkable) node.getProtocol(linkableID);

        final List<MediaStreamingProtocol> protocols = new ArrayList<>();

        // Sort neighbors according to score
        for (int i = 0; i < linkable.degree(); i++) {
            final Node peer = linkable.getNeighbor(i);
            // should be active
            if (!peer.isUp()) {
                continue;
            }
            protocols.add((MediaStreamingProtocol) peer.getProtocol(protocolID));
        }

        Collections.sort(protocols);

        // Access all neighbors
        for (int i = 0; i < protocols.size(); i++) {

            final MediaStreamingProtocol p = protocols.get(i);

            if (shouldUpload(p)) {
                doTransfer(p);
            }

            if (!canUpload()) {
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
        return uploadQuota > 0;
    }

    private boolean shouldUpload(MediaStreamingProtocol neighbor) {
        final boolean peerNeedsChunk = neighbor.value < value;
        final boolean peerCanDownload = neighbor.downloadQuota > 0;
        return peerNeedsChunk && peerCanDownload;
    }

    private void doTransfer(MediaStreamingProtocol neighbor) {
        // Determine maximum transfer possible
        double maxTrans = Math.min(uploadQuota, neighbor.downloadQuota);
        maxTrans = Math.min(maxTrans, value - neighbor.value);

        neighbor.value += maxTrans;
        neighbor.downloadQuota -= maxTrans;
        neighbor.downloadedChunks += maxTrans;

        uploadQuota -= maxTrans;
        uploadedChunks += maxTrans;
    }

    private double getScore() {
        return uploadedChunks / (downloadedChunks + 1);
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
