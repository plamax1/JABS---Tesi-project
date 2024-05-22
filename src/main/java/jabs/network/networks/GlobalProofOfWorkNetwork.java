package jabs.network.networks;

import jabs.consensus.config.ChainBasedConsensusConfig;
import jabs.consensus.config.ConsensusAlgorithmConfig;
import jabs.consensus.config.GenericConsensusAlgorithmConfig;
import jabs.ledgerdata.Block;
import jabs.ledgerdata.ProofOfWorkBlock;
import jabs.network.stats.MinerGlobalRegionDistribution;
import jabs.network.stats.ProofOfWorkGlobalNetworkStats;
import jabs.network.node.nodes.MinerNode;
import jabs.network.node.nodes.Node;
import jabs.simulator.Simulator;
import jabs.simulator.randengine.RandomnessEngine;

import java.util.ArrayList;
import java.util.List;
//what is GlobalProofofWorkNetwork
public abstract class GlobalProofOfWorkNetwork<N extends Node, M extends MinerNode, B extends Block<B>, R extends Enum<R>>
        extends GlobalNetwork<N, R> {
    //What is GlobalProofofWorkNetwork? Extends Node, MinerNode, and Block
    protected final List<MinerNode> miners = new ArrayList<>();
    protected final MinerGlobalRegionDistribution<R> minerDistribution;
    //So the minerdistribution is essentially the networkstats.
    public List<Double> hashPowers;
    public long totalHashPower;

    protected GlobalProofOfWorkNetwork(RandomnessEngine randomnessEngine, ProofOfWorkGlobalNetworkStats<R> networkStats) {
        super(randomnessEngine, networkStats);
        this.minerDistribution = networkStats;
    }

    public void startAllMiningProcesses() { //used in the scenario
        List<MinerNode> allMiners = this.getAllMiners();
        for (MinerNode miner: allMiners) {
            miner.startMining(); //Quindi nel global Proof of work network c'è la funzione per fare
            //lo start del mining..., e questa funzione chiama il miner startmining che chiama a sua volta i processes
        }
    }

    public List<MinerNode> getAllMiners() {
        return miners;
    }
    public R sampleMinerRegion() {
        return minerDistribution.sampleMinerRegion();
    }
    public MinerNode getMiner(int i) {
        return miners.get(i);
    }

    public void addMiner(M node) { //funzione to add a miner
        nodes.add((N) node);
        miners.add(node);
        nodeTypes.put((N) node, sampleMinerRegion());
    }

    protected double sampleHashPower() {
        return minerDistribution.sampleMinerHashPower();
    }

    public abstract B genesisBlock(double difficulty);
    public abstract N createSampleNode(Simulator simulator, int nodeID, B genesisBlock,
                                       GenericConsensusAlgorithmConfig chainBasedConsensusConfig);
    public abstract M createSampleMiner(Simulator simulator, int nodeID, double hashPower, B genesisBlock,
                                        GenericConsensusAlgorithmConfig chainBasedConsensusConfig);

    //Funzioni populateNetwork...


    public void populateNetwork(Simulator simulator, ConsensusAlgorithmConfig consensusAlgorithmConfig) {
        this.populateNetwork(simulator, minerDistribution.totalNumberOfMiners(), nodeDistribution.totalNumberOfNodes(),
                consensusAlgorithmConfig);
    }

    public void populateNetwork(Simulator simulator, int numNodes, ConsensusAlgorithmConfig consensusAlgorithmConfig) {
        int numMiners = (int) Math.floor(minerDistribution.shareOfMinersToAllNodes() * numNodes) + 1;
        this.populateNetwork(simulator, numMiners, numNodes-numMiners, consensusAlgorithmConfig);
    }

    public void populateNetwork(Simulator simulator, int numMiners, int numNonMiners,
                                ConsensusAlgorithmConfig consensusAlgorithmConfig) {
        //qui mettiamo a disposizione la funzione populateNetwork vera e propria, che
        //prende i miner, non miner, l'algoritmo di consenso e il sumulatore
        this.totalHashPower = 0;
        this.hashPowers = new ArrayList<>();
        for (int i = 0; i < numMiners; i++) {
            double hashPower = sampleHashPower();
            hashPowers.add(hashPower);
            totalHashPower += hashPower;
        } //calcoliamo l'hashpower totale

        //Perchè fa il cast????

        //ChainBasedConsensusConfig chainBasedConsensusConfig = (ChainBasedConsensusConfig) consensusAlgorithmConfig;
        GenericConsensusAlgorithmConfig chainBasedConsensusConfig = (GenericConsensusAlgorithmConfig) consensusAlgorithmConfig;

        double miningInterval = chainBasedConsensusConfig.averageBlockMiningInterval();
        //qui stabiliamo l'average block interval
        B genesisBlock = (B) chainBasedConsensusConfig.getGenesisBlock();
        double genesisDifficulty = ((ProofOfWorkBlock) genesisBlock).getDifficulty();
        //get the difficulty from the genesis block

        double hashPowerScale = genesisDifficulty / (totalHashPower * miningInterval);
        //For each miner we create a sample miner, for each node a sample node, and then
        //connect everything to the network.
        for (int i = 0; i < numMiners; i++) {
            //System.err.println("Appling hashpower scale: " + hashPowerScale + " to miner " + i + " with hashpower " + hashPowers.get(i));
            this.addMiner(createSampleMiner(simulator, i, hashPowerScale * hashPowers.get(i), genesisBlock,
                    chainBasedConsensusConfig));
        }

        for (int i = 0; i < numNonMiners; i++) {
            this.addNode(createSampleNode(simulator, numMiners + i, genesisBlock, chainBasedConsensusConfig));
        }

        for (Node node:this.getAllNodes()) {
            node.getP2pConnections().connectToNetwork(this);
        }
    }
}