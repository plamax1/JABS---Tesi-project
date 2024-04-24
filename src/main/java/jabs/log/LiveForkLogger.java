package jabs.log;

import jabs.network.node.nodes.Node;
import jabs.simulator.event.Event;
import jabs.simulator.event.NewForkEvent;
import jabs.simulator.event.NewUncleEvent;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

public class LiveForkLogger extends AbstractCSVLogger {
    /**
     * creates an abstract CSV logger
     * @param writer this is output CSV of the logger
     */
    public LiveForkLogger(Writer writer) {
        super(writer);
    }

    /**
     * creates an abstract CSV logger
     * @param path this is output path of CSV file
     */
    public LiveForkLogger(Path path) throws IOException {
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
        return event instanceof NewForkEvent;
    }

    @Override
    protected boolean csvOutputConditionFinalPerNode() {
        return false;
    }

    @Override
    protected String[] csvHeaderOutput() {
        return new String[]{"SimulationTime", "NodeID"};
    }

    @Override
    protected String[] csvEventOutput(Event event) {
        Node node = ((NewForkEvent) event).getNode();

        return new String[]{
                Double.toString(((NewForkEvent) event).getTime()),
                Integer.toString(node.nodeID),
        };
    }
}
