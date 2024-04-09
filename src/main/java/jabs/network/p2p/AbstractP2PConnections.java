package jabs.network.p2p;

import jabs.network.networks.Network;
import jabs.network.node.nodes.Node;

import java.util.ArrayList;
import java.util.List;

// TODO recheck if it is a better method for implementing Abstract Routing Table
//p2p connections among nodes
public abstract class AbstractP2PConnections {
    //This is the routing table, to be understand better in the future
    protected Node node;//the node
    protected final List<Node> peerNeighbors = new ArrayList<>();
    //The list of neighbors nodes

    protected Node getNode() {
        return node;
    } //getnode
    public void setNode(Node node) { //setnode
        this.node = node;
    }
    public List<Node> getNeighbors(){
        return this.peerNeighbors;
    } //peerneighbors
    public abstract void connectToNetwork(Network network); //in che senso connecttonetwork?
    public abstract boolean requestConnection(Node node); //a chi request connection?
}
