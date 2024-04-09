package jabs.network.networks.ethereum;

import jabs.consensus.config.ChainBasedConsensusConfig;
import jabs.consensus.config.GenericConsensusAlgorithmConfig;
import jabs.consensus.config.GhostProtocolConfig;
import jabs.ledgerdata.ethereum.EthereumBlock;
import jabs.network.networks.GlobalProofOfWorkNetwork;
import jabs.network.stats.*;
import jabs.network.node.nodes.ethereum.EthereumMinerNode;
import jabs.network.node.nodes.ethereum.EthereumNode;
import jabs.simulator.randengine.RandomnessEngine;
import jabs.simulator.Simulator;

import java.util.HashSet;

public class EthereumGlobalProofOfWorkNetwork<R extends Enum<R>> extends //this class of course extends
//Pow network
        GlobalProofOfWorkNetwork<EthereumNode, EthereumMinerNode, EthereumBlock, R> {
    public EthereumGlobalProofOfWorkNetwork(RandomnessEngine randomnessEngine,
                                            ProofOfWorkGlobalNetworkStats<R> networkStats) {
        super(randomnessEngine, networkStats);
    }

    /**
     * @param difficulty Difficulty of genesis block
     * @return Parent-less block that could be used for genesis block
     */
    @Override
    public EthereumBlock genesisBlock(double difficulty) { //this is for the generation of the genesis block
        return new EthereumBlock(0, 0, 0, null, null, new HashSet<>(), difficulty,
                0);
    }


    //We create the sample node for when we will populate the network
    @Override
    public EthereumNode createSampleNode(Simulator simulator, int nodeID, EthereumBlock genesisBlock,
                                         GenericConsensusAlgorithmConfig chainBasedConsensusConfig) {
        R region = this.sampleRegion();
        return new EthereumNode(simulator, this, nodeID, this.sampleDownloadBandwidth(region),
                this.sampleUploadBandwidth(region),
                genesisBlock, (GhostProtocolConfig) chainBasedConsensusConfig);
    }

    //We will create the sample miner for when we will populate the network
    @Override
    public EthereumMinerNode createSampleMiner(Simulator simulator, int nodeID, double hashPower,
                                               EthereumBlock genesisBlock,
                                               GenericConsensusAlgorithmConfig chainBasedConsensusConfig) {
        R region = this.sampleMinerRegion();
        return new EthereumMinerNode(simulator, this, nodeID, this.sampleDownloadBandwidth(region),
                this.sampleUploadBandwidth(region), hashPower,
                genesisBlock, (GhostProtocolConfig) chainBasedConsensusConfig);
    }

}
