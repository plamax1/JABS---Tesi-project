package jabs.log;

import jabs.ledgerdata.Block;
import jabs.ledgerdata.Tx;
import jabs.network.node.nodes.Node;
import jabs.network.node.nodes.PeerBlockchainNode;
import jabs.network.node.nodes.PeerDLTNode;
import jabs.network.node.nodes.sycoghost.SycoGhostNode;
import jabs.simulator.event.Event;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

/**
 *
 */
public class SycoGhostFinalUncleBlocksLogger extends AbstractCSVLogger {
    /**
     * creates an abstract CSV logger
     * @param writer this is output CSV of the logger
     */
    public SycoGhostFinalUncleBlocksLogger(Writer writer) {
        super(writer);
    }

    /**
     * creates an abstract CSV logger
     * @param path this is output path of CSV file
     */
    public SycoGhostFinalUncleBlocksLogger(Path path) throws IOException {
        super(path);
    }

    @Override
    protected String csvStartingComment() {
        return String.format("Simulation name: %s      Number of nodes: %d      Network type: %s", scenario.getName(),
                this.scenario.getNetwork().getAllNodes().size(), this.scenario.getNetwork().getClass().getSimpleName());
    }

    @Override
    protected boolean csvOutputConditionBeforeEvent(Event event) {
        return false;
    }

    @Override
    protected boolean csvOutputConditionAfterEvent(Event event) {
        return false;
    }

    @Override
    protected boolean csvOutputConditionFinalPerNode() {
        return true;
    }

    @Override
    protected String[] csvHeaderOutput() {
        return new String[]{"NodeID", "CanonicalChainLength", "NumUncles"};
    }

    @Override
    protected String[] csvEventOutput(Event event) {
        return new String[0];
    }

    @Override
    protected String[] csvNodeOutput(Node node) {
        int canonicalHeadLen =1;
                //((SycoGhostNode) node).getConsensusAlgorithm().getChainHeads().getHeight();
        int totalBlocks = ((PeerBlockchainNode) node).getConsensusAlgorithm().getLocalBlockTree().size();
        int numUncles = totalBlocks - canonicalHeadLen - 1; // The genesis block shall not be counted

        return new String[]{
                Integer.toString(node.nodeID),
                Integer.toString(canonicalHeadLen),
                Integer.toString(numUncles),
        };
    }
}
