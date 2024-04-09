package jabs.ledgerdata;

import jabs.network.node.nodes.Node;

public abstract class Vote extends BasicData {//This is a vote kind of data
    private final Node voter;

    protected Vote(int size, Node voter) {
        super(size);
        this.voter = voter;
    }

    public Node getVoter() {
        return voter;
    }
}
