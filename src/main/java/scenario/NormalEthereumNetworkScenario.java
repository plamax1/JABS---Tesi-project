package main.java.scenario;

import main.java.network.BlockchainNetwork;
import main.java.network.EthereumGlobalBlockchainNetwork;
import main.java.node.nodes.BlockchainNode;
import main.java.random.Random;

import static main.java.event.EventFactory.createBlockGenerationEvents;
import static main.java.event.EventFactory.createTxGenerationEvents;

public class NormalEthereumNetworkScenario extends AbstractScenario {
    long simulationTime = 0;

    private final int numOfMiners;
    private final int numOfNonMiners;
    private final long simulationStopTime;
    private final long txGenerationRate;
    private final long blockGenerationRate;

    public NormalEthereumNetworkScenario(long seed, int numOfMiners, int numOfNonMiners, long simulationStopTime, long txGenerationRate, long blockGenerationRate) {
        this.random = new Random(seed);
        this.numOfMiners = numOfMiners;
        this.numOfNonMiners = numOfNonMiners;
        this.simulationStopTime = simulationStopTime;
        this.txGenerationRate = txGenerationRate;
        this.blockGenerationRate = blockGenerationRate;
    }

    @Override
    public void setupSimulation() {
        this.network = new EthereumGlobalBlockchainNetwork(random);
        createTxGenerationEvents(simulator, random, network, ((int) (simulationStopTime*txGenerationRate)), (long)(1000/txGenerationRate));
        createBlockGenerationEvents(simulator,random, (BlockchainNetwork) network, ((int) (simulationStopTime*blockGenerationRate)), (long)(1000/blockGenerationRate));
    }

    @Override
    public boolean simulationStopCondition() {
        if (simulator.getCurrentTime() - 10000 >= simulationTime) {
            simulationTime = simulator.getCurrentTime();
            System.out.printf("\rsimulation time: %s, number of already seen blocks for miner 0: %s\n",
                    simulationTime,
                    ((BlockchainNode) network.getAllNodes().get(0)).numberOfAlreadySeenBlocks());
        }
        return (simulator.getCurrentTime() > simulationStopTime*1000);
    }

    @Override
    public void outputResults() {

    }
}