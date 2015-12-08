import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.util.IncrementalStats;
import peersim.vector.SingleValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Danah Veronica Torres on 12/5/15.
 */
public class ScoreObserver implements Control {
    private static final String PAR_ACCURACY = "accuracy";
    private static final String PAR_PROT = "protocol";
    private final String name;
    private final double accuracy;
    private final int pid;

    public ScoreObserver(String var1) {
        this.name = var1;
        this.accuracy = Configuration.getDouble(var1 + "." + "accuracy", -1.0D);
        this.pid = Configuration.getPid(var1 + "." + "protocol");
    }

    public boolean execute() {
        long var1 = CommonState.getTime();
        final List<MediaStreamingProtocol> protocols = new ArrayList<>();


        for(int var4 = 0; var4 < Network.size(); ++var4) {
            // Sort nodes by score
            // Get first 5000; get last 5000
            // Cycle X Chunks ng high vs low score. Get average score, get average chunks
            protocols.add((MediaStreamingProtocol) Network.get(var4).getProtocol(this.pid));

        }
        Collections.sort(protocols);

        List<MediaStreamingProtocol> high;
        List<MediaStreamingProtocol> low;

        high = protocols.subList(0, 5000);
        low = protocols.subList(45000, 50000);

        IncrementalStats highStats = new IncrementalStats();
        IncrementalStats highStatsScore = new IncrementalStats();

        for(int var4 = 0; var4 < high.size(); ++var4) {
            MediaStreamingProtocol var5 = high.get(var4);
            highStats.add(var5.getValue());
            highStatsScore.add(var5.getScore());
        }

        IncrementalStats lowStats = new IncrementalStats();
        IncrementalStats lowStatsScore = new IncrementalStats();

        for(int var4 = 0; var4 < low.size(); ++var4) {
            MediaStreamingProtocol var5 = low.get(var4);
            lowStats.add(var5.getValue());
            lowStatsScore.add(var5.getScore());
        }


        System.out.println(this.name + ": " + var1 + " " + highStats.getAverage() + ", " + highStatsScore.getAverage() +  " " + lowStats.getAverage() + ", " + lowStatsScore.getAverage());
        return highStats.getStD() <= this.accuracy || lowStats.getStD() <= this.accuracy;
    }
}
