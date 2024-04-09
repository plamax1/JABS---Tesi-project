package jabs.network.message;

import jabs.network.node.nodes.Node;

public class Packet {
    //just a simple packet class, with size, from, to, message
    private final int size;
    private final Node from;
    private final Node to;
    private final Message message;
    //This is the packet class

    public int getSize(){ return this.size; }
    public Node getFrom(){ return this.from; } //The node that is sending the packet
    public Node getTo(){ return this.to; } //The node to which the packet will be sent
    public Message getMessage(){ return this.message; } //the message of the packet

    public Packet(Node from, Node to, Message message) {
        this.size = message.getSize();
        this.from = from;
        this.to = to;
        this.message = message;
    }
}
