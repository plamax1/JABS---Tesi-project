package jabs.log;

import jabs.ledgerdata.Block;
import jabs.network.node.nodes.Node;
import jabs.simulator.event.BlockConfirmationEvent;
import jabs.simulator.event.Event;
import jabs.simulator.event.NewUncleEvent;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

public class LiveUncleBlocksLogger extends AbstractCSVLogger {
    /**
     * creates an abstract CSV logger
     * @param writer this is output CSV of the logger
     */
    public LiveUncleBlocksLogger(Writer writer) {
        super(writer);
    }

    /**
     * creates an abstract CSV logger
     * @param path this is output path of CSV file
     */
    public LiveUncleBlocksLogger(Path path) throws IOException {
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
        return event instanceof NewUncleEvent;
    }

    @Override
    protected boolean csvOutputConditionFinalPerNode() {
        return false;
    }

    @Override
    protected String[] csvHeaderOutput() {
        return new String[]{"SimulationTime", "NodeID", "CurrentNumUncles"};
    }

    @Override
    protected String[] csvEventOutput(Event event) {
        Node node = ((NewUncleEvent) event).getNode();
        int currentNumUncles = ((NewUncleEvent) event ).getCurrentNumUncles();

        return new String[]{
                Double.toString(this.scenario.getSimulator().getSimulationTime()),
                Integer.toString(node.nodeID),
                Integer.toString(currentNumUncles)
        };
    }
}
