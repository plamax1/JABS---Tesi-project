package jabs.network.networks.sycomore;

import jabs.consensus.config.ChainBasedConsensusConfig;
import jabs.consensus.config.GhostProtocolConfig;
import jabs.consensus.config.SycomoreProtocolConfig;
import jabs.ledgerdata.ethereum.EthereumBlock;
import jabs.ledgerdata.sycomore.SycomoreBlock;
import jabs.network.networks.GlobalProofOfWorkNetwork;
import jabs.network.node.nodes.ethereum.EthereumMinerNode;
import jabs.network.node.nodes.ethereum.EthereumNode;
import jabs.network.node.nodes.sycomore.SycomoreMinerNode;
import jabs.network.node.nodes.sycomore.SycomoreNode;
import jabs.network.stats.ProofOfWorkGlobalNetworkStats;
import jabs.simulator.Simulator;
import jabs.simulator.randengine.RandomnessEngine;

import java.util.HashSet;

public class SycomoreProofOfWorkNetwork<R extends Enum<R>> extends //Sycomore POW network
        GlobalProofOfWorkNetwork<SycomoreNode, SycomoreMinerNode, SycomoreBlock, R> {//uses ethereum things // to edit
    // We use syc block syc node and syc syc miner node
    public SycomoreProofOfWorkNetwork(RandomnessEngine randomnessEngine,
                                      ProofOfWorkGlobalNetworkStats<R> networkStats) {
        super(randomnessEngine, networkStats);
    }

    /**
     * @param difficulty Difficulty of genesis block
     * @return Parent-less block that could be used for genesis block
     */
    @Override

   /* public EthereumBlock genesisBlock(double difficulty) {
        return new EthereumBlock(0, 0, 0, null, null, new HashSet<>(), difficulty,
                0);
    }*/

    //
       public SycomoreBlock genesisBlock(double difficulty) {
            return new SycomoreBlock(0, 0, 0, null, null, new HashSet<>(), difficulty,
              0);
       }

    @Override
    public SycomoreNode createSampleNode(Simulator simulator, int nodeID, SycomoreBlock genesisBlock,
                                         ChainBasedConsensusConfig chainBasedConsensusConfig) {
        R region = this.sampleRegion();
        return new SycomoreNode(simulator, this, nodeID, this.sampleDownloadBandwidth(region),
                this.sampleUploadBandwidth(region),
                genesisBlock, (SycomoreProtocolConfig) chainBasedConsensusConfig);
    }

    @Override
    public SycomoreMinerNode createSampleMiner(Simulator simulator, int nodeID, double hashPower,
                                               SycomoreBlock genesisBlock,
                                               ChainBasedConsensusConfig chainBasedConsensusConfig) {
        R region = this.sampleMinerRegion();
        return new SycomoreMinerNode(simulator, this, nodeID, this.sampleDownloadBandwidth(region),
                this.sampleUploadBandwidth(region), hashPower,
                genesisBlock, (SycomoreProtocolConfig) chainBasedConsensusConfig);
    }

}
