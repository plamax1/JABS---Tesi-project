package jabs.scenario;

import jabs.consensus.config.GhostProtocolConfig;
import jabs.ledgerdata.ethereum.EthereumBlock;
import jabs.network.networks.ethereum.EthereumGlobalProofOfWorkNetwork;
import jabs.network.stats.sixglobalregions.ethereum.EthereumProofOfWorkGlobalNetworkStats6Regions;

import static jabs.network.stats.eightysixcountries.ethereum.EthereumProofOfWorkGlobalNetworkStats86Countries.ETHEREUM_DIFFICULTY_2022;

public class NormalEthereumNetworkScenario extends AbstractScenario {
    //This is Ethereum scenario
    private final double simulationStopTime; //we have as variables, simulationstoptime
    private final double averageBlockInterval; //and average block interval

    /**
     * @param name
     * @param seed
     * @param simulationStopTime
     * @param averageBlockInterval
     */
    //Here we define the scenario, with the name, the seed, the simulationStopTime and the
    //block interval
    public NormalEthereumNetworkScenario(String name, long seed,
                                         double simulationStopTime, double averageBlockInterval) {
        super(name, seed);
        this.simulationStopTime = simulationStopTime;
        this.averageBlockInterval = averageBlockInterval;
    }
//in the override method we override the abstract functions
    @Override
    public void createNetwork() { //method create network
        //We create an ethereum proof of work global network
        EthereumGlobalProofOfWorkNetwork<?> ethereumNetwork = new EthereumGlobalProofOfWorkNetwork<>(randomnessEngine,
                new EthereumProofOfWorkGlobalNetworkStats6Regions(randomnessEngine));
        this.network = ethereumNetwork;
        ethereumNetwork.populateNetwork(simulator,
                new GhostProtocolConfig(EthereumBlock.generateGenesisBlock(ETHEREUM_DIFFICULTY_2022),
                        this.averageBlockInterval));
        //So we have created the network and populated the network
    }

    @Override
    protected void insertInitialEvents() {
        ((EthereumGlobalProofOfWorkNetwork<?>) network).startAllMiningProcesses();
    }
    //we start all mining process
    @Override
    public boolean simulationStopCondition() {
        return (simulator.getSimulationTime() > simulationStopTime);
    }
}
