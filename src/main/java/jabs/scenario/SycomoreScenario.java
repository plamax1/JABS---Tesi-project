package jabs.scenario;

import jabs.consensus.config.GhostProtocolConfig;
import jabs.consensus.config.SycomoreProtocolConfig;
import jabs.ledgerdata.ethereum.EthereumBlock;
import jabs.ledgerdata.sycomore.SycomoreBlock;
import jabs.network.networks.ethereum.EthereumGlobalProofOfWorkNetwork;
import jabs.network.networks.sycomore.SycomoreGlobalProofOfWorkNetwork;
import jabs.network.stats.sixglobalregions.ethereum.EthereumProofOfWorkGlobalNetworkStats6Regions;

import static jabs.network.stats.eightysixcountries.ethereum.EthereumProofOfWorkGlobalNetworkStats86Countries.ETHEREUM_DIFFICULTY_2022;

public class SycomoreScenario extends AbstractScenario {
    private final double simulationStopTime;
    private final double averageBlockInterval;

    /**
     * @param name
     * @param seed
     * @param simulationStopTime
     * @param averageBlockInterval
     */
    public SycomoreScenario(String name, long seed,
                                         double simulationStopTime, double averageBlockInterval) {
        super(name, seed);
        this.simulationStopTime = simulationStopTime;
        this.averageBlockInterval = averageBlockInterval;
    }

    @Override
    public void createNetwork() {
        SycomoreGlobalProofOfWorkNetwork<?> sycomoreNetwork =  new SycomoreGlobalProofOfWorkNetwork<>(randomnessEngine,
                new EthereumProofOfWorkGlobalNetworkStats6Regions(randomnessEngine));

        this.network = sycomoreNetwork;
        sycomoreNetwork.populateNetwork(simulator,
                new SycomoreProtocolConfig<>(SycomoreBlock.generateGenesisBlock(ETHEREUM_DIFFICULTY_2022),
                        this.averageBlockInterval));
    }

    @Override
    protected void insertInitialEvents() {
        ((SycomoreGlobalProofOfWorkNetwork<?>) network).startAllMiningProcesses();
    }

    @Override
    public boolean simulationStopCondition() {
        return (simulator.getSimulationTime() > simulationStopTime);
    }
}
