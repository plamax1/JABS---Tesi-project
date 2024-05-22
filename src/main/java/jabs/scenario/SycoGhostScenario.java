package jabs.scenario;

import jabs.consensus.config.SycoGhostProtocolConfig;
import jabs.consensus.config.SycomoreProtocolConfig;
import jabs.ledgerdata.sycoghost.SycoGhostBlock;
import jabs.ledgerdata.sycomore.SycomoreBlock;
import jabs.network.networks.SycoGhost.SycoGhostGlobalProofOfWorkNetwork;
import jabs.network.networks.sycomore.SycomoreGlobalProofOfWorkNetwork;
import jabs.network.stats.sixglobalregions.SixRegions;
import jabs.network.stats.sixglobalregions.ethereum.EthereumProofOfWorkGlobalNetworkStats6Regions;

import static jabs.network.stats.eightysixcountries.ethereum.EthereumProofOfWorkGlobalNetworkStats86Countries.ETHEREUM_DIFFICULTY_2022;

public class SycoGhostScenario extends AbstractScenario {
    private final double simulationStopTime;
    private final double averageBlockInterval;

    /**
     * @param name
     * @param seed
     * @param simulationStopTime
     * @param averageBlockInterval
     */
    public SycoGhostScenario(String name, long seed,
                             double simulationStopTime, double averageBlockInterval) {
        super(name, seed);
        this.simulationStopTime = simulationStopTime;
        this.averageBlockInterval = averageBlockInterval;
    }

    @Override
    public void createNetwork() {
        SycoGhostGlobalProofOfWorkNetwork<?> sycoGhostNetwork = new SycoGhostGlobalProofOfWorkNetwork<>(randomnessEngine,
                new EthereumProofOfWorkGlobalNetworkStats6Regions(randomnessEngine));
        this.network = sycoGhostNetwork;
        sycoGhostNetwork.populateNetwork(simulator,
                new SycoGhostProtocolConfig<>(SycoGhostBlock.generateGenesisBlock(ETHEREUM_DIFFICULTY_2022),
                        this.averageBlockInterval));
    }

    @Override
    protected void insertInitialEvents() {
        ((SycoGhostGlobalProofOfWorkNetwork<?>) network).startAllMiningProcesses();
    }

    @Override
    public boolean simulationStopCondition() {
        return (simulator.getSimulationTime() > simulationStopTime);
    }
}