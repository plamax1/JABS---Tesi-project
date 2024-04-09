package jabs.simulator.event;

import jabs.ledgerdata.Block;
import jabs.network.node.nodes.Node;

public class NewUncleEvent extends AbstractLogEvent {
    //event for the confirmation of a block...
    /**
     * This is the node that confirms a block.
     */
    private final Node node;
    /**
     * The block that gets confirmed
     */
    private final int currentNumUncles;
    public NewUncleEvent(double time, Node node, int currentNumUncles) {
        super(time);
        this.node = node;
        this.currentNumUncles = currentNumUncles;
    }

    public Node getNode() {
        return node;
    }

    public int getCurrentNumUncles() {
        return currentNumUncles;
    }
}
