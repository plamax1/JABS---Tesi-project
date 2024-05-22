package jabs.network.networks.sycomore;

import jabs.consensus.config.ChainBasedConsensusConfig;
import jabs.consensus.config.GenericConsensusAlgorithmConfig;
import jabs.consensus.config.SycomoreProtocolConfig;
import jabs.ledgerdata.sycomore.SycomoreBlock;
import jabs.network.networks.GlobalProofOfWorkNetwork;
import jabs.network.node.nodes.sycomore.BlockHeader;
import jabs.network.node.nodes.sycomore.SycomoreMinerNode;
import jabs.network.node.nodes.sycomore.SycomoreNode;
import jabs.network.stats.ProofOfWorkGlobalNetworkStats;
import jabs.simulator.Simulator;
import jabs.simulator.event.TxGenerationProcessRandomNetworkNode;
import jabs.simulator.randengine.RandomnessEngine;

import java.util.HashSet;

public class SycomoreGlobalProofOfWorkNetwork<R extends Enum<R>> extends //Sycomore POW network
        GlobalProofOfWorkNetwork<SycomoreNode, SycomoreMinerNode, SycomoreBlock, R> {//uses ethereum things // to edit
    // We use syc block syc node and syc syc miner node

    public SycomoreGlobalProofOfWorkNetwork(RandomnessEngine randomnessEngine,
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

       public SycomoreBlock genesisBlock(double difficulty) {
            return new SycomoreBlock(new BlockHeader(), "", 0, 0, 0, 0, null, null, new HashSet<>(), difficulty,
              0);
       }
@Override
    public SycomoreNode createSampleNode(Simulator simulator, int nodeID, SycomoreBlock genesisBlock,
                                         GenericConsensusAlgorithmConfig chainBasedConsensusConfig) {
        R region = this.sampleRegion();
        return new SycomoreNode(simulator, this, nodeID, this.sampleDownloadBandwidth(region),
                this.sampleUploadBandwidth(region),
                genesisBlock, (SycomoreProtocolConfig) chainBasedConsensusConfig);
    }
@Override
    public SycomoreMinerNode createSampleMiner(Simulator simulator, int nodeID, double hashPower,
                                               SycomoreBlock genesisBlock,
                                               GenericConsensusAlgorithmConfig chainBasedConsensusConfig) {
           //System.err.println("Creating a new miner node" + " with hash power " + hashPower + " and nodeID " + nodeID);
        R region = this.sampleMinerRegion();
        return new SycomoreMinerNode(simulator, this, nodeID, this.sampleDownloadBandwidth(region),
                this.sampleUploadBandwidth(region), hashPower,
                genesisBlock, (SycomoreProtocolConfig) chainBasedConsensusConfig);
    }

}
