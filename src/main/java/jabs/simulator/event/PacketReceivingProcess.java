package jabs.simulator.event;

import jabs.network.message.Packet;
import jabs.network.networks.Network;
import jabs.network.node.nodes.Node;
import jabs.simulator.randengine.RandomnessEngine;
import jabs.simulator.Simulator;

public class PacketReceivingProcess extends AbstractPacketProcessor {
    //this is the process that receives the packets
    public PacketReceivingProcess(Simulator simulator, Network network, RandomnessEngine randomnessEngine, Node node) {
        super(simulator, network, randomnessEngine, node);
    }

    protected void sendPacketToNextProcess(Packet packet) {
        this.node.processIncomingPacket(packet);
        //processincomingpacket, ma perche si chiama sendpackettonextprocess
    }

    public double processingTime(Packet packet) {
        //return the processing time on the packet, based on the size of the packet
        return ((packet.getSize()*8) / (((double) node.getNodeNetworkInterface().downloadBandwidth)));
    }
}

