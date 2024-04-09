package jabs.ledgerdata;

import jabs.network.node.nodes.Node;

public class Query extends BasicData { //This is a query type of data, we have a node which is an
    //inquirer
    private final Node inquirer;

    protected Query(int size, Node inquirer) {
        super(size);
        this.inquirer = inquirer;
    }

    public Node getInquirer() {
        return inquirer;
    }

}
