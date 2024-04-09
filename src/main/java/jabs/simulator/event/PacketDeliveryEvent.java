package jabs.simulator.event;

import jabs.network.message.Packet;

public class PacketDeliveryEvent implements Event {
    //what is PacketDeliveryEvent?
    public final Packet packet;

    public PacketDeliveryEvent(Packet packet) {
        this.packet = packet;
    }

    public void execute(){
        this.packet.getTo().getNodeNetworkInterface().addToDownLinkQueue(this.packet);
        //Quindi lui prende il getTo() di un paccketto, dove sta il nodo di destinazione,
        //prende la network interface del nodo e ci mette il paccketto nella queue dei pacchetti
        //in ingresso
    }
}
