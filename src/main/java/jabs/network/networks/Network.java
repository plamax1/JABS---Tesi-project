package jabs.network.networks;

import jabs.consensus.config.ConsensusAlgorithmConfig;
import jabs.network.stats.NetworkStats;
import jabs.network.node.nodes.Node;
import jabs.simulator.randengine.RandomnessEngine;
import jabs.simulator.Simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class Network<N extends Node, NodeType extends Enum<NodeType>> {
    //Network wich takes as parameters N which is node, and NodeType, because the network
    //is relative to a single node???
    protected final List<N> nodes = new ArrayList<N>(); //list of nodes
    protected final RandomnessEngine randomnessEngine;
    public final NetworkStats<NodeType> networkStats; //these are the stats of the network
    public final HashMap<N, NodeType> nodeTypes = new HashMap<>();
    //what is this hashmap between node and nodetype

    protected Network(RandomnessEngine randomnessEngine, NetworkStats<NodeType> networkStats) {
        //here we instantiate the network
        this.randomnessEngine = randomnessEngine;
        this.networkStats = networkStats;
    }

    public N getRandomNode() {
        System.err.println("NODES SIZE: " + String.valueOf(nodes.size()));
        return nodes.get(randomnessEngine.sampleInt(nodes.size()));
    } //get a random node
//So the network has a set of nodes, and we get a random node
    public List<N> getAllNodes() {
        return nodes;
    }

    public N getNode(int i) {
        return nodes.get(i);
    }

    public double getLatency(N from, N to) {
        return networkStats.getLatency(nodeTypes.get(from), nodeTypes.get(to));
    };
    //latency between 2 nodes
    //we get sampledownloadbandwidth from NodeType, this simply return the bandwidth
    public long sampleDownloadBandwidth(NodeType type) {
        return (long) (networkStats.sampleDownloadBandwidth(type)*1);
    };
    public long sampleUploadBandwidth(NodeType type){
        return (long) (networkStats.sampleUploadBandwidth(type)*1);
    };
    //then we have the functions populatenetwork
    public abstract void populateNetwork(Simulator simulator, ConsensusAlgorithmConfig consensusAlgorithmConfig);
    //populate network cosa fa? bho sistema i nodi nel network
    public abstract void populateNetwork(Simulator simulator, int numNodes,
                                         ConsensusAlgorithmConfig consensusAlgorithmConfig);

    //populate network will be implemented later...

    public abstract void addNode(N node); //function to addNode to the network

    public void addNode(N node, NodeType nodeType) {
        nodes.add(node);
        nodeTypes.put(node, nodeType);
    }

    public RandomnessEngine getRandom() {
        return this.randomnessEngine;
    }
}