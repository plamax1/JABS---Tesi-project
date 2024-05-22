package jabs.network.networks.SycoGhost;

import jabs.consensus.config.GenericConsensusAlgorithmConfig;
import jabs.consensus.config.SycoGhostProtocolConfig;
import jabs.consensus.config.SycomoreProtocolConfig;
import jabs.ledgerdata.sycoghost.SycoGhostBlock;
import jabs.ledgerdata.sycomore.SycomoreBlock;
import jabs.network.networks.GlobalProofOfWorkNetwork;
import jabs.network.node.nodes.sycoghost.SGBlockHeader;
import jabs.network.node.nodes.sycoghost.SycoGhostMinerNode;
import jabs.network.node.nodes.sycoghost.SycoGhostNode;
import jabs.network.node.nodes.sycomore.BlockHeader;
import jabs.network.node.nodes.sycomore.SycomoreMinerNode;
import jabs.network.node.nodes.sycomore.SycomoreNode;
import jabs.network.stats.ProofOfWorkGlobalNetworkStats;
import jabs.simulator.Simulator;
import jabs.simulator.randengine.RandomnessEngine;

import java.util.HashSet;

public class SycoGhostGlobalProofOfWorkNetwork<R extends Enum<R>> extends //Sycomore POW network
        GlobalProofOfWorkNetwork<SycoGhostNode, SycoGhostMinerNode, SycoGhostBlock, R> {//uses ethereum things // to edit
    // We use syc block syc node and syc syc miner node
    public SycoGhostGlobalProofOfWorkNetwork(RandomnessEngine randomnessEngine,
                                             ProofOfWorkGlobalNetworkStats<R> networkStats) {
        super(randomnessEngine, networkStats);
        //System.err.println("NETWORK CONSTRUCTOR NODES SIZE: " + String.valueOf(this.nodes.size()));
        //TxGenerationProcessRandomNetworkNode txGenerationProcessRandomNetworkNode = new TxGenerationProcessRandomNetworkNode(this.getRandomNode().getSimulator(), this, new RandomnessEngine(94656456), 1);
    }

    /**
     *
     * @param difficulty Difficulty of genesis block
     * @return Parent-less block that could be used for genesis block
     */
    @Override

   /* public EthereumBlock genesisBlock(double difficulty) {
        return new EthereumBlock(0, 0, 0, null, null, new HashSet<>(), difficulty,
                0);
    }*/

    //
       public SycoGhostBlock genesisBlock(double difficulty) {
            return new SycoGhostBlock(new SGBlockHeader(), "", 0, 0, 0, 0, null, null, new HashSet<>(), difficulty,
              0);
       }
@Override
    public SycoGhostNode createSampleNode(Simulator simulator, int nodeID, SycoGhostBlock genesisBlock,
                                         GenericConsensusAlgorithmConfig chainBasedConsensusConfig) {
        R region = this.sampleRegion();
        return new SycoGhostNode(simulator, this, nodeID, this.sampleDownloadBandwidth(region),
                this.sampleUploadBandwidth(region),
                genesisBlock, (SycoGhostProtocolConfig) chainBasedConsensusConfig);
    }
@Override
    public SycoGhostMinerNode createSampleMiner(Simulator simulator, int nodeID, double hashPower,
                                               SycoGhostBlock genesisBlock,
                                               GenericConsensusAlgorithmConfig chainBasedConsensusConfig) {
        R region = this.sampleMinerRegion();
        return new SycoGhostMinerNode(simulator, this, nodeID, this.sampleDownloadBandwidth(region),
                this.sampleUploadBandwidth(region), hashPower,
                genesisBlock, (SycoGhostProtocolConfig) chainBasedConsensusConfig);
    }

}
