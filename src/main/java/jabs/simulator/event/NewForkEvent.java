package jabs.simulator.event;

import jabs.network.node.nodes.Node;

public class NewForkEvent extends AbstractLogEvent {
    //event for the confirmation of a block...
    /**
     * This is the node that confirms a block.
     */
    private final Node node;
    /**
     * The block that gets confirmed
     */
    public NewForkEvent(double time, Node node) {
        super(time);
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public double getTime(){return time;}

}
