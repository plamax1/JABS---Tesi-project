package jabs.log;

import jabs.ledgerdata.Block;
import jabs.network.node.nodes.Node;
import jabs.simulator.event.Event;
import jabs.simulator.event.K_ConfirmationBlockEvent;
import jabs.simulator.event.NewUncleEvent;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

public class K_ConfirmationBlockLogger extends AbstractCSVLogger {
    /**
     * creates an abstract CSV logger
     * @param writer this is output CSV of the logger
     */
    private String K_level;
    public K_ConfirmationBlockLogger(Writer writer) {
        super(writer);
    }

    /**
     * creates an abstract CSV logger
     * @param path this is output path of CSV file
     */
    public K_ConfirmationBlockLogger(Path path, String K_level) throws IOException {
        super(path);
        this.K_level=K_level;
    }

    @Override
    protected String csvStartingComment() {
        return String.format("Simulation name: %s      Number of nodes: %d      Network type: %s      Confirmation Level: %s", scenario.getName(),
                this.scenario.getNetwork().getAllNodes().size(), this.scenario.getNetwork().getClass().getSimpleName(), K_level);
    }

    @Override
    protected boolean csvOutputConditionBeforeEvent(Event event) {
        return false;
    }

    @Override
    protected boolean csvOutputConditionAfterEvent(Event event) {
        return event instanceof K_ConfirmationBlockEvent;
    }

    @Override
    protected boolean csvOutputConditionFinalPerNode() {
        return false;
    }

    @Override
    protected String[] csvHeaderOutput() {
        return new String[]{ "NodeID", "BlockHash", "BlockCreationTime", "ConfirmationTime", "ConfirmationDelay"};
    }

    @Override
    protected String[] csvEventOutput(Event event) {
        Node node = ((K_ConfirmationBlockEvent) event).getNode();
        Block block = ((K_ConfirmationBlockEvent) event ).getBlock();

        return new String[]{
                Integer.toString(node.nodeID),
                Integer.toString(block.hashCode()),
                Double.toString(block.getCreationTime()),
                Double.toString(this.scenario.getSimulator().getSimulationTime()),
                Double.toString(this.scenario.getSimulator().getSimulationTime()- block.getCreationTime())

        };
    }
}
