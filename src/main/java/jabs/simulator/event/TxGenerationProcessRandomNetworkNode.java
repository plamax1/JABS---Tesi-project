package jabs.simulator.event;

import jabs.network.networks.Network;
import jabs.simulator.randengine.RandomnessEngine;
import jabs.simulator.Simulator;

public class TxGenerationProcessRandomNetworkNode extends AbstractTxPoissonProcess {
    protected final Network network;

    public TxGenerationProcessRandomNetworkNode(Simulator simulator, Network network, RandomnessEngine randomnessEngine, double averageTimeBetweenTxs) {
        super(simulator, randomnessEngine, averageTimeBetweenTxs);
        this.network = network;
    }

    @Override
    public void generate() {
        //ok, in this case we have a process which randomly selects a node and generates a transaction
        this.node = network.getRandomNode();
        node.generateNewTransaction();
    }
}
