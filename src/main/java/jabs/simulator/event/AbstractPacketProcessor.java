package jabs.simulator.event;

import jabs.network.message.Packet;
import jabs.network.networks.Network;
import jabs.network.node.nodes.Node;
import jabs.simulator.randengine.RandomnessEngine;
import jabs.simulator.Simulator;

import java.util.PriorityQueue;

public abstract class AbstractPacketProcessor implements Event {
    //this class is also in the network interface... the AbstractPacketProcessor in a process that
    //is abstract to process in out packets
    protected final Simulator simulator; //Which is the simulator...queue...ecc
    protected final Network network;//...network... list of nodes ecc.
    protected final RandomnessEngine randomnessEngine;
    protected final PriorityQueue<TimedPacket> packetsQueue = new PriorityQueue<>();

    private record TimedPacket(Packet packet, double time) implements Comparable<TimedPacket> {
        public int compareTo(TimedPacket o) {
                return Double.compare(this.time, o.time);
            }
            //what is a timedpacket? a packet with a time
    }

    protected final Node node;

    public AbstractPacketProcessor(Simulator simulator, Network network, RandomnessEngine randomnessEngine, Node node) {
        this.simulator = simulator;
        this.network = network;
        this.randomnessEngine = randomnessEngine;
        this.node = node;
    }

    public boolean isQueueEmpty() {
        return (packetsQueue.isEmpty());
    }

    public void addToQueue(Packet packet) {
        TimedPacket timedPacket = new TimedPacket(packet, simulator.getSimulationTime());
        packetsQueue.add(timedPacket);
    }

    public Packet peek() {
        TimedPacket timedPacket = this.packetsQueue.peek();
        return timedPacket.packet;
    }

    public void execute() {
        //what do we do in execute? The next step in the packet processor is executed
        TimedPacket timedPacket = this.packetsQueue.poll();
        //the packet is pooled from the list
        if (timedPacket != null) { //if there is something in the list
            if (!this.packetsQueue.isEmpty()) {
                simulator.putEvent(this, processingTime(timedPacket.packet));
                //we put the event in the simulator

            }
            this.sendPacketToNextProcess(timedPacket.packet);
            //ok what means send packet to next processor?
        }
    }

    public abstract double processingTime(Packet packet);
    protected abstract void sendPacketToNextProcess(Packet packet);

    public Node getNode() {
        return node;
    }
}
